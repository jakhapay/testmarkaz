# TestMarkaz — Database Schema

> Two distinct data layers: **Server-side PostgreSQL** (user accounts, pack catalog, progress backup) and **On-device SQLite via Drift** (questions, test sessions, progress — the primary data store). The device DB works fully offline; the server DB is the backup and content delivery layer.

---

## Part A — Server-Side PostgreSQL 16

Standard conventions: `bigserial` PKs, `created_at timestamptz DEFAULT now()`, `updated_at timestamptz`, soft delete via `deleted_at timestamptz NULL`.

---

### 1. Users & Auth

```sql
CREATE TABLE users (
  id            bigserial PRIMARY KEY,
  public_id     uuid NOT NULL DEFAULT gen_random_uuid() UNIQUE,
  phone         text UNIQUE,                      -- E.164, primary in UZ
  email         text UNIQUE,
  password_hash text,
  full_name     text NOT NULL,
  avatar_url    text,
  locale        text NOT NULL DEFAULT 'uz-Latn',  -- uz-Latn|uz-Cyrl|ru|en
  role          text NOT NULL DEFAULT 'student',  -- student|admin
  grade         smallint,                         -- 1..11 for school students
  region        text,
  is_premium    boolean NOT NULL DEFAULT false,
  premium_until timestamptz,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now(),
  deleted_at    timestamptz
);

CREATE TABLE user_devices (
  id          bigserial PRIMARY KEY,
  user_id     bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  device_uid  text NOT NULL UNIQUE,               -- client-generated UUID
  platform    text NOT NULL,                      -- android|ios
  app_version text,
  last_seen   timestamptz NOT NULL DEFAULT now(),
  created_at  timestamptz NOT NULL DEFAULT now()
);
```

---

### 2. Content Pack Catalog

```sql
-- Master subject registry
CREATE TABLE subjects (
  id           bigserial PRIMARY KEY,
  code         text NOT NULL UNIQUE,   -- 'matematika', 'fizika', etc.
  name_uz      text NOT NULL,
  name_ru      text,
  name_en      text,
  icon         text,                   -- emoji or icon key
  grade_from   smallint NOT NULL,      -- minimum grade this subject appears
  grade_to     smallint NOT NULL,      -- maximum grade
  is_dtm       boolean DEFAULT false,  -- included in DTM exam set
  sort_order   smallint DEFAULT 0
);

-- Content packs — one row per subject+grade combination
CREATE TABLE content_packs (
  id              bigserial PRIMARY KEY,
  pack_key        text NOT NULL UNIQUE,  -- 'matematika_09', 'fizika_10', etc.
  subject_code    text NOT NULL REFERENCES subjects(code),
  grade           smallint,              -- NULL = DTM/cross-grade bundle
  grade_min       smallint,              -- for range bundles
  grade_max       smallint,              -- for range bundles
  version         integer NOT NULL DEFAULT 1,
  question_count  integer NOT NULL DEFAULT 0,
  size_bytes      bigint,
  r2_key          text NOT NULL,         -- path in R2 bucket
  checksum_sha256 text NOT NULL,         -- SHA256 of .db file
  lang            text NOT NULL DEFAULT 'uz-Latn',
  is_published    boolean NOT NULL DEFAULT false,
  published_at    timestamptz,
  created_at      timestamptz NOT NULL DEFAULT now(),
  updated_at      timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_packs_subject_grade ON content_packs(subject_code, grade);

-- Log of pack downloads per user (for analytics + premium gating)
CREATE TABLE pack_downloads (
  id          bigserial PRIMARY KEY,
  user_id     bigint REFERENCES users(id) ON DELETE SET NULL,
  device_uid  text NOT NULL,
  pack_id     bigint NOT NULL REFERENCES content_packs(id),
  pack_version integer NOT NULL,
  downloaded_at timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_downloads_user ON pack_downloads(user_id);
```

---

### 3. Server-Side Progress Backup

