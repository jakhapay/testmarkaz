# TestMarkaz — Content Onboarding & Book Pipeline

> How to go from a PDF book to a published `.db` content pack that students can download and use offline for 25-question tests. This is the founder's core operational workflow.

---

## 1. The Complete Subject & Grade Map

TestMarkaz covers the full Uzbek national school curriculum (grades 1–11). Here is every subject, which grades it covers, and whether it is a DTM exam subject.

| Code | Fan nomi | Grades | DTM | Notes |
|---|---|---|---|---|
| `matematika` | Matematika | 1–11 | ✅ | Core DTM |
| `fizika` | Fizika | 7–11 | ✅ | Core DTM |
| `kimyo` | Kimyo | 8–11 | ✅ | Core DTM |
| `biologiya` | Biologiya | 6–11 | ✅ | Core DTM |
| `uzbek_tili` | O'zbek tili va adabiyoti | 1–11 | ✅ | Core DTM |
| `ingliz_tili` | Ingliz tili | 2–11 | ✅ | Core DTM |
| `tarix` | Tarix | 5–11 | ✅ | Core DTM |
| `geografiya` | Geografiya | 6–11 | ✅ | Core DTM |
| `rus_tili` | Rus tili | 1–11 | ❌ | Russian-language schools |
| `informatika` | Informatika | 5–11 | ❌ | IT basics |
| `fuqarolik` | Fuqarolik ta'limi | 5–11 | ❌ | Civic studies |
| `texnologiya` | Texnologiya | 1–9 | ❌ | Tech / craft |
| `tasviriy_sanat` | Tasviriy san'at | 1–7 | ❌ | Fine arts |
| `musiqa` | Musiqa | 1–7 | ❌ | Music |
| `jismoniy_tarbiya` | Jismoniy tarbiya | 1–11 | ❌ | PE |
| `tarbiya` | Tarbiya | 1–4 | ❌ | Ethics / lower primary |

**Launch priority order:** Start with 8 DTM subjects for grades 9–11 → expand to grades 7–8 → grades 1–6 → non-DTM subjects.

---

## 2. Content Pack Target Matrix

At launch (Week 14), have these packs published:

| Subject | Grades covered | Min questions/grade | Total questions |
|---|---|---|---|
| Matematika | 9, 10, 11 | 500 | 1,500 |
| Fizika | 9, 10, 11 | 400 | 1,200 |
| Kimyo | 9, 10, 11 | 400 | 1,200 |
| Biologiya | 9, 10, 11 | 400 | 1,200 |
| O'zbek tili | 9, 10, 11 | 400 | 1,200 |
| Ingliz tili | 9, 10, 11 | 400 | 1,200 |
| Tarix | 9, 10, 11 | 300 | 900 |
| Geografiya | 9, 10, 11 | 300 | 900 |
| **Total** | **24 packs** | | **~9,300 questions** |

500+ questions per pack means a student can take 20+ unique 25-question tests before seeing significant repetition.

---

## 3. Book Acquisition

### 3.1 Free source — Ministry of Education (priority #1)

**URL:** `https://kitob.edu.uz` and `https://maktab.uz`

All state-published school textbooks are free to use (publicly funded). Download PDFs directly — no permissions or fees.

**What to download:**
```
For each subject above, download:
  grade_9_<subject>.pdf
  grade_10_<subject>.pdf
  grade_11_<subject>.pdf

Example:
  https://kitob.edu.uz/matematika_9_sinf.pdf
  https://kitob.edu.uz/fizika_10_sinf.pdf
```

Organize locally:
```
~/testmarkaz_books/
├── matematika/
│   ├── grade_09.pdf
│   ├── grade_10.pdf
│   └── grade_11.pdf
├── fizika/
│   ├── grade_09.pdf
...
```

### 3.2 Supplementary sources

