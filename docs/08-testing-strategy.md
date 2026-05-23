# TestMarkaz — Testing Strategy

> Offline-first app. Tests must cover the device data layer (Drift), the test engine (random 25-question generation), content pack import/merge, and sync. Network is optional — every critical path must pass in airplane mode.

---

## 1. Testing Philosophy

- Test behaviour, not implementation
- The test engine and local DB are critical — they must have near-100% coverage
- Network-dependent features are integration-tested against a local mock server
- E2E tests run in airplane mode for all core flows
- Fast: unit tests < 5s, widget tests < 30s, integration tests < 3 min

---

## 2. Testing Pyramid

```
                ┌──────────────────────────────┐
                │   E2E / Integration  (few)   │  ← Patrol (device)
                │   Full offline user journeys │
                └──────────────────────────────┘
              ┌──────────────────────────────────────┐
              │   Widget Tests  (moderate)           │  ← Flutter testWidgets
              │   Test config UI, results, downloads │
              └──────────────────────────────────────┘
         ┌──────────────────────────────────────────────────┐
         │         Unit Tests  (many)                       │  ← dart test
         │  TestEngine, Drift DAOs, pack import, sync logic │
         └──────────────────────────────────────────────────┘
```

---

## 3. Unit Tests — Flutter/Dart

Location: `app/test/unit/`

### 3.1 TestGenerator (most critical)

```dart
// test/unit/test_generator_test.dart
group('TestGenerator', () {
  late AppDatabase db;

  setUp(() async {
    db = AppDatabase(NativeDatabase.memory());
    // Seed 200 questions: math grade 9, mix of difficulties
    await seedQuestions(db, subject: 'matematika', grade: 9, count: 200);
  });

  test('always returns exactly 25 questions', () async {
    final config = TestConfig(
      subjects: ['matematika'], gradeMin: 9, gradeMax: 9,
      mode: TestMode.subject,
    );
    final session = await TestGenerator(db).generate(config);
    expect(session.questions.length, equals(25));
  });

  test('respects subject filter — no other subjects leak in', () async {
    await seedQuestions(db, subject: 'fizika', grade: 9, count: 50);
    final config = TestConfig(subjects: ['matematika'], gradeMin: 9, gradeMax: 9);
    final session = await TestGenerator(db).generate(config);
    expect(session.questions.every((q) => q.subject == 'matematika'), isTrue);
  });

  test('range mode includes questions from all grades in range', () async {
    await seedQuestions(db, subject: 'matematika', grade: 7, count: 100);
    await seedQuestions(db, subject: 'matematika', grade: 8, count: 100);
    final config = TestConfig(
      subjects: ['matematika'], gradeMin: 7, gradeMax: 9,
      mode: TestMode.range,
    );
    final session = await TestGenerator(db).generate(config);
    final grades = session.questions.map((q) => q.grade).toSet();
    expect(grades, containsAll([7, 8, 9]));
  });

  test('full random mode draws from all downloaded subjects', () async {
    await seedQuestions(db, subject: 'fizika', grade: 9, count: 100);
    await seedQuestions(db, subject: 'kimyo', grade: 9, count: 100);
    final config = TestConfig(
      subjects: [], gradeMin: 9, gradeMax: 9,
      mode: TestMode.randomClass,
    );
    final session = await TestGenerator(db).generate(config);
    final subjects = session.questions.map((q) => q.subject).toSet();
    expect(subjects.length, greaterThan(1));
  });

  test('stratified difficulty: ~40% easy, ~40% medium, ~20% hard', () async {
    // Seed with known difficulty distribution
    await seedQuestions(db, subject: 'matematika', grade: 9, count: 300,
      difficultyDistribution: {1: 100, 2: 100, 3: 100});
    final config = TestConfig(subjects: ['matematika'], gradeMin: 9, gradeMax: 9);
    final session = await TestGenerator(db).generate(config);
    final easy   = session.questions.where((q) => q.difficulty == 1).length;
    final medium = session.questions.where((q) => q.difficulty == 2).length;
    final hard   = session.questions.where((q) => q.difficulty == 3).length;
    // Allow ±3 tolerance for randomness
    expect(easy,   inInclusiveRange(7, 13));   // target 10
    expect(medium, inInclusiveRange(7, 13));   // target 10
    expect(hard,   inInclusiveRange(2, 8));    // target 5
  });

  test('throws InsufficientQuestionsException if pool < 25', () async {
    await seedQuestions(db, subject: 'matematika', grade: 1, count: 20);
    final config = TestConfig(subjects: ['matematika'], gradeMin: 1, gradeMax: 1);
    expect(() => TestGenerator(db).generate(config),
      throwsA(isA<InsufficientQuestionsException>()));
  });

  test('two consecutive tests never have the same question order', () async {
    final config = TestConfig(subjects: ['matematika'], gradeMin: 9, gradeMax: 9);
    final s1 = await TestGenerator(db).generate(config);
    final s2 = await TestGenerator(db).generate(config);
    final order1 = s1.questions.map((q) => q.checksum).toList();
    final order2 = s2.questions.map((q) => q.checksum).toList();
    expect(order1, isNot(equals(order2)));
  });
});
```

