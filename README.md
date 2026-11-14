# GRC VANGUARD

GRC VANGUARD is an offline-first Android learning and practice platform for Governance, Risk, and Compliance (GRC). It delivers curriculum content, assessments, scenario simulations, practical toolkit workflows, progress tracking, and an offline mentor experience in a single Jetpack Compose app.

> Status: active development. Core architecture, navigation, domain engines, and a seeded content set are in place; curriculum/question coverage is being expanded toward the full master plan (see `docs/project-status/`).

## Features

- **Learning paths & curriculum**: modules, lessons, 60-day accelerator, projects, glossary, and framework/control explorer backed by packaged JSON content.
- **Practice & assessment**: question-driven practice, assessments, competency tracking, and spaced-repetition scheduling.
- **Scenario simulator & toolkit**: decision scenarios and practical GRC tools/registers with export support (CSV/PDF where implemented).
- **Mentor**: offline-first mentor flow (optional external/local provider configuration reserved in settings).
- **Progress & settings**: learner progress, bookmarks/notes, appearance/accessibility, and data management.
- **Account login**: sign-in gate validated via a configurable HTTPS endpoint (see [Login setup](#login-setup)).

## Tech stack

- **Language/UI**: Kotlin 2.0.21, Jetpack Compose, Material 3
- **Architecture**: single-module MVVM-style UI with manual DI (`AppContainer`)
- **Navigation**: Navigation Compose 2.7.7
- **Persistence**: Room 2.6.1 (SQLite), schema export in `app/schemas/`
- **Content**: kotlinx.serialization JSON assets under `app/src/main/assets/curriculum/`
- **Async**: Kotlin Coroutines / Flow
- **Build**: Gradle 8.7, AGP 8.5.2, JDK 17+

| Setting | Value |
|---|---|
| Application ID | `com.vanguard.grc` (debug suffix: `.debug`) |
| minSdk | 24 |
| targetSdk / compileSdk | 35 |
| Version | 1.0.0 (versionCode 1) |

## Project structure

```text
.
├─ app/                         # Android application module
│  ├─ src/main/java/com/vanguard/grc/
│  │  ├─ core/                  # AppContainer (manual DI)
│  │  ├─ data/                  # local (Room), content, repo, settings, auth
│  │  ├─ domain/                # assessment, competency, risk, srs, mentor, toolkit, export
│  │  └─ ui/                    # navigation, screens, components, theme, ViewModel
│  ├─ src/main/assets/curriculum/  # modules, lessons, questions, controls, scenarios, etc.
│  ├─ src/test/                 # unit tests
│  └─ schemas/                  # exported Room schemas
├─ docs/                        # architecture, curriculum plan, DB schema, roadmap, references
│  └─ project-status/           # audit, compliance matrix, tracker, status, logs
├─ tools/                       # content validation, schema docs, login endpoint template
├─ gradle/                      # wrapper & version catalog
├─ scripts/                     # local scratch scripts (gitignored)
└─ logs/                        # local logs (gitignored)
```

## Getting started

### Prerequisites

- Android Studio (recent stable) or Android SDK command-line tools
- JDK 17 (project targets JVM 17)
- Android SDK Platform 35

### Build

```powershell
# Debug APK
.\gradlew.bat :app:assembleDebug

# Unit tests
.\gradlew.bat :app:testDebugUnitTest
```

Outputs:
- APK: `app/build/outputs/apk/debug/app-debug.apk`
- Test report: `app/build/reports/tests/testDebugUnitTest/index.html`

Open the project in Android Studio (`File > Open` → repository root) to run on an emulator/device.

## Content validation

Curriculum/content assets are versioned JSON under `app/src/main/assets/curriculum/`. Validate structure and cross-references with:

```powershell
python tools\validate_content.py
```

See `tools/CONTENT_SCHEMA.md` for schema details. Current seeded snapshot (subject to change): 12 modules, 132 lessons, 215 questions, 93 controls, 20 frameworks, 24 scenarios, 8 templates, 8 projects, 60 accelerator days.

## Login setup

The app requires sign-in against a server-side HTTPS endpoint (it does not connect directly to a database).

1. Deploy the template `tools/grc_login.php` to your web host (e.g. `https://<your-domain>/api/grc_login.php`).
2. Configure database host/name/user/password in that PHP file and ensure the MySQL user has access to the login table (`SELECT` is sufficient). **Keep real credentials on the server only** (the committed template uses placeholders).
3. The endpoint expects `POST` JSON `{"username":"password"}` and returns `{"ok":true,"username":"...","message":"..."}` on success (401/400/500 JSON on failure).
4. In the app, set **Login API URL** on the sign-in screen or in **Settings → Account** (default: `https://cyberpantology.in/api/grc_login.php`).

## Documentation

- `docs/PROJECT_ARCHITECTURE.md` — architecture overview
- `docs/CURRICULUM_MASTER_PLAN.md` — curriculum plan
- `docs/DATABASE_SCHEMA.md` — data model
- `docs/IMPLEMENTATION_ROADMAP.md` — roadmap
- `docs/CONTENT_SOURCES_AND_REFERENCES.md` — content sources/references
- `docs/project-status/` — audits, compliance matrix, task tracker, status, and development log
- `tools/CONTENT_SCHEMA.md` — content asset schema

## Notes

- **Offline-first**: core functionality and packaged content work without network access. `INTERNET` permission is declared for optional/explicit features such as remote login validation and optional external mentor integration.
- **Security**: do not commit secrets, signing keys, or `local.properties`. Store server credentials only in server-side configuration. Prefer hashed passwords and least-privilege DB users in production deployments.
- **Content policy**: original explanations with attributed references; no copyrighted/leaked exam questions or paid standard text.

## License

No license file is included yet. Add a `LICENSE` if/when distribution terms are defined.