- **DTM practice tests (2022–2024):** Available on `https://dtm.uz/tests` — download official past exam PDFs. These are public government documents.
- **Teacher-made question banks:** Reach out to 2–3 teachers in each subject. Offer free Premium for 1 year in exchange for 100 reviewed questions.
- **Publisher agreements (Phase 2):** Contact Moliya, Navruz publishers for licensed digital copies. Offer 5% revenue share.

---

## 4. Pipeline Step by Step

The pipeline is 6 scripts run in order. One command (`process_book.sh`) chains them all.

### Step 1 — OCR (if PDF is scanned)
```bash
python tools/pipeline/01_ocr.py \
  --input ~/testmarkaz_books/matematika/grade_09.pdf \
  --output /tmp/matematika_09_raw.txt \
  --lang uz+rus+eng
```

OCR uses Tesseract 5. For digital PDFs (text layer present), this step is skipped — text is extracted directly.

**Quality check:** OCR outputs a confidence score per page. Pages below 70% are logged. Review these manually before proceeding.

### Step 2 — Chunk
```bash
python tools/pipeline/02_chunk.py \
  --input /tmp/matematika_09_raw.txt \
  --output /tmp/matematika_09_chunks.jsonl \
  --max-tokens 800 \
  --overlap 100
```

Output: JSONL where each line is `{"text": "...", "chapter": "7-bob", "page_start": 142, "page_end": 148}`.

### Step 3 — Generate questions
```bash
python tools/pipeline/03_generate.py \
  --input /tmp/matematika_09_chunks.jsonl \
  --output /tmp/matematika_09_questions_raw.jsonl \
  --subject matematika \
  --grade 9 \
  --lang uz-Latn \
  --questions-per-chunk 8 \
  --model gpt-4o-mini
```

Each chunk produces ~8 MCQ questions (4 options, correct answer, explanation). Cost: ~$0.002 per chunk, ~$0.50–1.00 per book.

Output per question:
```json
{
  "question_text": "sin²α + cos²α = ?",
  "option_a": "0",
  "option_b": "-1",
  "option_c": "1",
  "option_d": "2",
  "correct": "C",
  "explanation": "Pifagor trigonometrik tengligiga ko'ra...",
  "topic": "Trigonometriya",
  "difficulty": 2,
  "source_page": 156
}
```

### Step 4 — Validate & deduplicate
```bash
python tools/pipeline/04_validate.py \
  --input /tmp/matematika_09_questions_raw.jsonl \
  --output /tmp/matematika_09_questions_valid.jsonl
```

Validation rules:
- All required fields present
- `correct` is one of A/B/C/D
- `option_{correct.lower()}` is not empty
- Not a duplicate of existing questions (SHA256 checksum dedup)
- Auto-reject: questions with `?` in correct answer, questions shorter than 10 chars

Outputs a validation report: how many passed, failed, and why.

### Step 5 — Human review
```bash
python tools/pipeline/05_review_ui.py \
  --input /tmp/matematika_09_questions_valid.jsonl \
  --output /tmp/matematika_09_questions_approved.jsonl
```

Terminal UI that shows each question with options and highlights the claimed correct answer. Reviewer presses:
- `Enter` → approve
- `e` → edit question text / correct answer
- `d` → delete (reject)
- `s` → skip (review later)

**Review time:** ~1–2 minutes per 100 questions once you have a rhythm. A 500-question pack takes ~1–2 hours to review.

**Quality standard:**
- Factually correct answer
- Clear, unambiguous question
- Explanation makes sense

### Step 6 — Export to SQLite pack
```bash
python tools/pipeline/06_export_pack.py \
  --input /tmp/matematika_09_questions_approved.jsonl \
  --output ~/packs/matematika_09.db \
  --subject matematika \
  --grade 9 \
  --lang uz-Latn
```

Produces a `.db` file ready to distribute. Verifies:
- Schema matches expected structure
- All checksums unique
- Minimum question count met

