# TestMarkaz — MVP Roadmap & Scaling Plan

> Offline-first test prep app. Built for a solo founder with ~30 focused hours/week. The MVP ships a working offline Android app where a student can take 25-question tests from downloaded content packs, track their progress, and never need internet after first download.

---

## Phase 0 — Foundation (Weeks 1–2)

**Goal:** Infrastructure, CI/CD, and skeleton apps on day 1. No product features yet.

- Provision Hetzner CX32 VPS, Cloudflare DNS, Cloudflare R2 bucket
- Set up Supabase project (Postgres + Auth)
- Create repos: `app` (Flutter), `api` (Ktor), `tools` (Python pipeline)
- Ktor skeleton: `/health`, `/ready`, JWT validation, Sentry, Loki logging
- Flutter skeleton: navigation shell, Drift DB setup, dark theme, uz-Latn i18n
- GitHub Actions: test + build on every PR, deploy to staging on merge to `main`
- R2 bucket structure: `/packs/`, `/packs/catalog.json`
- Python pipeline environment: Tesseract 5 + OpenAI SDK installed, prompts written
- **Seed the first content pack**: process one complete book (e.g. Matematika 9-sinf) → 500+ questions → `matematika_09.db` uploaded to R2

**Exit criteria:** Flutter app loads, reads from local Drift DB, can download `matematika_09.db` from R2 and merge into local DB. CI green.

---

## Phase 1 — MVP (Weeks 3–14, ~3 months)

**Goal:** A student downloads the app, picks a subject and grade, takes a 25-question test fully offline, sees results with explanations, tracks their streak. Android first; iOS in week 13.

### Weeks 3–4 — Content pack pipeline
- Run all available books through the pipeline (target: 8 subjects × grades 9-11 = 24 packs)
- Admin question review UI (simple terminal script `05_review_ui.py`)
- Pack catalog endpoint (`GET /packs/catalog`, `GET /packs/catalog/diff`)
- Signed download URL endpoint (`GET /packs/:key/download-url`)
- Free tier: first 3 packs downloadable without account
- Pack catalog JSON served directly from R2 edge (Cloudflare cache)

**Deliverable:** 24 packs covering grades 9–11 core DTM subjects, all published and downloadable.

