# TestMarkaz — Production Folder Structures

> Two repos. The Android app is the core product; the API is a thin sync layer.
>
> - `testmarkaz/android` — Native Kotlin Android app (Jetpack Compose + Room + Hilt)
> - `testmarkaz/api` — Kotlin Ktor sync API (auth, pack catalog, progress sync)

---

## 1. Android App — `testmarkaz/android`

```
android/
├── build.gradle.kts                     # root build — versions + plugins
├── settings.gradle.kts                  # module declarations
├── gradle/
│   └── libs.versions.toml               # version catalog (Compose, Room, Hilt, etc.)
├── gradle.properties
│
└── app/
    ├── build.gradle.kts                 # app module — deps, signing, flavors
    ├── proguard-rules.pro
    └── src/
        ├── main/
        │   ├── AndroidManifest.xml
        │   └── kotlin/uz/testmarkaz/
        │       │
        │       ├── TestMarkazApp.kt         # Application class, Hilt entry point
        │       ├── MainActivity.kt          # single Activity, hosts NavHost
        │       │
        │       ├── di/
        │       │   ├── AppModule.kt         # Hilt: DB, Repos, UseCases
        │       │   └── NetworkModule.kt     # Hilt: Retrofit, OkHttp
        │       │
        │       ├── data/
        │       │   │
        │       │   ├── db/                  # Room — local SQLite
        │       │   │   ├── AppDatabase.kt   # @Database, all entities, migrations
        │       │   │   ├── entity/
        │       │   │   │   ├── QuestionEntity.kt
        │       │   │   │   ├── InstalledPackEntity.kt
        │       │   │   │   ├── TestSessionEntity.kt
        │       │   │   │   ├── SessionAnswerEntity.kt
        │       │   │   │   ├── TopicMasteryEntity.kt
        │       │   │   │   ├── UserStatsEntity.kt
        │       │   │   │   └── WrongAnswerEntity.kt
        │       │   │   └── dao/
        │       │   │       ├── QuestionDao.kt       # random 25 query, subject+grade filter
        │       │   │       ├── TestSessionDao.kt    # insert session + answers
        │       │   │       ├── ProgressDao.kt       # topic mastery, Elo update
        │       │   │       └── StatsDao.kt          # streak, XP, totals
        │       │   │
        │       │   ├── mock/
        │       │   │   └── MockDataSeeder.kt        # seeds Room DB with sample questions
        │       │   │                                # (replaces Python pipeline for dev)
        │       │   │
        │       │   ├── remote/                      # API client (online-only)
        │       │   │   ├── ApiService.kt            # Retrofit interface
        │       │   │   ├── dto/
        │       │   │   │   ├── PackCatalogDto.kt
        │       │   │   │   └── SyncPayloadDto.kt
        │       │   │   └── PackDownloader.kt        # OkHttp resumable download
        │       │   │
        │       │   └── repository/
        │       │       ├── QuestionRepository.kt    # local DB reads for test gen
        │       │       ├── PackRepository.kt        # catalog + download + import
        │       │       ├── ProgressRepository.kt    # mastery, stats, streaks
        │       │       └── SyncRepository.kt        # push sessions to server
        │       │
        │       ├── domain/
        │       │   ├── model/
        │       │   │   ├── Subject.kt               # enum: MATEMATIKA, FIZIKA, ...
        │       │   │   ├── TestMode.kt              # enum: SUBJECT, RANGE, RANDOM_CLASS, FULL_RANDOM
        │       │   │   ├── TestConfig.kt            # subjects, gradeMin, gradeMax, mode
        │       │   │   ├── TestQuestion.kt          # question + options + correct + explanation
        │       │   │   ├── TestSession.kt           # 25 questions + user answers
        │       │   │   └── TestResult.kt            # score, topic breakdown, wrong answers
        │       │   │
        │       │   └── usecase/
        │       │       ├── GenerateTestUseCase.kt   # core: random 25q, stratified difficulty
        │       │       ├── SubmitAnswerUseCase.kt   # record answer, update mastery
        │       │       ├── CompleteTestUseCase.kt   # finalize session, compute results
        │       │       └── GetWeakTopicsUseCase.kt  # topics with lowest accuracy
        │       │
        │       └── ui/
        │           ├── theme/
        │           │   ├── Theme.kt                 # MaterialTheme, dark/light
        │           │   ├── Color.kt                 # brand palette
        │           │   └── Type.kt                  # typography scale
        │           │
        │           ├── navigation/
        │           │   ├── AppNavigation.kt         # NavHost + all routes
        │           │   └── Routes.kt                # sealed class route definitions
        │           │
        │           ├── components/                  # shared Compose components
        │           │   ├── SubjectIcon.kt
        │           │   ├── GradeChip.kt
        │           │   ├── ScoreRing.kt
        │           │   ├── StreakPill.kt
        │           │   ├── OfflineBanner.kt
        │           │   ├── ProgressBar.kt
        │           │   └── EmptyState.kt
        │           │
        │           ├── home/
        │           │   ├── HomeScreen.kt
        │           │   └── HomeViewModel.kt
        │           │
        │           ├── testconfig/
        │           │   ├── TestConfigScreen.kt      # subject picker + grade/range/random
        │           │   ├── SubjectPickerGrid.kt
        │           │   ├── GradeRangePicker.kt
        │           │   └── TestConfigViewModel.kt
        │           │
        │           ├── testsession/
        │           │   ├── TestSessionScreen.kt     # 25 questions, progress bar
        │           │   ├── QuestionCard.kt
        │           │   ├── OptionItem.kt
        │           │   └── TestSessionViewModel.kt
        │           │
        │           ├── results/
        │           │   ├── ResultsScreen.kt
        │           │   ├── TopicBreakdownCard.kt
        │           │   ├── WrongAnswerItem.kt
        │           │   └── ResultsViewModel.kt
        │           │
        │           ├── downloads/
        │           │   ├── DownloadsScreen.kt
        │           │   ├── PackCard.kt
        │           │   └── DownloadsViewModel.kt
        │           │
        │           ├── progress/
        │           │   ├── ProgressScreen.kt
        │           │   ├── MasteryChart.kt
        │           │   └── ProgressViewModel.kt
        │           │
        │           └── profile/
        │               ├── ProfileScreen.kt
        │               └── ProfileViewModel.kt
        │
        ├── test/
        │   └── kotlin/uz/testmarkaz/
        │       ├── domain/
        │       │   └── GenerateTestUseCaseTest.kt   # 25q, stratified, no dupes
        │       ├── data/
        │       │   ├── QuestionDaoTest.kt           # Room in-memory DB tests
        │       │   └── MockDataSeederTest.kt
        │       └── ui/
        │           └── TestConfigViewModelTest.kt
        │
        └── androidTest/
            └── kotlin/uz/testmarkaz/
                └── OfflineTestFlowTest.kt           # full E2E: config→test→results
```