### One-command pipeline
```bash
./tools/scripts/process_book.sh \
  --file ~/testmarkaz_books/matematika/grade_09.pdf \
  --subject matematika \
  --grade 9 \
  --lang uz-Latn \
  --title "Algebra 9-sinf" \
  --author "O.R. Haydarov"
# Interactive: pauses at review step, then continues
```

---

## 5. Upload to R2 & Publish

After the `.db` file is generated and reviewed:

```bash
python tools/upload/upload_to_r2.py \
  --pack-key matematika_09 \
  --file ~/packs/matematika_09.db \
  --subject matematika \
  --grade 9
```

This script:
1. Computes SHA256 checksum of the `.db` file
2. Uploads to R2 at `/packs/matematika_09_v{N}.db`
3. Updates `content_packs` row in Postgres: version++, question_count, size_bytes, checksum, is_published=true
4. Invalidates Cloudflare cache for the catalog endpoint
5. Prints a summary: "matematika_09 v3 published — 850 questions, 1.2 MB"

**After upload:** All app users who are online see the new/updated pack in the catalog on their next app launch.

---

## 6. Quality Standards

### Minimum questions per pack
| Grade range | Min questions | Rationale |
|---|---|---|
| Grades 9–11 (DTM) | 500/grade | 20+ unique tests before repetition |
| Grades 7–8 | 300/grade | 12+ unique tests |
| Grades 1–6 | 200/grade | 8+ unique tests |

### Required question quality
- Correct answer accuracy: > 98% (no factual errors allowed post-review)
- Explanation present: > 80% of questions
- Clear phrasing: tested by reading aloud — if it sounds ambiguous, rewrite it
- Difficulty distribution: roughly 40% easy / 40% medium / 20% hard per pack

### Languages
All launch packs are in `uz-Latn` (Uzbek Latin script). Add `uz-Cyrl` and `ru` versions as separate packs in Phase 2 when resources allow.

---

## 7. Pre-Launch Checklist

- [ ] 24 packs published (grades 9–11, 8 DTM subjects)
- [ ] Each pack: ≥ 500 questions
- [ ] Each pack: > 80% questions have explanations
- [ ] Spot-check: randomly reviewed 20 questions per subject — no factual errors
- [ ] All checksums unique within and across packs
- [ ] Pack catalog endpoint returns all 24 packs
- [ ] Test download + import on a physical Android device
- [ ] Take a 25-question test from each subject — all work offline
- [ ] Question text displays correctly in Uzbek Latin on device
- [ ] Math expressions render correctly (plain text LaTeX notation is acceptable for Phase 1)

---

## 8. Ongoing Content Operations

### Weekly (30 min)
- Check for validation failures in pipeline logs
- Review any questions flagged by users ("Bu savol xato" button in app)
- Monitor Telegram for pack upload notifications

### Monthly (2–4 hours)
- Process 2–3 new books → generate new packs or top-up existing packs
- Review question reports from users (wrong answers, bad phrasing)
- Check pack usage analytics: which packs most downloaded, which least
- Run `tools/scripts/stats.py` to see question counts per pack

### Version bump process
When adding questions to an existing pack:
1. Export updated `.db` with new questions merged in
2. Run `upload_to_r2.py` → version increments automatically
3. App users see "↑ Yangilash mavjud" on the Downloads screen

---

## 9. Estimated Pipeline Time per Book

| Book type | OCR | Generate | Review | Export | Total |
|---|---|---|---|---|---|
| Digital PDF, clean text | 0 min | 15 min | 60 min | 2 min | ~1.5 hrs |
| Scanned PDF, good quality | 10 min | 15 min | 75 min | 2 min | ~1.75 hrs |
| Scanned PDF, poor quality | 45 min | 15 min | 120 min | 2 min | ~3 hrs |

**To publish 24 packs for launch:** 24 books × ~1.75 hrs average = ~42 hours total. Spread over Weeks 3–4 (10 days), this is ~4 hours/day — very achievable alongside development work.
