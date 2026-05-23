# TestMarkaz — Monetization Strategy

> Designed for Uzbekistan's purchasing power. The core product — offline 25-question tests — is free with a 3-pack download limit. Premium unlocks the full library of 80+ subject packs. No AI cost per user in Phase 1 (questions are pre-generated).

---

## 1. Pricing Tiers

| Plan | Price (UZS / month) | Price (~$) | Target |
|---|---|---|---|
| **Free** | 0 | 0 | Acquisition — try 3 packs |
| **Premium** | 29,000 | ~$2.30 | Active students |
| **Premium Yearly** | 199,000 | ~$15.90 | Committed learners (save 43%) |
| **DTM Intense** | 49,000 | ~$3.90 | Final year abituriyents |
| **Center** | 990,000 + 9,000/student | ~$78 + $0.71 | Learning centers (min 50 students) |

---

## 2. Feature Matrix

| Feature | Free | Premium | DTM Intense | Center |
|---|---|---|---|---|
| 25-question test (offline) | ✓ | ✓ | ✓ | ✓ |
| Download content packs | 3 packs max | Unlimited | Unlimited | Unlimited |
| Subjects available | 3 | All 16 subjects | All + DTM bundles | All |
| Grade range for packs | 1 grade only | Any grade 1–11 | Any | Any |
| Subject bundles (all grades) | — | ✓ | ✓ | ✓ |
| Full random test (all subjects) | — | ✓ | ✓ | ✓ |
| Timed exam mode | — | ✓ | ✓ | ✓ |
| Bookmarks test | — | ✓ | ✓ | ✓ |
| Wrong answers test | — | ✓ | ✓ | ✓ |
| Full progress analytics | Last 7 days | Full history | Full history | Full |
| Topic mastery breakdown | — | ✓ | ✓ | ✓ |
| DTM simulation (multi-subject 90 min) | — | — | ✓ | ✓ |
| Leaderboards | — | ✓ | ✓ | ✓ |
| Streak freezes | 1/month | 5/month | 10/month | 10/student |
| Offline access | ✓ | ✓ | ✓ | ✓ |
| Ads | Yes (future) | No | No | No |
| Priority pack updates | — | ✓ | ✓ | ✓ |
| Classroom analytics | — | — | — | ✓ |
| Bulk student management | — | — | — | ✓ |

---

## 3. What "3 Free Packs" Means

Free users can download any 3 packs from the catalog without an account. Examples:

- Matematika 9-sinf + Fizika 9-sinf + Kimyo 9-sinf → covers a 9th grader's core DTM subjects
- Ingliz tili 10-sinf + O'zbek tili 10-sinf + Tarix 10-sinf → covers humanities track

This is intentionally generous. A student can get meaningful value for free. The goal is to get the app installed, not to restrict — conversion to premium happens when the student wants more grades, more subjects, or bundle packs.

**No account needed for free packs.** Download requires only a device UUID. This removes registration friction entirely for the first 3 packs.

---

## 4. Content Pack Download Limits by Plan

| Plan | Max packs | Bundles | Grade range |
|---|---|---|---|
| Free (no account) | 3 single-grade packs | No | One grade only |
| Free (with account) | 5 single-grade packs | No | Any single grade |
| Premium | Unlimited | Yes | Any range |
| DTM Intense | Unlimited | Yes (DTM-optimized) | Any |
| Center | Unlimited | Yes | Any |

---

## 5. DTM Intense Plan

Targeted specifically at 11th graders preparing for the university entrance exam (DTM). Slightly higher price because this student has urgent motivation.

Extras over Premium:
- Pre-built DTM simulation tests (matching official format exactly)
- DTM subject bundles optimized with exam-difficulty questions only
- 90-minute timed sessions matching real DTM duration
- Score prediction based on practice performance

---

## 6. Revenue Projections

### Phase 1 (Month 1–3): Free user growth
No paid plans yet. Focus on downloads, retention, content quality.

### Phase 2 (Month 4–8): First paid users
- 1% of registered users convert to Premium at 29,000 UZS/month
- At 5,000 registered users → 50 paying → **1,450,000 UZS (~$115) MRR**

### Year 1 targets
| Registered users | Premium % | MRR (UZS) | MRR (~$) |
|---|---|---|---|
| 10,000 | 2% | 5,800,000 | $460 |
| 50,000 | 3% | 43,500,000 | $3,450 |
| 100,000 | 4% | 116,000,000 | $9,200 |

At 4% conversion on 100k users: **~$9,200 MRR**. Yearly subscribers boost this by ~30% (lumpsum payments).

---

## 7. Payment Methods (Uzbekistan)

| Provider | Market share | Integration complexity | Go-live timeline |
|---|---|---|---|
| Click | ~40% | Medium | Week 13 |
| Payme | ~35% | Medium | Week 19 |
| Uzcard / Humo | ~20% | High (use Octo aggregator) | Phase 2 |
| Stripe | ~5% (diaspora) | Easy | Week 19 |

**Recommendation:** Ship Click first. It has the highest share among tech-savvy users and the best developer documentation.

---

## 8. Other Revenue Streams (Phase 2+)

### Learning center B2B
- 990,000 UZS base + 9,000/student/month
- Min 50 students = ~1,440,000 UZS/month per center
- Target: 10 centers in Year 1 = 14,400,000 UZS/month ($1,140 MRR) just from B2B
- Value prop: teacher can monitor every student's test history, weak topics, streak

### DTM exam event (Phase 3)
- Branded "Official DTM 2027 Simulation" — one-time 50,000 UZS per student
- Non-premium students pay; premium students get it free
- Target 2,000 paid participants = 100,000,000 UZS per event (~$8,000)

### Sponsored packs (Phase 3)
- Universities and tutoring centers can sponsor a subject pack ("Toshkent Davlat Texnika Universiteti taqdimotida: Matematika DTM paketi")
- 500,000–2,000,000 UZS/month per sponsor

---

## 9. Cost Model

Unlike the original AI-assistant design, Phase 1 has almost no per-user variable cost:

| Cost | Nature | Amount |
|---|---|---|
| Infrastructure (VPS + R2 + Supabase) | Fixed | ~$35/month |
| OpenAI (question generation per book) | One-time per book | ~$0.50–1.00/book |
| Eskiz.uz SMS OTP | Per new user | ~15 UZS ($0.001) per OTP |
| Pack download bandwidth | R2 zero egress | ~$0 |

**Variable cost per active user: essentially $0** after initial content generation. This makes the unit economics dramatically better than an AI-assistant model.

Breakeven: ~25 Premium subscribers at 29,000 UZS = 725,000 UZS = ~$58/month, which covers all infrastructure.
