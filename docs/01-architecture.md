# TestMarkaz — System Architecture

> Offline-first test preparation platform for Uzbek school students (grades 1–11) and DTM abituriyents. Every feature works without internet after the initial content download.

---

## 1. Core Product Concept

TestMarkaz is fundamentally an **offline-first exam practice app**. Every feature must work without an internet connection after initial setup. The server exists only for three things: content delivery (downloading question packs), account sync (progress backup), and AI enrichment (generating new questions from books the founder uploads). A student on a bus with no signal must be able to take a full 25-question test without any degradation.

### Test format (fixed, non-negotiable)
- Every test = **exactly 25 questions**
- Questions drawn randomly from the local SQLite database
- Student selects scope: subject + grade (e.g. "Matematika, 9-sinf") OR grade range (e.g. "Matematika, 7–9-sinf") OR fully random (any subject, any grade)
- No time limit by default; optional countdown for exam simulation mode

---

## 2. Architectural Horizons

| Horizon | Model | Trigger to evolve |
|---|---|---|
| Phase 1 — MVP | Offline-first Flutter app + lightweight Ktor sync API | 10k MAU or $5k MRR |
| Phase 2 — Growth | Expand AI pipeline, teacher tools, payments | 50k MAU |
| Phase 3 — Platform | Real-time features, multi-region, B2B | 500k MAU |

Everything below is Phase 1 first.

---

## 3. High-Level Architecture

```
┌──────────────────────────────────────────────────────────────────┐
│                   FLUTTER APP (iOS + Android)                    │
│                                                                  │
│  ┌──────────────────────────────────────────────────────────┐   │
│  │               LOCAL SQLITE (Drift)                       │   │
│  │  questions · subjects · grades · sessions · progress     │   │
│  └───────────────────────┬──────────────────────────────────┘   │
│                          │  read/write (always available)        │
│  ┌───────────────────────▼──────────────────────────────────┐   │
│  │                  CORE MODULES                            │   │
│  │  TestEngine · SubjectBrowser · ProgressTracker · Sync   │   │
│  └───────────────────────┬──────────────────────────────────┘   │
│                          │  optional — only when online         │
│  ┌───────────────────────▼──────────────────────────────────┐   │
│  │           SYNC / DOWNLOAD LAYER                          │   │
│  │   ContentPackManager · SyncService · AuthService         │   │
│  └───────────────────────┬──────────────────────────────────┘   │
└─────────────────────────┬┘                                       │
                          │ HTTPS (when online)
          ┌───────────────▼───────────────────────────┐
          │           CLOUDFLARE CDN                  │
          │   .db pack files served from R2 edge      │
          └───────────────┬───────────────────────────┘
                          │
          ┌───────────────▼───────────────────────────┐
          │         Ktor Sync API (lightweight)        │
          │  auth · progress-sync · pack-catalog      │
          └───┬───────────────────────────────────────┘
              │
   ┌──────────┴──────────────┬─────────────────────────┐
   │ Supabase Postgres       │ Cloudflare R2            │  AI Worker (async)
   │ (users, sessions,       │ (.db pack files)         │  book→OCR→chunks→
   │  pack catalog)          │                          │  questions→pack export
   └─────────────────────────┴──────────────────────────┘
```

---

## 4. The Content Pack System

Content packs are the backbone of the offline experience. A **content pack** is a single pre-built SQLite `.db` file containing all questions for a specific subject + grade combination. The app downloads these packs and merges them into the local database.

### Pack naming convention
```
{subject_code}_{grade}.db

Examples:
  matematika_09.db        → Math, grade 9
  fizika_10.db            → Physics, grade 10
  ingliz_tili_07.db       → English, grade 7
  dtm_matematika.db       → DTM-level Math (cross-grade)
  bundle_biologiya.db     → Biology all grades (1–11)
```

### Pack contents (SQLite schema inside each .db file)
```sql
CREATE TABLE questions (
  id            INTEGER PRIMARY KEY,
  subject       TEXT NOT NULL,
  grade         INTEGER NOT NULL,   -- 1..11; 0 = DTM
  topic         TEXT,
  question_text TEXT NOT NULL,
  option_a      TEXT NOT NULL,
  option_b      TEXT NOT NULL,
  option_c      TEXT NOT NULL,
  option_d      TEXT NOT NULL,
  correct       TEXT NOT NULL,      -- 'A'|'B'|'C'|'D'
  explanation   TEXT,
  difficulty    INTEGER DEFAULT 2,  -- 1=easy 2=medium 3=hard
  source_book   TEXT,
  source_page   INTEGER,
  lang          TEXT DEFAULT 'uz-Latn',
  checksum      TEXT                -- SHA256(question_text+correct)
);
CREATE INDEX idx_subject_grade ON questions(subject, grade);
CREATE INDEX idx_topic ON questions(topic);
```

### Pack versioning
Each pack has a `version` integer on the server. The app checks the pack catalog on every launch (when online) and downloads only updated packs in the background — non-blocking, user never waits.

### Pack sizes (estimates)
| Subject | Per grade | Full bundle |
|---|---|---|
| Matematika | ~1.2 MB | ~11 MB |
| Fizika | ~0.9 MB | ~7 MB |
| Kimyo | ~0.8 MB | ~6 MB |
| Biologiya | ~0.8 MB | ~7 MB |
| O'zbek tili | ~1.0 MB | ~9 MB |
| Ingliz tili | ~1.1 MB | ~10 MB |
| Tarix | ~0.7 MB | ~6 MB |
| **Total all subjects** | — | **~80 MB** |

80 MB for the complete library — comfortably fits on any device. A student downloading only their 3 exam subjects needs ~3–4 MB.