```sql
-- Synced test sessions from devices
CREATE TABLE test_sessions (
  id               bigserial PRIMARY KEY,
  local_id         uuid NOT NULL UNIQUE,  -- device-generated ID
  user_id          bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  device_uid       text NOT NULL,
  subject_code     text,                  -- NULL = multi-subject random
  grade_min        smallint NOT NULL,
  grade_max        smallint NOT NULL,
  mode             text NOT NULL,         -- subject|range|random_class|full_random|dtm
  score            smallint NOT NULL,     -- correct answers out of 25
  total            smallint NOT NULL DEFAULT 25,
  duration_seconds integer,
  started_at       timestamptz NOT NULL,
  completed_at     timestamptz,
  synced_at        timestamptz NOT NULL DEFAULT now()
);
CREATE INDEX idx_sessions_user_started ON test_sessions(user_id, started_at DESC);

-- Individual answers within a synced session
CREATE TABLE session_answers (
  id                bigserial PRIMARY KEY,
  session_id        bigint NOT NULL REFERENCES test_sessions(id) ON DELETE CASCADE,
  question_checksum text NOT NULL,    -- SHA256 — links across pack versions
  selected_option   text NOT NULL,    -- 'A'|'B'|'C'|'D'
  correct_option    text NOT NULL,
  is_correct        boolean NOT NULL,
  answered_at       timestamptz
);
CREATE INDEX idx_answers_session ON session_answers(session_id);
CREATE INDEX idx_answers_checksum ON session_answers(question_checksum);
```

---

### 4. Subscriptions & Billing

```sql
CREATE TABLE subscriptions (
  id            bigserial PRIMARY KEY,
  user_id       bigint NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  plan          text NOT NULL DEFAULT 'free',  -- free|premium|premium_yearly
  status        text NOT NULL DEFAULT 'active', -- active|expired|cancelled
  started_at    timestamptz NOT NULL DEFAULT now(),
  expires_at    timestamptz,
  payment_provider text,                        -- click|payme|stripe
  provider_subscription_id text,
  created_at    timestamptz NOT NULL DEFAULT now(),
  updated_at    timestamptz NOT NULL DEFAULT now()
);

CREATE TABLE payments (
  id               bigserial PRIMARY KEY,
  user_id          bigint NOT NULL REFERENCES users(id),
  subscription_id  bigint REFERENCES subscriptions(id),
  provider         text NOT NULL,
  provider_tx_id   text NOT NULL UNIQUE,
  amount_uzs       integer,
  amount_usd_cents integer,
  status           text NOT NULL,  -- pending|paid|failed|refunded
  paid_at          timestamptz,
  created_at       timestamptz NOT NULL DEFAULT now()
);
```

---

## Part B — On-Device SQLite (Drift)

This is the **primary data store**. Every feature reads from here first. The Drift schema mirrors a subset of the server schema plus device-only tables.

---

### 5. Questions (merged from downloaded packs)

```dart
// Drift table definition
class Questions extends Table {
  IntColumn get id          => integer().autoIncrement()();
  TextColumn get subject    => text()();
  IntColumn  get grade      => integer()();          // 1..11; 0 = DTM
  TextColumn get topic      => text().nullable()();
  TextColumn get questionText => text()();
  TextColumn get optionA    => text()();
  TextColumn get optionB    => text()();
  TextColumn get optionC    => text()();
  TextColumn get optionD    => text()();
  TextColumn get correct    => text()();             // 'A'|'B'|'C'|'D'
  TextColumn get explanation => text().nullable()();
  IntColumn  get difficulty  => integer().withDefault(const Constant(2))();
  TextColumn get sourceBook  => text().nullable()();
  IntColumn  get sourcePage  => integer().nullable()();
  TextColumn get lang        => text().withDefault(const Constant('uz-Latn'))();
  TextColumn get checksum    => text().unique()();   // dedup key
  TextColumn get packKey     => text()();            // which pack this came from
  DateTimeColumn get importedAt => dateTime().withDefault(currentDateAndTime)();
}
```

