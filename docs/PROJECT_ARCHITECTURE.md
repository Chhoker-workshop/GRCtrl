# GRC VANGUARD — Project Architecture

**Product:** GRC VANGUARD — *From GRC Fundamentals to Enterprise Leadership*
**Platform:** Android (native), Kotlin, Jetpack Compose, offline-first
**Package:** `com.vanguard.grc`
**Status:** v1.0.0

---

## 1. Starting point

The working directory `GRCtrl` was **empty** at the start of this engagement. There was no
pre-existing Android project, no Gradle configuration, no database and no source code to
preserve. The "existing project" referenced in the brief was therefore built from first
principles in this repository, using the environment that was available:

| Component | Choice | Rationale |
|---|---|---|
| Language | Kotlin 2.0.21 | Required by Compose in 2026; the only sane choice for a new Android app |
| UI | Jetpack Compose (Material 3) | Modern, single-activity, no XML layout churn, fast iteration |
| Architecture | Single-module, MVVM with a hand-rolled service locator | The app has one bounded context and no need for multi-module build complexity; an explicit container is easier to reason about than a DI framework for this size |
| Persistence | Room 2.6.1 over SQLite | Standard Android local database; migrations, Flow support and testability |
| Content format | Versioned JSON in `assets/curriculum` | Plain-text, diffable, reviewable, extendable without shipping new Kotlin; supports a future remote content update mechanism |
| Serialisation | kotlinx.serialization | Compile-time generated serializers, no reflection, no extra runtime |
| Background work | Kotlin coroutines + Flow | Built into the stack; no WorkManager dependency because all computation is on-device and immediate |
| PDF export | `android.graphics.pdf.PdfDocument` | Platform API — no third-party rendering library, no extra permission |
| CSV export | Manual UTF-8 writer | Same reasoning; full control over quoting and header format |

**Toolchain used:** Gradle 8.7, Android Gradle Plugin 8.5.2, JDK 17 target (built on JDK 21),
compileSdk 35, minSdk 24, targetSdk 35.

### Preserved / not applicable

Because the directory was empty there was no existing functionality to preserve, no data to
protect and no migration burden from an unknown prior schema. The Room database is created at
**version 1** with a clean, forward-compatible design, and the migration array is already in
place so that v2+ migrations are additive.

---

## 2. Layered architecture

```
┌──────────────────────────────────────────────────────────────────────┐
│  ui/           Compose screens, shared components, navigation, theme │
│                GrcViewModel (single, scoped to the Activity)        │
├──────────────────────────────────────────────────────────────────────┤
│  domain/       Pure Kotlin logic, no Android dependencies            │
│                assessment/  competency/  risk/  srs/  export/         │
│                mentor/      toolkit/ (tool registry)                 │
├──────────────────────────────────────────────────────────────────────┤
│  data/         content/  local/ (Room)  repo/  settings/             │
│                content/ = JSON corpus loading, indexing, validation  │
├──────────────────────────────────────────────────────────────────────┤
│  assets/curriculum/   The versioned content corpus (JSON)           │
└──────────────────────────────────────────────────────────────────────┘
```

### Dependency rule

`ui → domain ← data`. The domain layer has no dependency on Android or on Room, which is why
`RiskEngine`, `AssessmentEngine`, `CompetencyEngine`, `SpacedRepetitionScheduler` and
`MentorEngine` are unit-testable on the JVM without Robolectric.

### Module map

| Package | Responsibility |
|---|---|
| `core/AppContainer` | Manual dependency container, lazily constructs the database, repositories and engines |
| `data/content` | `ContentModels` (DTOs), `ContentRepository` (loads + indexes the corpus), `ContentValidator` (static checks), `ContentSearch` (offline global search) |
| `data/local` | Room database, 13 entities, 3 DAOs, migrations |
| `data/repo` | `LearningRepository`, `AssessmentRepository`, `ToolkitRepository` |
| `data/settings` | `SettingsStore` (SharedPreferences + StateFlow) |
| `domain/assessment` | `AssessmentEngine` (scoring), `AssessmentCatalog` (exam definitions) |
| `domain/competency` | `CompetencyDomain` enum (10 domains), `CompetencyEngine` (evidence-weighted scoring) |
| `domain/risk` | `RiskEngine` (5×5 inherent/residual, FAIR-style quantification, control evaluation) |
| `domain/srs` | `SpacedRepetitionScheduler` (SM-2 style) |
| `domain/export` | `ExportService` (CSV + PDF via platform APIs) |
| `domain/mentor` | `MentorEngine` — retrieval and synthesis over the verified corpus |
| `domain/toolkit` | `ToolRegistry` — the 30 toolkit instrument definitions |
| `ui/theme` | Colour, type, shape system; dark/light; user-adjustable text scaling |
| `ui/components` | Shared Compose components (cards, chips, progress, callouts, tables) |
| `ui/navigation` | Routes, destinations, `GrcNavHost` |
| `ui/screens/*` | 29 screens grouped by feature |

---

## 3. Content management architecture

