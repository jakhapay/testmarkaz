# TestMarkaz — API Design

> The API is intentionally minimal. Because the app is offline-first, the API handles only four concerns: authentication, content pack delivery, progress sync, and admin content management. All test logic runs on-device.

---

## 1. Conventions

- **Base URL**: `https://api.testmarkaz.uz/api/v1`
- **Auth**: `Authorization: Bearer <JWT>` (15 min expiry). Refresh via `/auth/refresh`.
- **Response envelope**: `{ "data": {...}, "error": null, "meta": {} }`
- **Errors**: `{ "data": null, "error": { "code": "PACK_NOT_FOUND", "message": "..." } }`
- **Offline behaviour**: all endpoints must return gracefully — the app never crashes due to API unavailability. Any 5xx is silently retried on next launch.
- **Rate limits**: 60 req/min per IP, 200 req/min per authenticated user. Returned in `X-RateLimit-*` headers.

---

## 2. Authentication

| Method | Path | Description |
|---|---|---|
| POST | `/auth/register` | Phone + password, returns tokens |
| POST | `/auth/login` | Returns access + refresh tokens |
| POST | `/auth/refresh` | Rotate access token |
| POST | `/auth/logout` | Revoke refresh token |
| POST | `/auth/social/google` | Google Sign-In token exchange |
| POST | `/auth/otp/request` | Send SMS OTP via Eskiz.uz |
| POST | `/auth/otp/verify` | Verify OTP → session |
| POST | `/auth/password/reset/request` | Email reset link |
| POST | `/auth/password/reset/confirm` | Set new password |
| DELETE | `/auth/account` | Delete account + all server data |

**Register example**
```http
POST /auth/register
{
  "phone": "+998901234567",
  "password": "Secret123!",
  "full_name": "Ali Karimov",
  "grade": 9,
  "region": "TAS",
  "locale": "uz-Latn"
}
```
Response 201:
```json
{
  "data": {
    "user": { "public_id": "...", "full_name": "Ali Karimov", "grade": 9 },
    "access_token": "...",
    "refresh_token": "..."
  }
}
```

---

## 3. Content Pack Catalog

The catalog tells the app what packs exist, their versions, and sizes. The app checks this on every launch when online — it is the engine for keeping content fresh.

### GET /packs/catalog
Returns all published packs. No auth required (public endpoint).

```http
GET /api/v1/packs/catalog
Accept-Language: uz-Latn
```

Response:
```json
{
  "data": {
    "catalog_version": 47,
    "updated_at": "2026-05-20T10:00:00Z",
    "packs": [
      {
        "pack_key":       "matematika_09",
        "subject_code":   "matematika",
        "subject_name":   "Matematika",
        "grade":          9,
        "grade_min":      null,
        "grade_max":      null,
        "version":        3,
        "question_count": 850,
        "size_bytes":     1258000,
        "checksum_sha256":"a1b2c3...",
        "lang":           "uz-Latn",
        "updated_at":     "2026-05-18T14:00:00Z"
      },
      {
        "pack_key":       "bundle_matematika",
        "subject_code":   "matematika",
        "grade":          null,
        "grade_min":      1,
        "grade_max":      11,
        "version":        2,
        "question_count": 9200,
        "size_bytes":     11400000,
        ...
      }
    ]
  }
}
```

### GET /packs/catalog/diff
Returns only packs that changed since a given version. The app uses this after first install for efficient delta updates.

```http
GET /api/v1/packs/catalog/diff?since_version=44
```

Returns the same structure but only packs where `version > 44`.

### GET /packs/:pack_key/download-url
Returns a signed, short-lived download URL for the `.db` file stored in R2. Auth required for packs beyond the free tier.

```http
GET /api/v1/packs/matematika_09/download-url
Authorization: Bearer <token>
```

Response:
```json
{
  "data": {
    "url":        "https://cdn.testmarkaz.uz/packs/matematika_09_v3.db?sig=...",
    "expires_at": "2026-05-22T13:00:00Z",
    "size_bytes": 1258000,
    "checksum":   "a1b2c3..."
  }
}
```

The app then downloads directly from the CDN URL — this download never hits the API server.

### GET /packs/free-list
Returns pack keys available without an account (trial users — up to 3 packs).

```http
GET /api/v1/packs/free-list
```

---

## 4. Progress Sync

The app syncs completed test sessions to the server when online. Sync is idempotent — resending the same `local_id` is safe.