SQL indexes on device:
```sql
CREATE INDEX idx_q_subject_grade ON questions(subject, grade);
CREATE INDEX idx_q_topic ON questions(topic);
CREATE INDEX idx_q_difficulty ON questions(difficulty);
CREATE INDEX idx_q_checksum ON questions(checksum);
```

---

### 6. Installed Packs

```dart
class InstalledPacks extends Table {
  TextColumn get packKey      => text()();            // primary key
  IntColumn  get version      => integer()();
  TextColumn get subjectCode  => text()();
  IntColumn  get grade        => integer().nullable()();
  IntColumn  get gradeMin     => integer().nullable()();
  IntColumn  get gradeMax     => integer().nullable()();
  IntColumn  get questionCount => integer()();
  DateTimeColumn get installedAt => dateTime().withDefault(currentDateAndTime)();
  DateTimeColumn get updatedAt   => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {packKey};
}
```

---

### 7. Test Sessions (device-local)

```dart
class TestSessions extends Table {
  TextColumn  get id          => text()();            // UUID, device-generated
  TextColumn  get subjectCode => text().nullable()(); // null = multi-subject
  IntColumn   get gradeMin    => integer()();
  IntColumn   get gradeMax    => integer()();
  TextColumn  get mode        => text()();            // subject|range|random_class|full_random|dtm
  IntColumn   get score       => integer()();
  IntColumn   get total       => integer().withDefault(const Constant(25))();
  IntColumn   get durationSeconds => integer().nullable()();
  DateTimeColumn get startedAt    => dateTime()();
  DateTimeColumn get completedAt  => dateTime().nullable()();
  BoolColumn  get syncedToServer  => bool().withDefault(const Constant(false))();

  @override
  Set<Column> get primaryKey => {id};
}

class SessionAnswers extends Table {
  IntColumn  get id               => integer().autoIncrement()();
  TextColumn get sessionId        => text().references(TestSessions, #id)();
  TextColumn get questionChecksum => text()();
  TextColumn get questionText     => text()();        // snapshot at time of test
  TextColumn get selectedOption   => text()();
  TextColumn get correctOption    => text()();
  BoolColumn get isCorrect        => bool()();
  TextColumn get explanation      => text().nullable()();
  DateTimeColumn get answeredAt   => dateTime().nullable()();
}
```

---

### 8. User Progress (device-local aggregates)

```dart
// Topic-level mastery, updated after every session
class TopicMastery extends Table {
  IntColumn  get id           => integer().autoIncrement()();
  TextColumn get subjectCode  => text()();
  IntColumn  get grade        => integer()();
  TextColumn get topic        => text()();
  IntColumn  get totalAnswered => integer().withDefault(const Constant(0))();
  IntColumn  get correctCount  => integer().withDefault(const Constant(0))();
  IntColumn  get eloRating     => integer().withDefault(const Constant(1500))();
  DateTimeColumn get lastPracticed => dateTime().nullable()();
  DateTimeColumn get updatedAt     => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {subjectCode, grade, topic};
}

// Daily streak tracking
class StreakData extends Table {
  IntColumn  get id           => integer().autoIncrement()();
  DateTimeColumn get day      => dateTime()();        // date only (no time)
  IntColumn  get testsCompleted => integer().withDefault(const Constant(0))();
  IntColumn  get xpEarned     => integer().withDefault(const Constant(0))();
  BoolColumn get streakKept   => bool().withDefault(const Constant(false))();
}

// Cumulative XP and gamification
class UserStats extends Table {
  IntColumn get id          => integer().withDefault(const Constant(1))();
  IntColumn get totalXp     => integer().withDefault(const Constant(0))();
  IntColumn get currentStreak => integer().withDefault(const Constant(0))();
  IntColumn get longestStreak => integer().withDefault(const Constant(0))();
  IntColumn get totalTests   => integer().withDefault(const Constant(0))();
  IntColumn get totalCorrect => integer().withDefault(const Constant(0))();
  DateTimeColumn get lastTestAt => dateTime().nullable()();
  DateTimeColumn get updatedAt  => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {id};
}
```

