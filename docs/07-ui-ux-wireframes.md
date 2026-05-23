# TestMarkaz — UI/UX Wireframes & Screen Specifications

> Mobile-first offline app. Dark mode default. Primary: indigo `#6366F1`. Success: emerald `#10B981`. Warning: amber `#F59E0B`. Error: rose `#EF4444`. Streak: orange `#FB923C`. Typography: Inter. Platform: Flutter (Android + iOS).

---

## 1. Global Layout

```
┌────────────────────────┐
│  STATUS BAR            │
├────────────────────────┤
│  PAGE TITLE  [actions] │  ← AppBar (56dp)
├────────────────────────┤
│                        │
│   SCROLLABLE CONTENT   │
│                        │
├────────────────────────┤
│ 🏠  📦  ✏️  📊  👤   │  ← BottomNavigationBar
└────────────────────────┘
```

**Bottom nav tabs:** Home · Downloads · Test · Progress · Profile

**Offline indicator:** When offline, a subtle amber chip `● Oflayn` appears in the AppBar — never blocking, never a modal.

---

## 2. Onboarding (3 screens, shown once on first launch)

### Screen 1 — Welcome
```
[Full-screen illustration: student with phone, books, stars]

  TestMarkaz

  "O'zbek maktab dasturining barcha
   fanlari bo'yicha test yechim"

  [Boshlash →]         [Kirish]
```

### Screen 2 — Pick your subjects (multi-select)
```
  Fanlaringizni tanlang

  [📐 Matematika ✓]  [⚗️ Kimyo    ]
  [🔭 Fizika    ✓]  [🧬 Biologiya]
  [🌍 Ingliz    ✓]  [📖 O'zbek   ]
  [🏛️ Tarix      ]  [🗺️ Geografiya]

  Sinf: [9-sinf ▾]

  [Davom etish →]
```

### Screen 3 — Download first pack
```
  Birinchi paketni yuklab oling

  Siz tanlagan fanlar:
  ┌──────────────────────────────────┐
  │  📐 Matematika 9-sinf  1.2 MB   │
  │  🔭 Fizika 9-sinf      0.9 MB   │
  │  🌍 Ingliz tili 9-sinf 1.1 MB   │
  └──────────────────────────────────┘

  Jami: 3.2 MB · Bepul (3 ta paket)

  [Yuklab olish ▼]
  
  ──── yoki ────
  
  [Keyinroq]
```

---

## 3. Home Screen (Dashboard)

```
┌────────────────────────────────────────┐
│  Salom, Ali 👋              ● Online   │
│  Streak: 🔥 14 kun   XP: ▓▓▓░ 2,340  │
├────────────────────────────────────────┤
│  ┌──────────────────────────────────┐  │
│  │  TEZKOR TEST                     │  │
│  │  Oxirgi fan: Matematika 9-sinf   │  │
│  │                    [Boshlash →]  │  │
│  └──────────────────────────────────┘  │
├────────────────────────────────────────┤
│  KUCHSIZ MAVZULAR                      │
│  ⚠ Differensial hisob   42%  [Test]  │
│  ⚠ Organik kimyo        51%  [Test]  │
├────────────────────────────────────────┤
│  YUKLAB OLINGAN FANLAR                 │
│  [📐 Mat.]  [🔭 Fiz.]  [🌍 Ingl.]   │
│   68%        54%        79%           │
│  [+ Yangi fan qo'shish]               │
├────────────────────────────────────────┤
│  SO'NGGI NATIJALAR                     │
│  Matematika 9 · 18/25 · 72% · bugun  │
│  Fizika 9    · 15/25 · 60% · kecha   │
└────────────────────────────────────────┘
```

**"Boshlash →" on the quick test card** opens the Test Configuration screen with last-used settings pre-filled.

---

## 4. Test Configuration Screen ⭐ (Core new screen)

This is the most important screen in the app. It must be fast and satisfying to use.