---

## 5. Test Engine (On-Device, 100% Offline)

The test engine lives entirely in Flutter/Dart. It never touches the network during test execution.

### Test generation algorithm
```dart
TestSession generateTest(TestConfig config) {
  // config.subjects  — e.g. ['matematika'] or [] for all
  // config.gradeMin  — e.g. 7
  // config.gradeMax  — e.g. 9  (set equal to gradeMin for single grade)
  // config.mode      — TestMode.subject | .range | .randomClass | .fullRandom
  // config.count     — always 25 (fixed)

  final pool = localDb.questions
    .where(subjectIn(config.subjects))
    .where(gradeBetween(config.gradeMin, config.gradeMax))
    .toList();

  // Stratified shuffle: 40% easy, 40% medium, 20% hard
  // Falls back to pure random if pool < 50 questions
  final questions = stratifiedSample(pool, 25);
  return TestSession(id: uuid(), questions: questions, startedAt: DateTime.now());
}
```

### Test modes
| Mode | Description |
|---|---|
| Subject + class | One subject, one grade — e.g. Fizika 9-sinf |
| Subject + range | One subject, grade range — e.g. Fizika 7–9-sinf |
| Random class | All subjects, single grade — e.g. everything for grade 8 |
| Full random | Any subject, any grade from downloaded packs |
| DTM prep | DTM-focused subjects and difficulty only |

---

## 6. Offline-First Data Flow

```
App opens
    │
    ├─► Load local Drift DB instantly ──────────────► Show home screen
    │
    └─► Background: check network (non-blocking)
              │
              ├── Offline ──► Show subtle "offline" chip, no action needed
              │
              └── Online
                    ├── Fetch pack catalog, queue any updates
                    ├── Sync local progress sessions to server
                    └── Pull any new notifications
```

**The app never blocks or shows a loading spinner due to network.** Network calls are always background and advisory.

### Full offline capabilities (zero degradation)
- Take any 25-question test from downloaded packs
- View all previous test results and explanations
- Browse subject/grade catalog for downloaded content
- Track streaks, XP, and progress locally
- Review wrong answers and explanations

### Online-only features
- Download new/updated content packs
- Sync progress to cloud backup
- Account registration and login (first time)

---

## 7. Tech Stack

### Mobile (Android — Kotlin native)
- **Kotlin 2.0 + Jetpack Compose** — modern declarative Android UI
- **Hilt** — dependency injection
- **Room 2** — type-safe SQLite ORM, the local data layer
- **ViewModel + StateFlow** — state management (MVVM)
- **Coroutines + Flow** — async and reactive streams
- **OkHttp + Retrofit** — HTTP with download resume (for pack downloads)
- **Navigation Compose** — type-safe in-app navigation
- **Vico / MPAndroidChart** — progress charts
- **DataStore** — settings, streak data, preferences

### Sync API (lightweight, Kotlin)
- **Ktor 3 + Kotlin 2** — minimal REST API: auth, sync, pack catalog
- **Supabase Postgres** — user accounts, pack metadata, progress backup
- **Redis (Upstash)** — rate limits, short-lived download tokens
- **Cloudflare R2** — `.db` pack files, delivered via CDN edge

### AI pipeline (admin-only, async Python)
- **OpenAI gpt-4o-mini** — question generation from book text
- **Tesseract 5** — OCR (uz/ru/en trained models)
- **Python + sqlite3** — pack builder script: chunks → questions → `.db` export
- Output: `.db` file → uploaded to R2 → version bumped in Postgres catalog

### Infrastructure
- **Hetzner CX32 VPS** — Ktor API + Python AI worker (~$6/month)
- **Cloudflare R2** — pack storage, zero egress cost
- **Supabase** — managed Postgres + Auth
- **Docker Compose** — single-VPS deployment

---

## 8. Progress Sync

Sync is best-effort and non-blocking. The local Drift DB is the source of truth; the server is a backup.

**Conflict resolution: local wins.** All test sessions are append-only. If a student takes 20 tests offline then reconnects, all 20 sessions push to server with their original timestamps. No merge conflict possible.

**Sync payload** sent to `POST /api/v1/sync/progress`:
```json
{
  "device_id": "uuid",
  "sessions": [
    {
      "local_id": "uuid",
      "subject": "matematika",
      "grade_min": 9, "grade_max": 9,
      "score": 18, "total": 25,
      "duration_seconds": 420,
      "started_at": "2026-05-12T08:30:00Z",
      "answers": [
        { "question_checksum": "abc123", "selected": "B", "correct": "C" }
      ]
    }
  ]
}
```

`question_checksum` (not integer ID) links answers to questions — survives pack version updates.

---

## 9. Security

- Content packs are public educational content — no DRM needed. Download URLs are signed tokens (1-hour expiry) to prevent hotlinking.
- Anonymous download: first 3 packs free, account required for more.
- Local Drift DB encrypted at rest via iOS Secure Enclave / Android Keystore.
- Server receives only question checksums, never question text.
- User can delete account and all server data in one tap; local DB stays on device until app uninstall.

---

## 10. Cost Scaling

The offline-first model keeps server costs very low because content is served from Cloudflare's edge, not the API:

| MAU | Infra | Monthly cost |
|---|---|---|
| 0–10k | Hetzner CX32 + Supabase free | ~$35 |
| 10k–100k | Hetzner CX42 + Supabase Pro | ~$80 |
| 100k–500k | 2× Hetzner CCX23 + dedicated Postgres | ~$250 |
| 500k+ | Extract AI worker, read replicas | ~$600 |

R2 has zero egress fees. 100,000 users downloading packs costs ~$1–2/month in storage operations only.