### Weeks 5–6 — Flutter: Downloads screen
- Pack catalog browser in app (subject grid → grade list → download button)
- Background download with progress indicator (dio + resumable)
- Checksum verification after download
- Pack import: merge `.db` into local Drift DB, dedup by `checksum`
- Installed packs registry (show what's downloaded, size, version)
- Offline indicator chip (connectivity_plus)

**Deliverable:** Student can browse all available packs, download 3 for free, see them installed.

### Weeks 7–8 — Flutter: Test engine + Test session screen
- `TestConfig` model: subject, gradeMin, gradeMax, mode (subject/range/randomClass/fullRandom)
- `TestGenerator.generateTest()`: SQL random select of exactly 25 questions, stratified by difficulty (40/40/20)
- Test configuration screen: subject picker → grade/range selector → start
- 25-question test UI: progress bar (1/25), question card, 4 options, no time limit
- Answer submission: instant feedback (correct ✓ / wrong ✗ + correct answer shown)
- Explanation shown after each answer
- Bookmark button on each question

**Deliverable:** Full offline test flow works end-to-end.

### Weeks 9–10 — Flutter: Results + Progress
- Results screen: score (N/25), topic breakdown, list of wrong answers for review
- Wrong answers saved to local `wrong_answers` table
- Progress screen: subject mastery bars, Elo-style rating updated after each test
- Streak tracking: daily tests = streak kept, +XP, streak_data table
- Home screen: current streak, XP this week, "Continue" CTA, weak topic suggestions
- History screen: list of all past sessions with scores

**Deliverable:** Student can see their progress over time and knows what to focus on.

### Weeks 11–12 — Auth + Sync
- Login / Register / OTP flow (phone-first, Google as backup)
- Progress sync: push completed sessions to server (`POST /sync/progress`)
- Sync scheduler: runs on app foreground if online + sessions pending
- Server-side session backup in Postgres
- Account profile: name, grade, region, locale

**Deliverable:** Logged-in students don't lose progress if they switch devices.

### Weeks 13–14 — Polish + iOS + Monetization stub
- iOS build, Apple Sign-In, TestFlight beta
- Premium paywall: > 3 packs requires free account; unlimited packs requires premium
- Click integration (basic — redirect to payment page, webhook activates premium)
- Push notifications: streak reminder (daily at 19:00 local), new pack alerts
- App Store + Play Store submission
- Onboarding flow (3 screens: welcome → pick subjects → download first pack)

**MVP launch criteria (end of Week 14):**
- 24+ content packs covering grades 9–11, all core DTM subjects
- Student can: download packs → take 25-question test fully offline → see results → track streak
- Works on Android 8+ and iOS 14+
- Sync works when online; everything works when offline
- 1,000 test questions minimum per subject for grade 9
- Crash-free rate > 99%
- App Store + Play Store both published

---

## Phase 2 — Growth & Content Expansion (Weeks 15–32, months 4–8)

**Goal:** Expand content to all grades 1–11, add premium features, get paying users.

### Weeks 15–18 — Expand content to all grades
- Run pipeline for grades 1–8 (currently have 9–11)
- Target: 80+ packs covering all subjects, all grades
- Subject bundles: `bundle_matematika.db` (all grades 1–11 in one file, ~11 MB)
- Admin dashboard (web): question approval UI, pack status board
- Automated quality check: flag questions with duplicate text, missing explanation

### Weeks 19–22 — Premium features
- Payme integration (second-most popular in UZ)
- Stripe for diaspora
- Unlimited pack downloads for premium
- Timed exam mode (25 questions, configurable countdown 15/30/45 min)
- Bookmarks test: take a 25-question test from your bookmarked questions only
- Wrong answers test: take a test made of only your historically wrong questions

### Weeks 23–26 — Advanced analytics
- Topic mastery detail screen (question-level breakdown per topic)
- Weak topic auto-test: app suggests a focused 25-question test on weakest topic
- Cumulative stats: total questions answered, accuracy trend, time invested
- Subject comparison chart

### Weeks 27–28 — Social / Gamification
- Leaderboard (national, regional, by grade)
- Badge system (50+ badges)
- Streak freezes (premium: 5 freezes/month)
- Share result card (screenshot-ready result card for sharing to Telegram)

### Weeks 29–32 — DTM Simulation Mode
- Official DTM format: multiple subjects in one session
- 90-minute timed test matching real DTM structure
- DTM-specific question difficulty calibration
- Mock DTM score prediction

**Growth milestones:**
- Week 20: 5,000 registered users
- Week 28: 25,000 registered users, 500 paying ($1k MRR)
- Week 32: 75,000 registered users, 2,500 paying ($5k MRR)

---

## Phase 3 — Platform (Months 9–18)

**Goal:** Become the default DTM prep app in Uzbekistan.

- Teacher / tutor tools: assign packs to students, track class performance
- Learning center B2B: bulk accounts, white-label, monthly billing
- Content marketplace: verified teachers can submit their own question sets
- Live monthly DTM simulations (10,000 students simultaneously)
- AI study assistant (RAG-based, scoped to downloaded books)
- Web app (for students who prefer desktop study)
- Multi-language packs: Uzbek Cyrillic + Russian language of instruction

---

## Content Targets by Phase

| Phase | Grades covered | Subjects | Total packs | Est. questions |
|---|---|---|---|---|
| MVP (Week 14) | 9–11 | 8 core | 24 | ~20,000 |
| Phase 2 (Week 32) | 1–11 | 13+ | 80+ | ~80,000 |
| Phase 3 | 1–11 + DTM | 16 | 120+ | ~150,000 |

---

## Resourcing & Spend (Phase 1)

| Item | Monthly cost |
|---|---|
| Hetzner CX32 VPS | $6 |
| Supabase Pro | $25 |
| Cloudflare R2 (80 MB packs + ~50 GB bandwidth) | ~$2 |
| OpenAI API (book processing — one-time per book, not per user) | ~$30 |
| Eskiz.uz SMS OTP | $10 |
| Sentry / UptimeRobot free tiers | $0 |
| **Total** | **~$75/month** |

One-time pre-launch:
- Apple Developer + Play Console: $124 + $25
- Legal (LLC + ToS + Privacy in UZ): $500
- Logo + brand kit: $300

**Pre-launch budget: ~$1,500 for 14 weeks.**

The AI pipeline runs once per book, not per user — unlike the original design, there is no per-user AI cost in Phase 1. OpenAI is only used to generate the question bank; after that, everything is offline.

---

## Risks & Mitigations (Updated)

| Risk | Mitigation |
|---|---|
| OCR quality poor on scanned books | Source digital PDFs from Ministry first; scanned books processed with Tesseract quality threshold |
| Generated questions are factually wrong | Human review step before any question is published (05_review_ui.py) |
| Pack download fails on slow network | Resumable downloads via dio Range header; retry on next launch |
| Local DB gets corrupted | Drift migrations; backup restore from server sync |
| Not enough questions for a 25-question test (small grade/subject combo) | Minimum pool check: if < 50 questions in pool, prompt user to download parent bundle |
| Play Store / App Store rejection | Submit early; education apps approved quickly; comply with content policies |

---

## North-Star Metrics

| Metric | MVP target | Year 1 target |
|---|---|---|
| Registered users | 1,000 (week 14) | 100,000 |
| Tests taken / user / week | 5 | 15 |
| Offline usage rate | > 60% of sessions | > 70% |
| Premium conversion | 1% | 4% |
| MRR | — | $10,000 |
| Crash-free rate | > 99% | > 99.5% |
| Questions in DB | 20,000 | 100,000 |