### 3.2 Pack Importer

```dart
group('PackImporter', () {
  test('imports questions from .db file into local Drift DB', () async {
    final packPath = 'test/fixtures/matematika_09_test.db';
    await PackImporter(db).import(packPath, packKey: 'matematika_09');
    final count = await db.questionsDao.countBySubjectGrade('matematika', 9);
    expect(count, greaterThan(0));
  });

  test('deduplicates by checksum — importing same pack twice is idempotent', () async {
    final packPath = 'test/fixtures/matematika_09_test.db';
    await PackImporter(db).import(packPath, packKey: 'matematika_09');
    final countAfterFirst = await db.questionsDao.countBySubjectGrade('matematika', 9);
    await PackImporter(db).import(packPath, packKey: 'matematika_09');
    final countAfterSecond = await db.questionsDao.countBySubjectGrade('matematika', 9);
    expect(countAfterFirst, equals(countAfterSecond));
  });

  test('verifies checksum — rejects corrupted .db file', () async {
    expect(
      () => PackImporter(db).importAndVerify('test/fixtures/corrupt.db',
            expectedChecksum: 'abc123'),
      throwsA(isA<PackChecksumMismatchException>()),
    );
  });

  test('registers pack in InstalledPacks table after import', () async {
    await PackImporter(db).import('test/fixtures/matematika_09_test.db',
      packKey: 'matematika_09', version: 3);
    final pack = await db.packsDao.get('matematika_09');
    expect(pack, isNotNull);
    expect(pack!.version, equals(3));
  });
});
```

### 3.3 Progress DAO — Elo + Streak

```dart
group('ProgressDAO', () {
  test('correct answer increases Elo rating', () async {
    await db.progressDao.upsertMastery('matematika', 9, 'Trigonometriya',
      totalAnswered: 10, correctCount: 5, eloRating: 1500);
    await db.progressDao.recordAnswer('matematika', 9, 'Trigonometriya',
      isCorrect: true);
    final mastery = await db.progressDao.getMastery('matematika', 9, 'Trigonometriya');
    expect(mastery!.eloRating, greaterThan(1500));
  });

  test('streak increments on consecutive days', () async {
    final today = DateTime.now();
    final yesterday = today.subtract(const Duration(days: 1));
    await db.statsDao.recordTestDay(yesterday);
    await db.statsDao.recordTestDay(today);
    final stats = await db.statsDao.get();
    expect(stats.currentStreak, equals(2));
  });

  test('streak resets to 1 after missed day', () async {
    final twoDaysAgo = DateTime.now().subtract(const Duration(days: 2));
    await db.statsDao.recordTestDay(twoDaysAgo);
    await db.statsDao.recordTestDay(DateTime.now());
    final stats = await db.statsDao.get();
    expect(stats.currentStreak, equals(1));
  });
});
```

### 3.4 Sync Service

```dart
group('SyncService', () {
  test('queues sessions with syncedToServer=false only', () async {
    await db.sessionsDao.insert(mockSession(synced: false));
    await db.sessionsDao.insert(mockSession(synced: true));
    final pending = await db.sessionsDao.getPendingSync();
    expect(pending.length, equals(1));
  });

  test('marks sessions as synced after successful push', () async {
    final session = await db.sessionsDao.insert(mockSession(synced: false));
    final mockApi = MockSyncApi(willSucceed: true);
    await SyncService(db, mockApi).sync();
    final updated = await db.sessionsDao.get(session.id);
    expect(updated!.syncedToServer, isTrue);
  });

  test('leaves sessions unsynced on network error — retries next time', () async {
    await db.sessionsDao.insert(mockSession(synced: false));
    final mockApi = MockSyncApi(willSucceed: false);
    await SyncService(db, mockApi).sync();  // should not throw
    final pending = await db.sessionsDao.getPendingSync();
    expect(pending.length, equals(1));      // still pending
  });
});
```

