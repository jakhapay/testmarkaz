# TestMarkaz 📚

**An offline test platform for Uzbekistan school students and university entrance exam (DTM) applicants**

---

## About

TestMarkaz lets students practice test questions fully offline. Question packs are bundled inside the app and can also be updated via Google Drive. Each test consists of exactly **25 questions**.

## Features

- 📶 **Offline-first** — all 30 question packs are bundled in the APK; no internet needed
- 📝 **25 questions per test** — consistent exam-style format
- 🎯 **4 test modes** — by subject, by grade range, random grade, fully random
- 🏫 **10 subjects** — Mathematics, Physics, Chemistry, Biology, History, Geography, Informatics, Uzbek Literature, Uzbek Language, English (grades 9–11)
- ⏸️ **Pause & resume** — exit mid-test and continue later; multiple paused tests supported
- 📊 **Progress tracking** — XP, streaks, per-session scores
- ☁️ **Pack updates** — download newer question packs from Google Drive when available

## Screenshots

> Coming soon

## Getting Started

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17+
- Android device or emulator (API 26+)

### Run

```bash
git clone https://github.com/jakhapay/testmarkaz.git
cd testmarkaz/android
./gradlew assembleDebug
# or open the android/ folder in Android Studio and press Run
```

No extra setup needed — question packs are bundled in `app/src/main/assets/`.

## Project Structure

```
testmarkaz/
├── android/
│   ├── app/                  # Entry point, navigation, Hilt app component
│   │   └── src/main/assets/
│   │       ├── catalog.db    # Pack metadata (subject, grade, description)
│   │       └── packs/*.db    # 30 SQLite question pack files
│   ├── core/
│   │   ├── data/             # Room DB, DAOs, entities, repositories, DI modules
│   │   ├── domain/           # Domain models (TestSession, TestConfig, Subject…)
│   │   └── ui/               # Shared theme, colors, typography
│   └── feature/
│       ├── home/             # Home screen with stats and resume banners
│       ├── test/             # Test config, test session, results
│       ├── downloads/        # Browse and download question packs
│       ├── progress/         # Test history and scores
│       └── profile/          # User profile
└── docs/                     # Architecture, UI/UX, roadmap docs
```

## Tech Stack

| Layer | Library |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| DI | Hilt 2.51.1 |
| Database | Room 2.6.1 |
| Navigation | Navigation Compose 2.7.7 |
| Async | Coroutines + Flow |
| Network | OkHttp 4.12.0 |

## Question Packs

Packs are plain SQLite files. The schema:

```sql
CREATE TABLE questions (
  id INTEGER PRIMARY KEY,
  subject TEXT, grade INTEGER, topic TEXT,
  question_text TEXT,
  option_a TEXT, option_b TEXT, option_c TEXT, option_d TEXT,
  correct TEXT,  -- "A" | "B" | "C" | "D"
  explanation TEXT, difficulty INTEGER
);
```

To add or update a pack, replace the `.db` file in `assets/packs/` or set its `driveFileId` in `catalog.db`.

---

*TestMarkaz — 2025*
