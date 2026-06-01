# TestMarkaz — Android

TestMarkaz is an Android app for Uzbek students (grades 9–11) to practice test questions across subjects such as Mathematics, Physics, Chemistry, Biology, History, and more.

## Tech Stack

- **Language**: Kotlin
- **UI**: Jetpack Compose + Material 3
- **DI**: Hilt
- **Database**: Room (SQLite via raw `SQLiteDatabase` for question packs)
- **Navigation**: Navigation Compose
- **Async**: Kotlin Coroutines + Flow
- **Network**: OkHttp (Google Drive downloads)

## Project Structure

```
android/
├── app/                        # Application entry point, navigation, DI wiring
│   └── src/main/assets/
│       ├── catalog.db          # Content pack metadata (SQLite)
│       └── packs/*.db          # 30 bundled question pack files (SQLite)
├── core/
│   ├── data/                   # Room DB, DAOs, entities, repositories, DI modules
│   ├── domain/                 # Domain models (TestSession, TestConfig, Subject…)
│   └── ui/                     # Shared theme, colors, typography
└── feature/
    ├── home/                   # HomeScreen, HomeViewModel
    ├── test/                   # TestConfig, TestSession, Results screens + PausedSessionRepository
    ├── downloads/              # DownloadsScreen, DownloadsViewModel, PackDownloadRepository
    ├── progress/               # ProgressScreen
    └── profile/                # ProfileScreen
```

## Key Architecture Rules

- **`core/domain` depends on `core/data`** — so `core/data` must NOT import domain models. Any logic that bridges DAOs and domain models (e.g. `PausedSessionRepository`) lives in the relevant `feature` module.
- `SessionHolder` / `ResultHolder` — singleton in-memory maps used to pass `TestSession` / `TestResult` between nav destinations without serialization overhead.

## Database

Room DB (`AppDatabase`) lives in `core/data`. Current version: **2** (uses `fallbackToDestructiveMigration`).

Tables:
| Table | Entity | Purpose |
|---|---|---|
| `questions` | `QuestionEntity` | All question content |
| `test_sessions` | `TestSessionEntity` | Completed test history |
| `user_stats` | `UserStatsEntity` | XP, streaks, totals |
| `installed_packs` | `InstalledPackEntity` | Which packs are downloaded |
| `paused_sessions` | `PausedSessionEntity` | In-progress (paused) tests |

## Question Packs

- **Catalog**: `assets/catalog.db` — metadata for all available packs (subject, grade, description, etc.)
- **Packs**: `assets/packs/<subject>_<grade>.db` — 30 SQLite files bundled offline; can also be downloaded from Google Drive if `driveFileId` is set.
- Download flow: `PackDownloadRepository` tries Google Drive first; falls back to copying from assets if `driveFileId` is blank.

## Navigation Routes

```
home → test_config → test_session/{sessionId} → results/{sessionId}
home → downloads
home → progress
home → profile
```

## Build & Run

```bash
cd android
./gradlew assembleDebug          # build APK
./gradlew :app:installDebug      # install on connected device
./gradlew :feature:home:compileDebugKotlin   # compile single module
```

## Common Patterns

### ViewModel state
```kotlin
val uiState = combine(flow1, flow2) { a, b -> MyUiState(a, b) }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MyUiState())
```

### Pausing / Resuming a test
- **Pause**: `PausedSessionRepository.save(session, currentIndex)` — serializes question IDs + answers to `paused_sessions` table.
- **Resume**: `PausedSessionRepository.restoreById(sessionId)` — re-fetches questions from Room, reconstructs `TestSession`.
- Home screen shows one resume banner per paused session via `PausedSessionDao.observeAll()`.