---

## 4. Widget Tests

Location: `app/test/widget/`

```dart
// Grade range picker
testWidgets('GradeRangePicker shows single grade chips', (tester) async {
  int? selected;
  await tester.pumpWidget(MaterialApp(
    home: GradeRangePicker(
      mode: GradePickerMode.single,
      onChanged: (min, max) => selected = min,
    ),
  ));
  await tester.tap(find.text('9'));
  await tester.pump();
  expect(selected, equals(9));
  expect(find.byKey(const Key('chip-9')),
    findsWidgetWithStyle(hasBackground(AppColors.primary)));
});

// QuestionCard
testWidgets('QuestionCard highlights correct and wrong options after submit', (tester) async {
  await tester.pumpWidget(MaterialApp(
    home: QuestionCard(
      question: mockQuestion(correct: 'C'),
      selectedOption: 'A',
      submitted: true,
    ),
  ));
  final optionA = find.byKey(const Key('option-A'));
  final optionC = find.byKey(const Key('option-C'));
  expect(optionA, hasBackgroundColor(AppColors.error.withOpacity(0.2)));
  expect(optionC, hasBackgroundColor(AppColors.success.withOpacity(0.2)));
});

// Empty state when no packs
testWidgets('Home shows empty state when no packs installed', (tester) async {
  await tester.pumpWidget(ProviderScope(
    overrides: [installedPacksProvider.overrideWith((_) => [])],
    child: const MaterialApp(home: HomeScreen()),
  ));
  expect(find.text('Hech qanday paket yuklanmagan'), findsOneWidget);
  expect(find.text('Paketlar sahifasiga o\'tish'), findsOneWidget);
});
```

---

## 5. Integration Tests (Patrol — on real device/emulator)

Location: `app/integration_test/`

### 5.1 Full offline test flow (most critical)
```dart
// integration_test/offline_test_flow_test.dart
patrolTest('student can take a full 25-question test in airplane mode', ($) async {
  // Pre-condition: matematika_09.db already imported into local DB
  await $.native.enableAirplaneMode();

  await $.pumpWidgetAndSettle(const MyApp());

  // Navigate to Test Config
  await $('Test').tap();

  // Select Matematika, grade 9
  await $('Matematika').tap();
  await $('9').tap();
  await $('TEST BOSHLASH').tap();

  // Answer all 25 questions
  for (var i = 0; i < 25; i++) {
    await $(find.byType(OptionCard).first).tap();
    await $('Keyingi').tap();
  }

  // Verify results screen
  expect($('/ 25'), findsOneWidget);
  expect($('Natija'), findsOneWidget);

  await $.native.disableAirplaneMode();
});
```

### 5.2 Pack download and merge
```dart
patrolTest('downloading a pack makes its questions available for tests', ($) async {
  await $.pumpWidgetAndSettle(const MyApp());
  await $('Paketlar').tap();
  await $('Fizika 9-sinf').tap();
  await $('Yuklab olish').tap();

  // Wait for download to complete
  await $.waitUntilVisible($('✓'), timeout: const Duration(minutes: 2));

  // Verify questions available
  await $('Test').tap();
  await $('Fizika').tap();
  await $('9').tap();
  expect($('850 savol'), findsOneWidget);
});
```

---

## 6. API Backend Tests (Kotlin / Ktor)

### Pack catalog endpoint
```kotlin
@Test
fun `catalog returns all published packs`() = testApplication {
  val response = client.get("/api/v1/packs/catalog")
  assertThat(response.status).isEqualTo(HttpStatusCode.OK)
  val body = response.body<CatalogResponse>()
  assertThat(body.data.packs).isNotEmpty()
  body.data.packs.forEach { pack ->
    assertThat(pack.packKey).isNotEmpty()
    assertThat(pack.questionCount).isGreaterThan(0)
    assertThat(pack.checksumSha256).hasLength(64)
  }
}

@Test
fun `download-url requires auth for premium packs`() = testApplication {
  val response = client.get("/api/v1/packs/matematika_10/download-url")
  assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
}

@Test
fun `download-url signed URL expires after 1 hour`() = testApplication {
  val response = client.get("/api/v1/packs/matematika_09/download-url") {
    bearerAuth(freeUserToken)
  }
  val url = response.body<DownloadUrlResponse>().data.url
  val expiresAt = response.body<DownloadUrlResponse>().data.expiresAt
  assertThat(expiresAt).isCloseTo(Instant.now().plusSeconds(3600), within(60, SECONDS))
}
```

