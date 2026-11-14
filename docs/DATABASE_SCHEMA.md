# GRC VANGUARD — Database Schema

**Database:** `grc_vanguard.db` (Room 2.6.1 over SQLite)
**Current version:** 1
**Schema export:** `app/schemas/` (Room `exportSchema = true`, so every migration is reviewable)

---

## Design principles

1. **Progress must never disappear.** Every learner-visible artefact — completion, notes,
   bookmarks, assessment history, competency, saved registers, project work — is in the
   database, not in memory.
2. **Content is separate from learner data.** The curriculum lives in `assets/curriculum` as
   JSON. The database holds only what the learner did. This means the corpus can be updated
   without touching learner history.
3. **Stable identifiers.** Progress, notes, bookmarks and question statistics key on the
   *content* identifier (for example `fnd.l01`), not on a row surrogate. Content can be
   re-versioned without orphaning history.
4. **Sample data is flagged, never mixed.** `tool_records.isSample` separates shipped
   demonstrations from real records, and the UI honours that distinction everywhere.
5. **Forward-only migrations.** Every schema change ships with an explicit `Migration` in
   `AppDatabase.MIGRATIONS`. Destructive fallback is **not** enabled, so a missed migration
   fails loudly in testing rather than silently deleting a learner's history in production.
6. **Minimal PII.** No passwords, no identity documents, no financial account data. The
   learner's optional name is a single display string.

---

## Entity relationship overview

```
lesson_progress ──┐
study_sessions  ──┤
notes           ──┼──► (learner activity)
bookmarks       ──┤
accelerator_days──┘

assessment_attempts ──1:N──► assessment_responses
question_stats          (independent longitudinal stats)
competency_scores       (cached projection of responses + stats)

tool_records
projects
simulator_decisions
interview_sessions
```

---

## Tables

### `lesson_progress`

One row per learning unit the learner has opened.

| Column | Type | Null | Notes |
|---|---|---|---|
| `contentId` | TEXT | no | **Primary key.** Content id, e.g. `fnd.l01` |
| `moduleId` | TEXT | no | Owning module id, denormalised for fast queries |
| `title` | TEXT | no | Snapshot of the title at last view |
| `completed` | INTEGER | no | 0/1 |
| `completedAt` | INTEGER | yes | Epoch millis |
| `firstViewedAt` | INTEGER | no | Epoch millis |
| `lastViewedAt` | INTEGER | no | Epoch millis |
| `timeSpentSeconds` | INTEGER | no | Accumulated, incremented in chunks |
| `bestScore` | INTEGER | yes | Best assessment score 0–100 |
| `revisionCount` | INTEGER | no | Number of revisits |
| `confidence` | INTEGER | yes | Self-reported 0–100 |
| `nextReviewAt` | INTEGER | yes | Spaced-repetition due date |
| `masteredObjectives` | TEXT | no | Comma-separated objective flags |

**Indexes:** `moduleId`, `completed`, `lastViewedAt`.

### `study_sessions`

Daily study-time ledger.

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | **Primary key**, auto-generated |
| `epochDay` | INTEGER | Epoch days since 1970 — a local calendar day number |
| `minutes` | INTEGER | Minutes studied |
| `contentId` | TEXT | Optional link to the unit studied |
| `activityType` | TEXT | `LESSON`, `ASSESSMENT`, `ACCELERATOR`, `TOOL`, `PROJECT` |
| `recordedAt` | INTEGER | Epoch millis |

**Index:** `epochDay`.

Drives: minutes today, minutes this week, streak calculation, daily goal adherence.

### `accelerator_days`

State of each of the 60 programme days.

| Column | Type | Notes |
|---|---|---|
| `day` | INTEGER | **Primary key**, 1–60 |
| `epochDay` | INTEGER | Calendar day it was worked on |
| `startedAt` | INTEGER | Epoch millis |
| `completedAt` | INTEGER | Epoch millis, null until completed |
| `minutesLogged` | INTEGER | Minutes logged against the day |
| `assessmentScore` | INTEGER | Score of the day's assessment |
| `notes` | TEXT | Free text |