---

## 2. Sync API — `testmarkaz/api` (Kotlin Ktor)

```
api/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle/libs.versions.toml
│
├── platform/
│   └── src/main/kotlin/uz/testmarkaz/platform/
│       ├── config/
│       ├── db/                          # Exposed + Flyway
│       ├── redis/                       # rate limiting
│       ├── http/                        # Ktor extensions, error model
│       ├── auth/                        # JWT RS256
│       └── r2/                          # Cloudflare R2 client, signed URLs
│
├── modules/
│   ├── auth/                            # register, login, OTP
│   ├── packs/                           # catalog, download-url
│   ├── sync/                            # POST /sync/progress
│   ├── users/                           # profile, stats
│   ├── billing/                         # Click/Payme webhooks
│   └── admin/                           # pack management
│
└── app/
    └── src/main/kotlin/uz/testmarkaz/
        ├── Application.kt
        ├── Routing.kt
        └── Wiring.kt
```

---

## 3. Key Architecture Decisions

**Why no Python pipeline?** Mock data seeds the Room DB directly via `MockDataSeeder.kt`. When real books arrive, the founder uploads PDFs through an admin panel; the Ktor API handles AI generation server-side (Kotlin coroutines + OpenAI API) and pushes the resulting `.db` pack to R2.

**Why Room over Drift?** Room is the Android-standard SQLite ORM with first-class Hilt + ViewModel + Flow integration. No cross-platform overhead.

**Why Hilt over manual DI?** Hilt generates all boilerplate, handles scoped lifecycles correctly with ViewModels, and is the Google-recommended standard for Android DI.