```
┌────────────────────────────────────────┐
│  ← Yangi test                          │
├────────────────────────────────────────┤
│  FAN TANLANG                           │
│                                        │
│  [📐 Mat. ✓] [🔭 Fiz. ] [⚗️ Kim. ]  │
│  [🧬 Bio.  ] [🌍 Ingl.] [📖 O'zb. ]  │
│  [🏛️ Tar.  ] [🗺️ Geo. ]             │
│  [🎲 Tasodifiy (barcha fanlar)]        │
├────────────────────────────────────────┤
│  SINF / DARAJA                         │
│                                        │
│  Test turi:                            │
│  ● Bitta sinf     ○ Sinf oralig'i     │
│                   ○ Tasodifiy sinf    │
│                                        │
│  [Bitta sinf mode:]                    │
│  ┌──────────────────────────────────┐  │
│  │  1  2  3  4  5  6  7  8  9 10 11│  │
│  │                    [9] ←selected │  │
│  └──────────────────────────────────┘  │
│                                        │
│  [Sinf oralig'i mode:]                 │
│  ┌──────────────────────────────────┐  │
│  │  Dan: [7]  Gacha: [9]           │  │
│  │  ▓▓▓▓▓▓░░░░░  7–9-sinf          │  │
│  └──────────────────────────────────┘  │
│                                        │
│  [Tasodifiy mode:]                     │
│  ┌──────────────────────────────────┐  │
│  │  🎲 Istalgan sinfdan 25 ta savol │  │
│  └──────────────────────────────────┘  │
├────────────────────────────────────────┤
│  Mavjud savollar: 1,240   ✓ Yetarli  │
├────────────────────────────────────────┤
│  [TEST BOSHLASH — 25 ta savol →]      │
└────────────────────────────────────────┘
```