### `notes`

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | **Primary key**, auto-generated |
| `contentId` | TEXT | The unit the note is attached to |
| `title` | TEXT | Display title |
| `body` | TEXT | Note body (Markdown-ish plain text) |
| `tags` | TEXT | Comma-separated |
| `createdAt` / `updatedAt` | INTEGER | Epoch millis |

**Indexes:** `contentId`, `updatedAt`.

### `bookmarks`

| Column | Type | Notes |
|---|---|---|
| `contentId` | TEXT | **Primary key** — one bookmark per item |
| `title`, `subtitle` | TEXT | Snapshot for the list view |
| `kind` | TEXT | `LESSON`, `CONTROL`, `TEMPLATE`, `SCENARIO`, `TOOL`, `QUESTION` |
| `createdAt` | INTEGER | Epoch millis |

### `assessment_attempts`

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | **Primary key**, auto-generated |
| `assessmentId` | TEXT | Catalogue id, e.g. `dom_governance`, `mock_full_2` |
| `title` | TEXT | Snapshot of the assessment title |
| `kind` | TEXT | `DOMAIN_EXAM`, `CERTIFICATION_PRACTICE`, `MOCK_EXAM`, `PRACTICE`, `DAILY` |
| `domain` | TEXT | Competency domain id, or `GENERAL` |
| `startedAt` | INTEGER | Epoch millis |
| `completedAt` | INTEGER | Epoch millis, null while in progress |
| `scorePercent` | INTEGER | 0–100 |
| `correctCount`, `totalCount` | INTEGER | Item counts |
| `timeSpentSeconds` | INTEGER | Wall clock time on the attempt |
| `passMark` | INTEGER | Threshold applied at scoring |
| `passed` | INTEGER | 0/1 |

**Indexes:** `assessmentId`, `completedAt`.

### `assessment_responses`

One row per answered item. This is the evidence base for competency.

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | **Primary key**, auto-generated |
| `attemptId` | INTEGER | FK → `assessment_attempts.id` (cascade delete) |
| `questionId` | TEXT | Content question id |
| `domain` | TEXT | Competency domain, denormalised for aggregation |
| `difficulty` | TEXT | `BEGINNER`/`INTERMEDIATE`/`ADVANCED`/`EXPERT` |
| `selectedOptionIds` | TEXT | Pipe-delimited option ids |
| `isCorrect` | INTEGER | 0/1 |
| `timeSpentSeconds` | INTEGER | Per item |
| `answeredAt` | INTEGER | Epoch millis — drives the recency discount |

**Indexes:** `attemptId`, `questionId`, `domain`, `isCorrect`.

### `question_stats`

Longitudinal statistics per question, powering weak-area detection and spaced repetition.

| Column | Type | Notes |
|---|---|---|
| `questionId` | TEXT | **Primary key** |
| `domain`, `topic` | TEXT | Denormalised for grouping |
| `seenCount` | INTEGER | Times presented |
| `correctCount`, `wrongCount` | INTEGER | Outcome counters |
| `streak` | INTEGER | Consecutive correct answers |
| `lastResultCorrect` | INTEGER | Boolean, null if never answered |
| `lastSeenAt` | INTEGER | Epoch millis |
| `nextReviewAt` | INTEGER | Epoch millis — SM-2 schedule |
| `easeFactor` | REAL | SM-2 ease, 1.3–2.8 |
| `intervalDays` | INTEGER | Current scheduling interval |

**Indexes:** `lastResultCorrect`, `nextReviewAt`.

### `competency_scores`

Cached projection of the ten domain scores so the dashboard renders instantly.

| Column | Type | Notes |
|---|---|---|
| `domain` | TEXT | **Primary key** — competency domain id |
| `score` | INTEGER | 0–100 |
| `confidenceWeight` | INTEGER | 0–100 confidence band |
| `questionsAnswered` | INTEGER | Evidence volume |
| `accuracy` | INTEGER | 0–100 raw accuracy |
| `updatedAt` | INTEGER | Epoch millis |

**Authoritative value:** always recomputed from `assessment_responses` + `question_stats`.
This table is a cache, refreshed by `AssessmentRepository.refreshCompetencyCache`.

