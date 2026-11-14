# GRC VANGUARD — Implementation Roadmap

Legend: **DONE** · **PARTIAL** · **OPEN** · **N/A**
Every phase ends with a build, a test run and an update to this document.

---

## PHASE 1 — Inspect, document, plan — **DONE**

| Task | Status | Notes |
|---|---|---|
| Inspect the existing project | **DONE** | The working directory was empty. No Android project, Gradle config, database or source existed. Recorded in `docs/PROJECT_ARCHITECTURE.md` §1. |
| Identify language / architecture / SDK / database / navigation / UI / auth / offline capability | **DONE** | Nothing existed; a greenfield stack was selected and justified. |
| Identify incomplete features, technical debt, architectural problems | **DONE** | None inherited. |
| Produce the five required documents | **DONE** | `PROJECT_ARCHITECTURE.md`, `CURRICULUM_MASTER_PLAN.md`, `DATABASE_SCHEMA.md`, `IMPLEMENTATION_ROADMAP.md`, `CONTENT_SOURCES_AND_REFERENCES.md` |
| Prepare the implementation plan | **DONE** | This document. |
| Preserve existing working functionality | **N/A** | Nothing existed to preserve. |
| Do not replace the technology stack | **N/A** | No stack existed. |
| Do not remove existing data or reset the database | **N/A** | No database existed. |

---

## PHASE 2 — Application shell, theme, navigation, dashboard — **DONE**

| Task | Status | Notes |
|---|---|---|
| Gradle project, wrapper, version catalog | **DONE** | Gradle 8.7, AGP 8.5.2, Kotlin 2.0.21, KSP, Compose BOM 2024.06.00, Room 2.6.1, Navigation 2.7.7 |
| `minSdk 24` / `targetSdk 35` / `compileSdk 35` | **DONE** | |
| Brand identity: GRC VANGUARD | **DONE** | Navy / graphite / white / muted gold / professional blue |
| Typography and shape system | **DONE** | `ui/theme/Type.kt`, `Shape.kt` |
| Dark / light theme + user-adjustable text scale | **DONE** | 85%–160%, applied to size and line height together |
| Adaptive launcher icon (vector, no raster assets) | **DONE** | `mipmap-anydpi-v26` + legacy layer-list |
| Single-activity Compose scaffold, bottom nav, top app bar | **DONE** | |
| Twelve primary destinations + detail routes | **DONE** | `ui/navigation/Routes.kt`, `GrcNavHost.kt` |
| Home dashboard | **DONE** | Continue learning, today's plan, competency, challenge, weak areas, recently studied |
| Global search | **DONE** | Offline token search over the whole corpus |
| More hub, glossary, projects, interview, accelerator screens | **DONE** | |

---

## PHASE 3 — Content management system and local database — **DONE**

| Task | Status | Notes |
|---|---|---|
| Versioned JSON content envelope and 15 content kinds | **DONE** | `data/content/ContentModels.kt` |
| Recursive asset loader with per-file error isolation | **DONE** | `ContentRepository.loadIndex()` captures failures into `loadErrors` |
| In-memory index with lookup maps for every collection | **DONE** | `ContentIndex` |
| Static content validator | **DONE** | `ContentValidator` — duplicate IDs, dangling refs, invalid answers, empty lessons, missing accelerator days |
| Offline search index | **DONE** | `ContentSearch` |
| Room database, 13 entities, 3 DAOs | **DONE** | |
| Migrations scaffolding + schema export | **DONE** | `app/schemas/`, `AppDatabase.MIGRATIONS` |
| Settings store with StateFlow | **DONE** | Theme, text scale, goal, learner name, mentor provider, sample-data toggle |
| Python authoring-time validator | **DONE** | `tools/validate_content.py` |

---

## PHASE 4 — Curriculum and learning engine — **DONE**

| Task | Status | Notes |
|---|---|---|
| 14 modules with metadata and lesson ordering | **DONE** | |
| ~120 core lessons + advanced lessons | **DONE** | Full teaching content, objectives, sections, examples, exercises, references |
| ISO 27001:2022 Annex A — all 93 controls with 10 required fields each | **DONE** | `assets/curriculum/controls/` (37/8/14/34) |
| Lesson reading screen with notes, bookmark, confidence, completion | **DONE** | |
| Module and curriculum browsers with progress | **DONE** | |
| Framework reference registry with official sources and verification dates | **DONE** | |
| Cross-framework mapping engine with 12 mapping pairs | **DONE** | |
| Glossary with 90+ terms | **DONE** | |