This is the part of the system that had to be designed first, because the curriculum is the
product.

### Storage layout

```
app/src/main/assets/curriculum/
├── modules/     MODULES
├── lessons/     LESSONS   (one file per module)
├── paths/       PATHS
├── accelerator/ ACCELERATOR (60 days)
├── frameworks/  FRAMEWORKS (reference registry)
├── mappings/    MAPPINGS  (cross-framework mapping pairs)
├── controls/    CONTROLS  (ISO/IEC 27001:2022 Annex A, split by category)
├── questions/   QUESTIONS (split by competency domain)
├── scenarios/   SCENARIOS
├── templates/   TEMPLATES
├── interviews/  INTERVIEWS
├── projects/    PROJECTS
├── glossary/    GLOSSARY
└── tools/       TOOLS (optional extensions to the built-in registry)
```

Every file uses the same envelope:

```json
{ "schemaVersion": "1.0.0", "kind": "LESSONS", "items": [ ... ] }
```

`ContentRepository` walks `assets/curriculum` recursively, parses each `.json` file, decodes
the `items` array according to `kind`, and builds a single in-memory `ContentIndex` with
lookup maps for every collection. A parse failure in one file is captured in
`ContentIndex.loadErrors` and does not prevent the rest of the corpus from loading.

### Why JSON in assets rather than a database

- The corpus is read-only at runtime; a database table would add no capability.
- Plain text is reviewable, diffable and translatable outside the toolchain.
- A future content-update mechanism can deliver a replacement corpus as an asset bundle or
  downloaded archive; the loader does not change.
- The corpus can be validated by a Python script in CI without an Android toolchain.

### Versioning and review metadata

Every lesson, control and template carries `version` and `lastReviewed`. Every framework entry
carries `edition`, `publicationDate`, `officialUrl`, `lastVerified` and `status`
(`ACTIVE` / `SUPERSEDED` / `WITHDRAWN` / `DRAFT`). This makes it possible to tell a learner
which content was checked when, and which regulatory facts must be re-verified.

### Validation

Two validators exist and they are deliberately consistent:

- `ContentValidator` (Kotlin) — runs in-app. Checks duplicate IDs, dangling lesson/question
  references, empty lessons, missing objectives, questions with no valid answer, options whose
  `correct` flag disagrees with `correctOptionIds`, scenarios with no high-scoring option,
  tools with selection fields that have no options, templates with no body, missing accelerator
  days, projects with no model solution.
- `tools/validate_content.py` — runs in CI and at authoring time. Mirrors the same rules and
  prints a report.

### Search

`ContentSearch` builds a tokenised index over lessons, controls, questions, frameworks,
scenarios, tools, templates, glossary terms, interview tracks and projects. Search scoring
rewards title matches, then tag matches, then body frequency. It runs entirely on-device with
no network access.

---

## 4. Navigation architecture

Single-Activity, Compose Navigation.

```
MainActivity
 └── MainScaffold  (bottom navigation bar: Home, Paths, Toolkit, Assess, More)
      └── GrcNavHost  (top app bar + NavHost)
```

The twelve primary destinations required by the brief are all first-class routes:

| Destination | Route | Notes |
|---|---|---|
| Home | `home` | Dashboard |
| Learning Paths | `paths` | Accelerator + extended tracks |
| Curriculum | `curriculum` | 14 modules |
| Practice Lab | `practice` | Question bank + targeted practice |
| GRC Toolkit | `toolkit` | 30 instruments |
| Framework Explorer | `explorer` | Registry + mapping engine |
| Assessments | `assessments` | Domain exams + mocks |
| GRC Simulator | `simulator` | Vanguard Financial Services |
| Templates | `templates` | Documentation library |
| AI Mentor | `mentor` | Offline retrieval mentor |
| Progress | `progress` | Competency + revision |
| Settings | `settings` | Appearance, data, mentor config |

Detail routes are parameterised: `lesson/{id}`, `module/{id}`, `control/{id}`, `tool/{id}`,
`mapping/{id}`, `scenario/{id}`, `template/{id}`, `project/{id}`, `glossary/{id}`,
`accelerator/{day}`, `assessment/{id}`, `interview/{trackId}`, `framework/{id}`, `search`,
`projects`, `interview`, `glossary`, `accelerator`, `more`.

Bottom-bar destinations use `popUpTo(startDestination) { saveState = true }` with
`restoreState = true`, so switching tabs preserves scroll position and state.

---

## 5. Data flow

### Progress and learning
`LessonScreen` → `LearningRepository` → `LearningDao` → Room. `markViewed` upserts on first
open; `markCompleted` sets the completion timestamp; `logStudySession` writes a daily minutes
row; `logAcceleratorProgress` updates the day record.

### Assessment
`AssessmentRunnerScreen` → `AssessmentEngine.buildExam` (balanced, randomised, difficulty-weighted)
→ collects answers → `AssessmentRepository.finaliseAttempt` → `AssessmentEngine.score` →
persists `assessment_responses`, updates `question_stats` (with SM-2 scheduling) and
finalises the attempt row → `CompetencyEngine.compute` recomputes the ten domain scores →
cached into `competency_scores`.