### `tool_records`

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | **Primary key**, auto-generated |
| `toolId` | TEXT | Tool registry id, e.g. `risk_register` |
| `title` | TEXT | User-given or generated title |
| `dataJson` | TEXT | Field key → value map, JSON |
| `isSample` | INTEGER | 1 = shipped sample, 0 = learner's record |
| `createdAt`, `updatedAt` | INTEGER | Epoch millis |

**Indexes:** `toolId`, `isSample`, `updatedAt`.

### `projects`

| Column | Type | Notes |
|---|---|---|
| `projectId` | TEXT | **Primary key** |
| `title` | TEXT | Snapshot |
| `status` | TEXT | `NOT_STARTED`, `IN_PROGRESS`, `SUBMITTED`, `COMPLETED` |
| `progressPercent` | INTEGER | 0–100 |
| `answersJson` | TEXT | Learner's submitted work |
| `lastSection` | TEXT | Resume point |
| `updatedAt` | INTEGER | Epoch millis |

### `simulator_decisions`

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | **Primary key**, auto-generated |
| `scenarioId` | TEXT | Scenario content id |
| `difficulty` | TEXT | Difficulty at the time of the attempt |
| `optionIndex` | INTEGER | Chosen option index |
| `score` | INTEGER | 0–100 management score |
| `maxScore` | INTEGER | 100 |
| `rationale` | TEXT | Chosen option text (self-review) |
| `decidedAt` | INTEGER | Epoch millis |

**Index:** `scenarioId`, `decidedAt`.

### `interview_sessions`

| Column | Type | Notes |
|---|---|---|
| `id` | INTEGER | **Primary key**, auto-generated |
| `roleId` | TEXT | Interview track id |
| `roleTitle` | TEXT | Snapshot |
| `answersJson` | TEXT | The learner's written answers |
| `score` | INTEGER | Self-assessed overall score |
| `dimensionScoresJson` | TEXT | Per-dimension scores |
| `completedAt` | INTEGER | Epoch millis |

---

## DAOs

| DAO | Responsibility |
|---|---|
| `LearningDao` | Progress upsert/query, study sessions, streaks, notes, bookmarks, accelerator days |
| `AssessmentDao` | Attempts, responses, question statistics, due-for-review queries, competency cache |
| `ToolDao` | Tool records, projects, simulator decisions, interview sessions |

Key query patterns:

- `observeMinutesForDay(epochDay)` — daily goal adherence.
- `observeRecentActiveDays(limit)` — streak computation in memory.
- `observeWeakQuestions(limit)` — ordered by wrong ratio, then last seen.
- `observeDueForReview(now, limit)` — ordered by due date ascending.
- `observeCompletedCount()` — curriculum completion.

---

## Migrations

```kotlin
val MIGRATIONS: Array<Migration> = arrayOf()
```

Version 1 is the initial schema, so the array is currently empty. It is referenced from the
database builder so future versions add entries in one place without changing construction
sites. The builder **does not** call `fallbackToDestructiveMigration()`, so:

- a missing migration raises a clear error rather than silently wiping progress;
- `app/schemas/` holds the exported schema for every version, enabling automated migration
  testing with `MigrationTestHelper` in `androidTest`.

**Rules for every future version.**
1. Never renumber or reuse a table.
2. Always add columns with a `NOT NULL DEFAULT` so existing rows survive.
3. Never drop a table that holds learner data; archive it instead.
4. Export the new schema (`room.schemaLocation` is wired to `app/schemas`).
5. Add a `Migration` object and a migration test.

---

## Backup and export

- Android Auto Backup (`backup_rules.xml`) includes the database and settings, excluding the
  transient `exports/` directory.
- `data_extraction_rules.xml` mirrors this for Android 12+ device transfer.
- The Settings screen offers a learner-data export and a complete local deletion.
- Exports (CSV/PDF) are written to `files/exports/` and shared via `FileProvider` with a
  one-shot read URI permission.

---

## Privacy

The database contains only what the learner did: progress, notes, bookmarks, scores,
self-assessed confidence, tool records the learner typed, project drafts, scenario choices and
the optional display name. There are no passwords, no government identifiers, no financial
account numbers and no telemetry. The application does not log database contents, and no
confidential record is written to logcat.