**Key UX details:**
- Available question count updates in real-time as subject/grade changes
- If pool < 50 questions: "Yetarli emas — ushbu fanning to'liq paketini yuklab oling" with deep link to Downloads
- Last-used config is saved to SharedPreferences — next open pre-fills it
- "Tasodifiy" (random) options are highlighted with a dice icon — fun and prominent
- 25 is shown but not editable (it is fixed and users don't need to think about it)

---

## 5. Test Session Screen

```
┌────────────────────────────────────────┐
│  Matematika · 9-sinf       [✕ Chiqish]│
│  ━━━━━━━━━━━━░░░░░░░░░░░░  8 / 25     │
├────────────────────────────────────────┤
│                                        │
│  MAVZU: Trigonometriya                 │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │  sin²α + cos²α = ?               │  │
│  └──────────────────────────────────┘  │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │  A   0                           │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │  B   −1                          │  │
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │  C   1              ← selected  │  │ ← highlighted indigo border
│  └──────────────────────────────────┘  │
│  ┌──────────────────────────────────┐  │
│  │  D   2                           │  │
│  └──────────────────────────────────┘  │
│                                        │
│  [🔖 Saqlash]    [Tekshirish →]       │
└────────────────────────────────────────┘
```

**After "Tekshirish" (check answer):**
```
  ✅ To'g'ri!  +10 XP

  Option C is shown with green fill.
  Option borders fade to gray (unchosen).

  Tushuntirish:
  "Pifagor trigonometrik tengligiga ko'ra,
   har qanday α uchun sin²α + cos²α = 1.
   — Algebra 9-sinf, 7-bob"

  [Keyingi savol →]
```

**If wrong:**
```
  ❌ Noto'g'ri.

  Option A (user's choice) shown with red fill.
  Option C (correct) shown with green fill.

  Tushuntirish: "..."

  [🔖 Xato savolni saqlash]   [Keyingi →]
```

**Exit confirmation (if mid-test):**
```
  ┌─────────────────────────────┐
  │  Testdan chiqmoqchimisiz?   │
  │                             │
  │  8/25 javob berildi         │
  │  Natija saqlanmaydi         │
  │                             │
  │  [Bekor qilish]  [Chiqish] │
  └─────────────────────────────┘
```

---

## 6. Test Results Screen

```
┌────────────────────────────────────────┐
│  ← Test natijasi                        │
├────────────────────────────────────────┤
│                                        │
│           18 / 25                      │
│         ████████████████░░░░░          │
│              72%                       │
│                                        │
│   +180 XP        ⏱ 6 daqiqa 40 soniya │
│                                        │
├────────────────────────────────────────┤
│  MAVZU BO'YICHA NATIJA                 │
│                                        │
│  Trigonometriya  ████████ 9/10  90% ✅ │
│  Algebra         ███████░ 7/10  70% 🔸 │
│  Differensial    █████░░░ 5/10  50% ⚠ │
│                  ↑ Zaif mavzu          │
├────────────────────────────────────────┤
│  XATO JAVOBLAR (7 ta)                  │
│                                        │
│  Q14. f'(x) = ?   Siz: A  To'g'ri: C │
│  Q19. ∫2x dx = ?  Siz: B  To'g'ri: A │
│  ...                [Barchasini ko'r] │
├────────────────────────────────────────┤
│  [🔄 Qayta test]  [⚠ Zaif mavzu test] │
│  [📊 Tahlilga o'tish]                  │
└────────────────────────────────────────┘
```

---

## 7. Downloads Screen ⭐ (Content Pack Manager)

```
┌────────────────────────────────────────┐
│  Paketlar                  [🔍 Qidirish]│
├────────────────────────────────────────┤
│  YUKLAB OLINGAN  (3 ta)                │
│                                        │
│  📐 Matematika 9-sinf                  │
│  850 savol · 1.2 MB · v3  [✓ O'chiris]│
│                                        │
│  🔭 Fizika 9-sinf                      │
│  620 savol · 0.9 MB · v2  [✓ O'chiris]│
│                                        │
│  🌍 Ingliz tili 9-sinf                 │
│  780 savol · 1.1 MB · v1  [↑ Yangilash]│ ← update available
│                                        │
├────────────────────────────────────────┤
│  MAVJUD PAKETLAR                       │
│                                        │
│  Filters: [Barcha fanlar ▾] [9-sinf ▾]│
│                                        │
│  📐 MATEMATIKA                         │
│  ┌──────────────────────────────────┐  │
│  │  1-sinf  200 savol  0.3MB  [⬇]  │  │
│  │  ...                             │  │
│  │  9-sinf  850 savol  1.2MB  [✓]  │  │ ← already installed
│  │  10-sinf 900 savol  1.3MB  [⬇]  │  │
│  │  11-sinf 920 savol  1.3MB  [⬇]  │  │
│  │  ─────────────────────────────── │  │
│  │  Bundle (1-11 sinf) 9,200 savol  │  │
│  │  11 MB  ⭐ Premium  [🔒 Premium] │  │
│  └──────────────────────────────────┘  │
│                                        │
│  ⚗️ KIMYO                              │
│  [1-sinf ... 11-sinf + Bundle]         │
│                                        │
└────────────────────────────────────────┘
```

**Download in progress:**
```
  📐 Matematika 10-sinf
  ━━━━━━━━━━━━━━━░░░░░  73%  0.9/1.3 MB
  [❌ Bekor qilish]
```

**Premium lock CTA (when user taps locked pack):**
```
  ┌─────────────────────────────────────┐
  │  ⭐ Premium kerak                    │
  │                                     │
  │  Barcha fanlar va sinflarni         │
  │  cheksiz yuklab oling.              │
  │                                     │
  │  29,000 UZS/oy                      │
  │  [Premium olish]   [Yoping]        │
  └─────────────────────────────────────┘
```

---

## 8. Subject Browser Screen

```
┌────────────────────────────────────────┐
│  ← Matematika                          │
├────────────────────────────────────────┤
│  Sinf tanlang:                         │
│                                        │
│  ✓ YUKLAB OLINGAN                      │
│  ┌───────────────────────────────┐     │
│  │  9-sinf   850 savol  [Test →] │     │
│  └───────────────────────────────┘     │
│                                        │
│  🔒 YUKLAB OLINMAGAN                   │
│  ┌───────────────────────────────┐     │
│  │  1-sinf   200 savol  [⬇ Yukla]│     │
│  │  2-sinf   220 savol  [⬇ Yukla]│     │
│  │  ...                          │     │
│  │  10-sinf  900 savol  [⬇ Yukla]│     │
│  │  11-sinf  920 savol  [⬇ Yukla]│     │
│  └───────────────────────────────┘     │
│                                        │
│  ORALIG' TESTI                         │
│  [7–9-sinf testi →]   [1–11 testi →]  │
└────────────────────────────────────────┘
```

---

## 9. Progress Screen

```
┌────────────────────────────────────────┐
│  Tahlil                                │
│  Tabs: [Umumiy]  [Fanlar]  [Tarix]    │
├────────────────────────────────────────┤
│  [Umumiy tab]                          │
│                                        │
│       75%   o'rtacha natija            │
│       ████████████████░░░░            │
│                                        │
│  Bu hafta: 347 savol · 4s 23d         │
│  Streak: 🔥 14 kun ketma-ket           │
│  Jami testlar: 82                      │
│                                        │
│  KUCHSIZ MAVZULAR                      │
│  ⚠ Differensial hisob  45%  [Test]   │
│  ⚠ Organik kimyo       51%  [Test]   │
│  ⚠ Optika              55%  [Test]   │
│                                        │
│  FAN REYTINGI (Elo)                    │
│  📐 Matematika  ████████████  1640 ↑  │
│  🔭 Fizika      █████████░░░  1420 ↑  │
│  🌍 Ingliz tili ███████████░  1580 ↑  │
│                                        │
│  [Fanlar tab]                          │
│  → Per-subject breakdown with topic    │
│    mastery bars and question counts    │
│                                        │
│  [Tarix tab]                           │
│  → Chronological list of all sessions  │
│    with scores, subjects, dates        │
└────────────────────────────────────────┘
```

---

## 10. Profile & Settings Screen

```
┌────────────────────────────────────────┐
│  Profil                                │
├────────────────────────────────────────┤
│         [Avatar: A]                    │
│         Ali Karimov                    │
│         9-sinf · Toshkent             │
│                                        │
│   2,340 XP    🔥 14 kun    82 test    │
│                                        │
│  ┌──────────────────────────────────┐  │
│  │  ⭐ Hozirgi reja: Bepul          │  │
│  │  [Premium olish — 29,000 UZS/oy]│  │
│  └──────────────────────────────────┘  │
├────────────────────────────────────────┤
│  [👤] Profilni tahrirlash          →  │
│  [🌐] Til: O'zbek (lotin)          →  │
│  [🔔] Bildirishnomalar             →  │
│  [📦] Paketlar boshqaruvi          →  │
│  [☁] Sinxronizatsiya               →  │
│  [🗑] Ma'lumotlarni tozalash       →  │
│  [ℹ] Ilova haqida                  →  │
│  [🚪] Chiqish                         │
└────────────────────────────────────────┘
```

**Sync status screen:**
```
  ☁ Sinxronizatsiya

  Holat:     Online ✓
  Oxirgi:    22-may, 09:00
  Kutayotgan: 0 sessiya
  
  [Hozir sinxronlash]
  
  Hisob: ali@example.com
  
  [Ma'lumotlarni o'chirish (serverdan)]
```

---

## 11. Grade Range Picker Component (Reusable)

This component appears on the Test Config screen and Subject Browser.

```
Test turi:
┌─────────────────────────────────────────┐
│  ● Bitta sinf                           │
│  ○ Sinf oralig'i                        │
│  ○ Tasodifiy                            │
└─────────────────────────────────────────┘

[Bitta sinf selected:]
  Sinf:
  ┌────────────────────────────────────┐
  │  1  2  3  4  5  6  7  8 [9] 10 11 │
  └────────────────────────────────────┘
  → Tappable number chips, selected = indigo fill

[Oralig' selected:]
  Dan:   [7 ▾]   Gacha: [9 ▾]
  ████████████████░░░░░░  7 · 8 · 9-sinf
  → Range highlighted on a visual bar

[Tasodifiy selected:]
  🎲  Istalgan sinfdan tasodifiy 25 ta savol
  → No selection needed, dice animation
```

---

## 12. Empty States

### No packs downloaded (fresh install)
```
  📦
  Hech qanday paket yuklanmagan

  Birinchi faningizni tanlang va
  oflayn testlarni boshlang.

  [Paketlar sahifasiga o'tish →]
```

### Pool too small for 25 questions
```
  ⚠ Savollar yetarli emas

  Matematika 1-sinf uchun faqat
  28 ta savol mavjud.

  [To'liq paketni yuklab olish]
  (Matematika 1-11-sinf Bundle — 11 MB)
```

### Offline — no packs
```
  📶 Oflayn rejim

  Paket yuklab olish uchun internet
  kerak. Yuklab olingan paketlar
  bilan ishlashingiz mumkin.

  [Yuklab olinganlarni ko'rish]
```

---

## 13. Design Tokens

### Colors
| Token | Hex | Usage |
|---|---|---|
| `primary` | `#6366F1` | Buttons, active nav, progress bars |
| `primary-dim` | `#2D2B5C` | Primary backgrounds, selected states |
| `success` | `#10B981` | Correct answers, installed packs |
| `warning` | `#F59E0B` | Weak topics, update available |
| `error` | `#EF4444` | Wrong answers, errors |
| `streak` | `#FB923C` | Streak flame, XP highlights |
| `bg-dark` | `#0D0D14` | Screen background (dark mode) |
| `card-dark` | `#1A1A26` | Card background |
| `text-primary` | `#F0F0FF` | Main text |
| `text-secondary` | `#8888A8` | Muted labels |

### Typography (Flutter)
| Role | Size | Weight |
|---|---|---|
| Screen title | 20sp | 500 |
| Section label | 11sp | 500, ALL CAPS, letter-spacing 0.6 |
| Body | 15sp | 400 |
| Question text | 16sp | 400, line-height 1.5 |
| Answer option | 14sp | 400 |
| Caption | 12sp | 400 |
| XP / score | 22–28sp | 500 |

### Spacing
`4 · 8 · 12 · 16 · 24 · 32dp`

### Border radius
`sm: 8dp  md: 12dp  lg: 16dp  pill: 24dp`
