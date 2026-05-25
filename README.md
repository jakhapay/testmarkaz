# TestMarkaz 📚

**An offline test platform for Uzbekistan school students and university entrance exam (DTM) applicants**

---

## About the Project

TestMarkaz works without an internet connection. Questions are downloaded to the device and the app runs fully offline. Each test consists of exactly **25 questions**.

## Key Features

- 📶 **Offline-first** — works fully without internet
- 📝 **25 questions** — always exactly this number per test
- 🎯 **4 test modes** — by subject, grade range, random grade, fully random
- 🏫 **16 subjects** — all school subjects (grades 1–11)
- 🏆 **DTM preparation** — grade 9–11 packages
- 📊 **Progress tracking** — XP, streaks, weak topic detection

## Tech Stack

- **Kotlin** + Jetpack Compose + Material 3
- **Room 2.6.1** (offline SQLite)
- **Hilt** (dependency injection)
- **Navigation Compose**
- **Coroutines + Flow**

## Project Structure

```
TestMarkaz/
├── android/          # Native Kotlin Android app
│   └── app/src/main/kotlin/uz/testmarkaz/
│       ├── data/     # Room DB, entities, DAOs, MockDataSeeder
│       ├── domain/   # Models, UseCases
│       ├── di/       # Hilt modules
│       └── ui/       # Compose screens + ViewModels
└── docs/             # Architecture, API, UI/UX, roadmap docs
```

## Getting Started

1. Open the `android/` folder in Android Studio
2. Run `./gradlew assembleDebug` or press the Run button in the IDE
3. On first launch, **150+ mock questions** are seeded automatically

## Mock Data

Currently, 150+ Uzbek-language questions are seeded via `MockDataSeeder.kt`:
- Mathematics (grades 9, 10, 11)
- Physics, Chemistry, Biology (grade 9)
- English, Uzbek Language, History, Geography (grade 9)

Once real question books are available, `MockDataSeeder.kt` will be replaced with actual content.

## Documentation

All architecture, API, UI/UX, and roadmap docs are available in the `docs/` folder.

---

*TestMarkaz MVP v1.0 — 2024*