### Sync endpoint
```kotlin
@Test
fun `POST sync progress stores sessions and returns synced count`() = testApplication {
  val payload = buildSyncPayload(sessionCount = 3)
  val response = client.post("/api/v1/sync/progress") {
    bearerAuth(studentToken)
    setBody(payload)
  }
  assertThat(response.status).isEqualTo(HttpStatusCode.OK)
  assertThat(response.body<SyncResponse>().data.syncedCount).isEqualTo(3)
}

@Test
fun `sync is idempotent — resending same local_id is safe`() = testApplication {
  val payload = buildSyncPayload(sessionCount = 1)
  client.post("/api/v1/sync/progress") { bearerAuth(studentToken); setBody(payload) }
  val response2 = client.post("/api/v1/sync/progress") { bearerAuth(studentToken); setBody(payload) }
  assertThat(response2.body<SyncResponse>().data.skippedCount).isEqualTo(1)
}
```

---

## 7. Python Pipeline Tests

Location: `tools/tests/`

```python
# test_chunker.py
def test_chunk_respects_max_tokens():
    text = "Lorem ipsum " * 500  # long text
    chunks = chunk_text(text, max_tokens=800, overlap=100)
    for chunk in chunks:
        assert count_tokens(chunk) <= 850  # 800 + small tolerance

def test_chunk_overlap_preserves_context():
    chunks = chunk_text(sample_text, max_tokens=800, overlap=100)
    # End of chunk N should appear at start of chunk N+1
    for i in range(len(chunks) - 1):
        end_of_current = chunks[i][-200:]
        start_of_next  = chunks[i+1][:200]
        assert any(word in start_of_next for word in end_of_current.split()[-10:])

# test_export.py
def test_exported_db_has_correct_schema():
    db_path = export_pack(mock_questions, 'matematika', 9)
    conn = sqlite3.connect(db_path)
    tables = conn.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall()
    assert ('questions',) in tables
    cols = [row[1] for row in conn.execute("PRAGMA table_info(questions)").fetchall()]
    for required in ['subject','grade','question_text','option_a','option_b',
                     'option_c','option_d','correct','checksum']:
        assert required in cols

def test_checksums_are_unique_within_pack():
    db_path = export_pack(mock_questions_with_duplicates, 'matematika', 9)
    conn = sqlite3.connect(db_path)
    total = conn.execute("SELECT COUNT(*) FROM questions").fetchone()[0]
    unique = conn.execute("SELECT COUNT(DISTINCT checksum) FROM questions").fetchone()[0]
    assert total == unique  # no duplicates

def test_minimum_questions_per_grade_met():
    # Each grade should have at least 200 questions after pipeline
    for grade in range(1, 12):
        db_path = f'output/matematika_{grade:02d}.db'
        if os.path.exists(db_path):
            conn = sqlite3.connect(db_path)
            count = conn.execute("SELECT COUNT(*) FROM questions").fetchone()[0]
            assert count >= 200, f"Grade {grade} has only {count} questions"
```

---

## 8. CI Pipeline

```yaml
# .github/workflows/test.yml
jobs:
  flutter-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: subosito/flutter-action@v2
        with: { flutter-version: '3.22.0', cache: true }
      - run: flutter pub get
      - run: flutter analyze --fatal-infos
      - run: flutter test --coverage
      - name: Check coverage
        run: |
          lcov --summary coverage/lcov.info
          # Fail if test_engine/ coverage < 90%
          python scripts/check_coverage.py --module test_engine --min 90

  api-test:
    runs-on: ubuntu-latest
    services:
      postgres: { image: postgres:16-alpine, env: { POSTGRES_DB: tm_test } }
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: temurin, cache: gradle }
      - run: ./gradlew test

  python-tools-test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-python@v5
        with: { python-version: '3.12' }
      - run: pip install -r tools/requirements.txt --break-system-packages
      - run: python -m pytest tools/tests/ -v
```

---

## 9. Coverage Targets

| Component | Unit | Integration | Critical paths |
|---|---|---|---|
| TestGenerator | 95% | 100% (offline flow) | 100% — wrong answers here = broken product |
| PackImporter | 90% | 100% (download+merge) | 100% — corrupt imports must be caught |
| Drift DAOs | 80% | — | Elo + streak: 100% |
| SyncService | 80% | — | Idempotency: 100% |
| API pack catalog | 85% | — | Auth + signed URL: 100% |
| Python pipeline | 70% | — | Export schema + dedup: 100% |