---

### 9. Bookmarks & Wrong Answers (device-local)

```dart
// Questions the user bookmarked for review
class Bookmarks extends Table {
  IntColumn  get id               => integer().autoIncrement()();
  TextColumn get questionChecksum => text().unique()();
  TextColumn get questionText     => text()();
  TextColumn get subject          => text()();
  IntColumn  get grade            => integer()();
  DateTimeColumn get bookmarkedAt => dateTime().withDefault(currentDateAndTime)();
}

// Wrong answers aggregated across all sessions — for targeted practice
class WrongAnswers extends Table {
  TextColumn get questionChecksum => text()();
  TextColumn get subject          => text()();
  IntColumn  get grade            => integer()();
  IntColumn  get wrongCount       => integer().withDefault(const Constant(1))();
  DateTimeColumn get lastWrongAt  => dateTime().withDefault(currentDateAndTime)();

  @override
  Set<Column> get primaryKey => {questionChecksum};
}
```

---

## Part C — Subject Taxonomy

All subjects available in the platform, covering grades 1–11 of the Uzbek national curriculum:

```sql
INSERT INTO subjects (code, name_uz, grade_from, grade_to, is_dtm, sort_order) VALUES
-- Core DTM subjects
('matematika',          'Matematika',                      1, 11, true,  1),
('fizika',              'Fizika',                          7, 11, true,  2),
('kimyo',               'Kimyo',                           8, 11, true,  3),
('biologiya',           'Biologiya',                       6, 11, true,  4),
('uzbek_tili',          'O''zbek tili va adabiyoti',       1, 11, true,  5),
('ingliz_tili',         'Ingliz tili',                     2, 11, true,  6),
('tarix',               'Tarix',                           5, 11, true,  7),
('geografiya',          'Geografiya',                      6, 11, true,  8),
-- Additional school subjects
('rus_tili',            'Rus tili',                        1, 11, false, 9),
('informatika',         'Informatika',                     5, 11, false, 10),
('fuqarolik',          'Fuqarolik ta''limi',               5, 11, false, 11),
('texnologiya',         'Texnologiya',                     1,  9, false, 12),
('tasviriy_sanat',      'Tasviriy san''at',                1,  7, false, 13),
('musiqa',              'Musiqa',                          1,  7, false, 14),
('jismoniy_tarbiya',    'Jismoniy tarbiya',                1, 11, false, 15),
('tarbiya',             'Tarbiya',                         1,  4, false, 16),
-- Special
('dtm_full',            'DTM To''liq tayyorlov',           0,  0, true,  0);
```

---

## Part D — Key Queries

### Generate a 25-question test (subject + grade range)
```sql
SELECT * FROM questions
WHERE subject = 'matematika'
  AND grade BETWEEN 7 AND 9
ORDER BY RANDOM()
LIMIT 25;
```

### Generate a random-class test (all subjects, one grade)
```sql
SELECT * FROM questions
WHERE grade = 9
ORDER BY RANDOM()
LIMIT 25;
```

### Stratified by difficulty (production query)
```sql
-- 10 easy + 10 medium + 5 hard
(SELECT * FROM questions WHERE subject='fizika' AND grade=10 AND difficulty=1 ORDER BY RANDOM() LIMIT 10)
UNION ALL
(SELECT * FROM questions WHERE subject='fizika' AND grade=10 AND difficulty=2 ORDER BY RANDOM() LIMIT 10)
UNION ALL
(SELECT * FROM questions WHERE subject='fizika' AND grade=10 AND difficulty=3 ORDER BY RANDOM() LIMIT 5)
ORDER BY RANDOM();
```

### Weak topics for a user
```sql
SELECT subject_code, grade, topic,
       ROUND(correct_count * 100.0 / total_answered) AS accuracy_pct,
       elo_rating
FROM topic_mastery
WHERE total_answered >= 5
ORDER BY accuracy_pct ASC
LIMIT 10;
```