### POST /sync/progress
```http
POST /api/v1/sync/progress
Authorization: Bearer <token>
Content-Type: application/json

{
  "device_id": "550e8400-e29b-41d4-a716-446655440000",
  "app_version": "1.2.0",
  "sessions": [
    {
      "local_id":        "uuid-of-session-on-device",
      "subject_code":    "matematika",
      "grade_min":       9,
      "grade_max":       9,
      "mode":            "subject",
      "score":           18,
      "total":           25,
      "duration_seconds":420,
      "started_at":      "2026-05-22T08:30:00Z",
      "completed_at":    "2026-05-22T08:37:00Z",
      "answers": [
        {
          "question_checksum": "sha256hex...",
          "selected":          "B",
          "correct":           "C",
          "answered_at":       "2026-05-22T08:31:12Z"
        }
      ]
    }
  ]
}
```

Response 200:
```json
{
  "data": {
    "synced_count": 1,
    "skipped_count": 0,
    "server_time": "2026-05-22T09:00:00Z"
  }
}
```

### GET /sync/status
Returns last sync time and whether there are server-side updates (pack version bumps, new notifications).

```http
GET /api/v1/sync/status
Authorization: Bearer <token>
```

Response:
```json
{
  "data": {
    "last_synced_at":  "2026-05-21T18:00:00Z",
    "catalog_version": 47,
    "has_new_packs":   true,
    "notifications":   2
  }
}
```

---

## 5. User Profile

### GET /users/me
```http
GET /api/v1/users/me
Authorization: Bearer <token>
```

Response includes subscription status, grade, locale — used on fresh install to restore profile.

### PATCH /users/me
```json
{ "full_name": "...", "grade": 10, "locale": "ru", "region": "SAM" }
```

### GET /users/me/stats
Returns server-side aggregated stats (total sessions, best subject, etc.) for profile display.

---

## 6. Admin — Content Management

All admin endpoints require `role=admin` JWT claim.

### POST /admin/books/upload
Upload a source book (PDF/DOCX) for AI question generation.

```http
POST /api/v1/admin/books/upload
Authorization: Bearer <admin-token>
Content-Type: multipart/form-data

file:          <binary>
subject_code:  matematika
grade:         9
lang:          uz-Latn
source_title:  "Algebra 9-sinf"
source_author: "O.R. Haydarov"
```

Response 202 (accepted, processing async):
```json
{ "data": { "job_id": "job_abc123", "status": "queued" } }
```

### GET /admin/jobs/:job_id
Poll the status of an AI processing job.

```json
{
  "data": {
    "job_id":   "job_abc123",
    "status":   "generating_questions",   // queued|ocr|chunking|generating_questions|exporting|done|failed
    "progress": 68,
    "questions_generated": 340,
    "eta_seconds": 120
  }
}
```

### POST /admin/packs/build
Trigger rebuild of a specific content pack after new questions are approved.

```http
POST /api/v1/admin/packs/build
{ "pack_key": "matematika_09" }
```

### GET /admin/packs
List all packs with question counts and last updated times.

### PATCH /admin/questions/:checksum
Edit or approve a generated question.

```json
{
  "question_text": "...",
  "option_a": "...",
  "correct": "C",
  "explanation": "...",
  "status": "approved"   // pending|approved|rejected
}
```

---

## 7. Notifications

### GET /notifications
Returns unread notifications for the user (new pack available, streak milestone, etc.).

```json
{
  "data": {
    "notifications": [
      {
        "id": 1,
        "type": "new_pack",
        "title": "Yangi paket mavjud",
        "body":  "Fizika 9-sinf yangilandi — 120 ta yangi savol qo'shildi.",
        "pack_key": "fizika_09",
        "created_at": "2026-05-22T06:00:00Z",
        "read": false
      }
    ]
  }
}
```

### POST /notifications/:id/read
Mark a notification as read.

---

## 8. Payments (Uzbekistan)

### POST /billing/subscribe
Initiate a subscription purchase.

```json
{
  "plan": "premium",
  "provider": "click"   // click|payme|stripe
}
```

Response includes a redirect URL to the payment provider's checkout page.

### POST /billing/webhooks/click
### POST /billing/webhooks/payme
Webhook endpoints for payment provider callbacks. Both are idempotent (duplicate webhooks safe).

### GET /billing/subscription
Returns current subscription status for the authenticated user.

---

## 9. Offline Behaviour Contract

The Flutter app must handle all these cases gracefully:

| Scenario | Expected behaviour |
|---|---|
| API returns 5xx | Silent retry on next app launch; no error shown to user |
| API unreachable (no network) | App works normally from local DB; offline chip shown |
| Pack download interrupted | Resume from byte offset on next attempt (dio `Range` header) |
| Sync fails mid-session | Local sessions remain in `synced_to_server=false`; retry next launch |
| Auth token expired while offline | App stays functional; re-auth on next network connection |
| Pack checksum mismatch after download | Delete corrupt file, re-download silently |