### Toolkit
`ToolRunnerScreen` renders a form generated from `ToolDefinition.fields`. Values are held in a
`Map<String, String>` and serialised to a JSON blob in `tool_records`. Sample records are
seeded once per tool and are always flagged `isSample = true` so the UI can label them
differently from user records. Export produces a CSV or a PDF via `ExportService`.

### Mentor
`MentorScreen` → `MentorEngine.ask` → `ContentSearch.search` → synthesised answer with
citations, actions and caveats. The engine is retrieval-only: it cannot invent a clause
because every sentence it produces is derived from corpus text it retrieved.

---

## 6. Security and privacy posture

| Concern | Position |
|---|---|
| Permissions | One: `INTERNET`. Required only by the *optional* external mentor provider. The app makes no network calls by default. |
| Data at rest | All learner data is in the app's private sandbox (Room database + SharedPreferences). Protected by the OS application sandbox. |
| No telemetry | No analytics SDK, no crash reporting, no advertising identifier, no account system. |
| Logging | The app logs nothing. No password, confidential record or personal datum is ever written to a log. |
| Credentials | No API key is hardcoded and none is stored. The external mentor endpoint is user-supplied and held locally in preferences. |
| Export | Files are written to `files/exports/` (private) and shared only through the system share sheet via `FileProvider` with a one-shot read grant. |
| Deletion | Settings → "Delete all learner data" clears progress, sessions, notes, bookmarks, attempts, responses, question statistics, competency cache, tool records, projects, decisions and interviews. |
| Sample separation | `tool_records.isSample` distinguishes shipped sample registers from user records, and the UI labels them differently everywhere they appear. |
| Cloud sync | Deliberately not implemented. The schema is local-only and the `ContentIndex`/Room design does not preclude a future optional sync layer, but no cloud component exists today. |

---

## 7. UI design system

**Identity.** Enterprise-grade, executive-facing, technically serious. The visual reference is
a security operations centre console or an enterprise risk platform — not an LMS.

**Palette** (`ui/theme/Color.kt`):

| Role | Token | Hex |
|---|---|---|
| Background (dark) | `Navy850` | `#0B1220` |
| Surface (dark) | `Navy800` | `#0E1626` |
| Container | `Graphite800` | `#161D2A` |
| Primary accent | `Blue500` | `#3F87D4` |
| Secondary / authority | `Gold400` | `#D4AF60` |
| Critical risk | `RiskCritical` | `#D2453F` |
| High risk | `RiskHigh` | `#E07A2F` |
| Moderate | `RiskMedium` | `#D8A93B` |
| Low | `RiskLow` | `#4FA36B` |
| Informational | `RiskInformational` | `#4C88C4` |

Dark navy and graphite carry the structure; muted gold marks authority, milestones and
executive-level emphasis; professional blue marks interaction and data. Semantic colours are
reserved for risk severity, control status and competency bands so that colour always carries
meaning and never decoration.

**Typography.** One family, tight tracking on headings (down to −0.5sp), generous line height
on body copy (15sp/24sp) for long-form study material. A user-adjustable scale from 85% to
160% is applied on top of the base ramp via `scaledTypography`, scaling sizes and line heights
together so the layout rhythm survives.

**Shape.** 4 / 8 / 12 / 18 / 26dp radii. Cards use `surfaceContainerLow` with an optional
1dp accent border; flat, quiet, dense enough for registers and matrices.

**Motion.** Deliberately minimal. No decorative animation. The only transitions are a top-bar
slide and a list-scroll behaviour.

---

## 8. Testing strategy

| Layer | What is tested | Where |
|---|---|---|
| Domain | Risk scoring, assessment scoring, competency computation, spaced repetition, mentor retrieval | `app/src/test/` (JVM, JUnit 4) |
| Data | Room DAOs: progress, notes, bookmarks, attempts, question statistics, tool records, projects | `app/src/test/` with an in-memory Room database |
| Content | JSON parsing, schema validation, duplicate detection, dangling references, accelerator completeness | `app/src/test/` + `tools/validate_content.py` |
| Migration | Schema export and migration path construction | `app/src/androidTest/` + exported schemas |
| UI | Manual verification on a phone/tablet emulator at multiple densities | Manual |

---

## 9. Known limitations (honest statement)

- The external AI mentor provider is **architecture only**. The offline corpus mentor is fully
  implemented and is the default. No third-party model call is made and no credential handling
  exists beyond storing a user-supplied endpoint URL.
- Cloud synchronisation is not implemented. The requirement was "optional and future-compatible";
  the local schema and content loader do not preclude it.
- Live regulatory verification at build time is not automated. Every regulatory entry carries
  an explicit "verify against the current published version" note, and the framework registry
  records a `lastVerified` date.
- The assessment bank is large but finite. Assessment selections are drawn from the available
  pool and the UI states the available pool size when it is smaller than the requested exam
  length.