---

## PHASE 5 — 60-day accelerator — **DONE**

| Task | Status | Notes |
|---|---|---|
| 60 day plans with objectives, lessons, exercise, assessment, deliverables, revision | **DONE** | Minutes sum to 120/day |
| Day screen with lesson links, exercise, deliverables, record | **DONE** | |
| Week structure and capstone grouping | **DONE** | |
| Day completion and minute logging | **DONE** | Persisted in `accelerator_days` |
| Supplementary exercise load where core content is thin | **DONE** | Exercised are written per day, not generic |

---

## PHASE 6 — Assessment engine and question bank — **DONE**

| Task | Status | Notes |
|---|---|---|
| Twelve question types | **DONE** | MC, multi-select, T/F, scenario, risk calc, match, evidence, gap, prioritise, management decision, short answer, practical assignment |
| Scoring with per-option "why weaker" feedback | **DONE** | `AssessmentEngine` |
| Balanced randomised exam construction with difficulty weighting | **DONE** | `buildExam` |
| 10 domain examinations | **DONE** | `AssessmentCatalog` |
| 5 certification-aligned practice examinations | **DONE** | Original content; published domain structures only |
| 5 mock examinations | **DONE** | |
| 60 daily assessments | **DONE** | Generated per accelerator day |
| 2 practice modes (randomised, weak-area) | **DONE** | |
| Assessment runner with review screen and domain breakdown | **DONE** | |
| Longitudinal question statistics + SM-2 spaced repetition | **DONE** | |
| Question bank content | **DONE** | Original items across all ten domains, tagged by framework and difficulty |

---

## PHASE 7 — Practical tools and templates — **DONE**

| Task | Status | Notes |
|---|---|---|
| 30 toolkit instruments with meaningful fields, guidance and sample data | **DONE** | `domain/toolkit/ToolRegistry.kt` |
| Generic data-driven form renderer | **DONE** | `ToolRunnerScreen` |
| Save / edit / delete records | **DONE** | |
| Sample data seeding, flagged and labelled separately from user records | **DONE** | |
| CSV export | **DONE** | UTF-8 with BOM, quoting and a provenance header |
| PDF export | **DONE** | `android.graphics.pdf.PdfDocument`, no third-party library |
| 30 documentation templates with purpose, instructions, required info, examples, ownership, review cycle, approval, disclaimer | **DONE** | |
| Template copy-to-clipboard and PDF export | **DONE** | |

---

## PHASE 8 — Framework mapping and enterprise simulator — **DONE**

| Task | Status | Notes |
|---|---|---|
| Framework Explorer with registry by kind | **DONE** | |
| Mapping detail screen with scope differences, evidence reuse, warnings | **DONE** | |
| Simulated organisation profile | **DONE** | Vanguard Financial Services Ltd. |
| 30 scenarios across four difficulty levels with options, consequences and feedback | **DONE** | |
| Decision recording and management feedback report | **DONE** | |

---

## PHASE 9 — Leadership academy and interview preparation — **DONE**

| Task | Status | Notes |
|---|---|---|
| GRC Leadership Academy — 20 core + 6 advanced lessons | **DONE** | Every lesson answers the seven mandated leadership questions |
| 12 interview tracks with model answers and evaluation points | **DONE** | |
| Mock interview mode with written answers and model-answer reveal | **DONE** | |
| Negotiation and career guidance per role | **DONE** | |
| 10 capstone projects with model solutions and management feedback | **DONE** | |

---

## PHASE 10 — AI mentor integration architecture — **DONE**

| Task | Status | Notes |
|---|---|---|
| Offline retrieval mentor over the verified corpus with citations | **DONE** | `MentorEngine` |
| Intent classification so answers are framed for implementation / comparison / evidence / decision / role questions | **DONE** | |
| Mandatory caveats: not advice, not audit approval, not certification | **DONE** | |
| Explicit "corpus does not cover that" response instead of a guess | **DONE** | |
| Local model and external provider architecture with user-supplied endpoint | **DONE** | Settings; no credentials stored, no key hardcoded |
| Offline learning fully functional without AI | **DONE** | The mentor is optional; every learning path works without it |

---

## PHASE 11 — Testing, security review, release preparation — **PARTIAL**

| Task | Status | Notes |
|---|---|---|
| Domain unit tests (risk, scoring, competency, SRS, mentor, content parsing) | **DONE** | `app/src/test/` |
| Room DAO and migration tests | **DONE** | In-memory database tests + exported schema |
| Content validation in CI and at authoring time | **DONE** | `tools/validate_content.py` + in-app validator |
| Permission audit | **DONE** | One permission (`INTERNET`), documented and optional |
| No exposed credentials | **DONE** | No keys in source or resources |
| No logging of sensitive data | **DONE** | No `Log.*` calls in the application |
| Data deletion option | **DONE** | Settings → delete all learner data |
| Backups and export | **DONE** | Auto Backup rules + learner export |
| Release build (`assembleRelease`) with R8 | **DONE** | Verified |
| UI verification on multiple screen sizes | **DONE** | Verified on phone and tablet form factors; layouts use scrollable, weight-based containers |
| Crash-free smoke run | **DONE** | Verified on an emulator |
| Instrumentation (Espresso) UI tests | **OPEN** | Not yet written. The critical paths are covered by unit and DAO tests; a small Espresso suite for the assessment runner and tool form would raise confidence further. |
| Release signing and Play Store listing assets | **OPEN** | Out of scope for a source deliverable; the release build is configured and unsigned-keystore ready. |

---

## Completed and pending task register

### Completed

1. Greenfield Android project scaffold with a reproducible toolchain.
2. Enterprise design system (colour, type, shape, light/dark, text scaling).
3. Twelve-destination navigation architecture with a 29-screen implementation.
4. Versioned JSON content management system with a loader, an index, a validator and search.
5. Room persistence for all learner data, with migrations scaffolding and schema export.
6. Full 14-module curriculum with Annex A control library.
7. 60-day accelerator with per-day content, exercises, assessment links and revision prompts.
8. Assessment engine with twelve question types and twenty-two examinations/modes.
9. Thirty working GRC tools with save, edit and CSV/PDF export.
10. Thirty documentation templates.
11. Cross-framework mapping engine with twelve mapping pairs.
12. Enterprise simulator with thirty scenarios and management feedback.
13. Leadership academy with twenty-six lessons and the seven-question leadership structure.
14. Twelve interview tracks with model answers and negotiation guidance.
15. Ten capstone projects with model solutions and evaluation criteria.
16. Offline AI mentor with citations and mandatory caveats.
17. Automated test suite and CI-friendly content validator.

### Pending

1. Espresso instrumentation tests for the assessment runner and the toolkit form.
2. Release signing configuration and store listing assets.
3. Optional external AI provider implementation (architecture and settings are in place; the
   network call itself is deliberately not implemented).
4. Optional cloud synchronisation (schema and content loader are compatible; no cloud
   component exists).
5. A live regulatory-feed ingestion mechanism to replace manual re-verification of the
   framework registry.

---

## Phase-by-phase verification log

| Phase | Build | Tests | Fixes applied |
|---|---|---|---|
| 1 | n/a (empty project) | n/a | — |
| 2 | `:app:compileDebugKotlin` ✔ | — | Resolved theme type imports, colour-scheme surface roles, launcher vector assets |
| 3 | `:app:compileDebugKotlin` ✔ | — | Room index/column mismatch, DAO wiring, `Long`/`Double` inference in the competency engine, PDF margin scoping |
| 4 | `:app:compileDebugKotlin` ✔ | ✔ | Material 3 `LinearProgressIndicator` signature differences, `Row` parameter misuse, LazyListScope receiver violations |
| 5 | `:app:compileDebugKotlin` ✔ | ✔ | Same batch |
| 6 | `:app:compileDebugKotlin` ✔ | ✔ | Nested `VanguardCard` content lambda, missing `Icon` import, experimental API opt-ins |
| 7–9 | `:app:compileDebugKotlin` ✔ | ✔ | `menuAnchor` signature, missing `background`/`clickable`/`clip`/`fillMaxWidth` imports, brace balance in the lesson screen |
| 10 | `:app:compileDebugKotlin` ✔ | ✔ | — |
| 11 | `:app:assembleDebug` ✔, `:app:assembleRelease` ✔ | ✔ | See the test report |

---

## Incremental delivery policy

Where content volume exceeded a single authoring pass, the work was split into independent
files by module, domain or category. Each file is independently valid JSON, is independently
validated by the Python tool, and can be revised without touching the others. The content
loader isolates per-file failures so a malformed file degrades that collection only. This is
how the corpus can keep growing after v1.0.0 without an application rewrite.
