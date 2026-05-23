# TestMarkaz — CI/CD & Deployment

> From commit to production. The app is Flutter (mobile), the backend is a minimal Ktor sync API on Hetzner, and content packs are static `.db` files on Cloudflare R2. Each piece deploys independently.

---

## 1. Infrastructure Overview

```
GitHub (source of truth)
   │
   ├── Push → tests run (CI)
   └── Merge to main → deploy to staging → manual approval → production

                    Hetzner CX32 VPS (Ubuntu 22.04)
                    ┌──────────────────────────────┐
                    │  Caddy (reverse proxy + TLS) │
                    │  └── api.testmarkaz.uz:8080  │
                    │                              │
                    │  Docker Compose              │
                    │  ├── ktor-api      (:8080)   │
                    │  └── redis         (:6379)   │
                    │                              │
                    │  Supabase Postgres (managed) │
                    └──────────────────────────────┘

                    Cloudflare R2 (content packs)
                    ┌──────────────────────────────┐
                    │  /packs/matematika_09.db     │
                    │  /packs/fizika_10.db         │
                    │  /packs/catalog.json (cache) │
                    │  ...                         │
                    │  Served via CDN edge         │
                    │  Zero egress cost            │
                    └──────────────────────────────┘
```

**Key difference from a typical web app:** Content packs are static files served from R2/CDN. They never hit the API server. This means 100k users downloading packs costs ~$1/month (R2 ops), not hundreds of dollars in API server egress.

---

## 2. Repositories

```
testmarkaz/app    → Flutter (mobile)
testmarkaz/api    → Kotlin Ktor (sync API)
testmarkaz/tools  → Python (AI pipeline — not deployed, runs locally)
testmarkaz/infra  → Docker Compose, Caddy, deploy scripts
```

---

## 3. GitHub Actions — Flutter App

### CI (test + build on every push)
```yaml
# app/.github/workflows/ci.yml
name: Flutter CI

on:
  push:
    branches: ['**']
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: subosito/flutter-action@v2
        with: { flutter-version: '3.22.0', cache: true }
      - run: flutter pub get
      - run: flutter analyze --fatal-infos
      - run: flutter test --coverage
      - name: Enforce test_engine coverage ≥ 90%
        run: dart run scripts/check_coverage.dart --module test_engine --min 90

  build-android:
    runs-on: ubuntu-latest
    needs: test
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '17', distribution: temurin }
      - uses: subosito/flutter-action@v2
        with: { flutter-version: '3.22.0', cache: true }
      - name: Decode keystore
        run: echo "${{ secrets.KEYSTORE_B64 }}" | base64 -d > android/app/keystore.jks
      - name: Build release APK + AAB
        run: |
          flutter build apk --release --flavor production
          flutter build appbundle --release --flavor production
        env:
          KEY_ALIAS:      ${{ secrets.KEY_ALIAS }}
          KEY_PASSWORD:   ${{ secrets.KEY_PASSWORD }}
          STORE_PASSWORD: ${{ secrets.STORE_PASSWORD }}
      - uses: actions/upload-artifact@v4
        with:
          name: android-release-${{ github.sha }}
          path: |
            build/app/outputs/flutter-apk/app-production-release.apk
            build/app/outputs/bundle/productionRelease/app-production-release.aab

  build-ios:
    runs-on: macos-14
    needs: test
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - uses: subosito/flutter-action@v2
        with: { flutter-version: '3.22.0', cache: true }
      - name: Install certs
        run: |
          echo "${{ secrets.IOS_CERT_P12_B64 }}" | base64 -d > cert.p12
          # ... keychain setup
      - run: flutter build ipa --release
      - uses: actions/upload-artifact@v4
        with:
          name: ios-release-${{ github.sha }}
          path: build/ios/ipa/testmarkaz.ipa
```

---

## 4. GitHub Actions — Ktor API

```yaml
# api/.github/workflows/ci.yml
name: API CI

on:
  push:
    branches: ['**']
  pull_request:
    branches: [main]

jobs:
  test:
    runs-on: ubuntu-latest
    services:
      postgres:
        image: postgres:16-alpine
        env: { POSTGRES_DB: tm_test, POSTGRES_USER: tm, POSTGRES_PASSWORD: tm }
        ports: ['5432:5432']
        options: --health-cmd pg_isready
      redis:
        image: redis:7-alpine
        ports: ['6379:6379']
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: temurin, cache: gradle }
      - run: ./gradlew test
        env:
          DATABASE_URL: jdbc:postgresql://localhost:5432/tm_test
          REDIS_URL: redis://localhost:6379
      - uses: actions/upload-artifact@v4
        if: failure()
        with:
          name: test-report
          path: '**/build/reports/tests/'

  build-and-push:
    runs-on: ubuntu-latest
    needs: test
    if: github.ref == 'refs/heads/main'
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with: { java-version: '21', distribution: temurin, cache: gradle }
      - run: ./gradlew app:shadowJar
      - name: Build + push Docker image
        run: |
          echo "${{ secrets.GITHUB_TOKEN }}" | docker login ghcr.io -u ${{ github.actor }} --password-stdin
          docker build -t ghcr.io/testmarkaz/api:${{ github.sha }} .
          docker tag  ghcr.io/testmarkaz/api:${{ github.sha }} ghcr.io/testmarkaz/api:latest
          docker push ghcr.io/testmarkaz/api:${{ github.sha }}
          docker push ghcr.io/testmarkaz/api:latest
```

---

## 5. Content Pack CI Pipeline ⭐ (New)

When the founder runs the Python pipeline locally and uploads a new `.db` pack, a GitHub Action validates and publishes it.

```yaml
# infra/.github/workflows/publish-pack.yml
name: Publish Content Pack

on:
  workflow_dispatch:
    inputs:
      pack_key:
        description: 'Pack key to publish (e.g. matematika_09)'
        required: true
      db_path:
        description: 'Local path to .db file (uploaded as artifact)'
        required: false

jobs:
  validate-and-publish:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
        with: { repository: testmarkaz/tools }

      - uses: actions/setup-python@v5
        with: { python-version: '3.12' }
      - run: pip install -r requirements.txt --break-system-packages

      - name: Download pack artifact (if uploaded)
        if: ${{ github.event.inputs.db_path != '' }}
        uses: actions/download-artifact@v4

      - name: Validate pack schema and content
        run: |
          python tools/scripts/validate_pack.py \
            --pack ${{ github.event.inputs.pack_key }}.db \
            --min-questions 200 \
            --require-explanations 80   # 80% of questions must have explanations

      - name: Upload to R2
        run: |
          python tools/upload/upload_to_r2.py \
            --pack-key ${{ github.event.inputs.pack_key }} \
            --file ${{ github.event.inputs.pack_key }}.db
        env:
          R2_ACCOUNT_ID: ${{ secrets.R2_ACCOUNT_ID }}
          R2_ACCESS_KEY: ${{ secrets.R2_ACCESS_KEY }}
          R2_SECRET_KEY: ${{ secrets.R2_SECRET_KEY }}
          DATABASE_URL:  ${{ secrets.PROD_DATABASE_URL }}

      - name: Notify Telegram
        run: |
          curl -s -X POST "https://api.telegram.org/bot${{ secrets.TG_BOT_TOKEN }}/sendMessage" \
            -d chat_id="${{ secrets.TG_CHAT_ID }}" \
            -d text="📦 Pack published: ${{ github.event.inputs.pack_key }}"
```

**Pack validation checks (validate_pack.py):**
- Required columns exist in SQLite schema
- No NULL `question_text`, `correct`, or `checksum`
- All checksums are unique (no duplicates)
- `correct` is always one of A/B/C/D
- Minimum question count met (configurable per grade)
- At least 80% of questions have a non-empty explanation

---

## 6. Docker Compose — Production

```yaml
# infra/docker-compose.yml
version: '3.9'

services:
  api:
    image: ghcr.io/testmarkaz/api:${IMAGE_TAG:-latest}
    restart: unless-stopped
    ports:
      - "127.0.0.1:8080:8080"
    environment:
      DATABASE_URL:     ${DATABASE_URL}
      DATABASE_USER:    ${DATABASE_USER}
      DATABASE_PASS:    ${DATABASE_PASS}
      REDIS_URL:        redis://redis:6379
      R2_ACCOUNT_ID:    ${R2_ACCOUNT_ID}
      R2_ACCESS_KEY:    ${R2_ACCESS_KEY}
      R2_SECRET_KEY:    ${R2_SECRET_KEY}
      R2_BUCKET:        testmarkaz-packs
      JWT_PRIVATE_KEY:  ${JWT_PRIVATE_KEY}
      JWT_PUBLIC_KEY:   ${JWT_PUBLIC_KEY}
      CLICK_SECRET_KEY: ${CLICK_SECRET_KEY}
      PAYME_KEY:        ${PAYME_KEY}
      ESKIZ_EMAIL:      ${ESKIZ_EMAIL}
      ESKIZ_PASSWORD:   ${ESKIZ_PASSWORD}
      SENTRY_DSN:       ${SENTRY_DSN}
      APP_ENV:          production
    depends_on:
      redis: { condition: service_healthy }
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/health"]
      interval: 30s
      timeout: 10s
      retries: 3
    deploy:
      resources:
        limits: { memory: 1g }   # API is lightweight — 1 GB is enough

  redis:
    image: redis:7-alpine
    restart: unless-stopped
    command: redis-server --save 60 1 --loglevel warning
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 5s
      retries: 3

volumes:
  redis_data:
```

Note: No Qdrant, no Meilisearch, no vector DB. The offline-first architecture eliminates the need for these in Phase 1 — all search and retrieval happens in SQLite on-device.

---

## 7. Caddy Configuration

```caddyfile
{
  email ops@testmarkaz.uz
  admin off
}

api.testmarkaz.uz {
  encode gzip

  header {
    Strict-Transport-Security "max-age=31536000; includeSubDomains"
    X-Content-Type-Options "nosniff"
    -Server
  }

  reverse_proxy localhost:8080 {
    health_uri /health
    health_interval 30s
  }
}
```

The web app (`testmarkaz.uz`) is not needed in Phase 1 — the product is mobile-only. Add web app Caddy config in Phase 3.

---

## 8. VPS Setup (Runbook)

```bash
# Fresh Hetzner CX32, Ubuntu 22.04
ssh root@<server-ip>

# Create deploy user
adduser deploy && usermod -aG sudo deploy
mkdir -p /home/deploy/.ssh
echo "<your-public-key>" >> /home/deploy/.ssh/authorized_keys
chmod 700 /home/deploy/.ssh && chmod 600 /home/deploy/.ssh/authorized_keys

# Harden SSH
sed -i 's/PermitRootLogin yes/PermitRootLogin no/' /etc/ssh/sshd_config
sed -i 's/#PasswordAuthentication yes/PasswordAuthentication no/' /etc/ssh/sshd_config
systemctl restart sshd

# Firewall
ufw allow 22/tcp && ufw allow 80/tcp && ufw allow 443/tcp && ufw --force enable

# Docker
curl -fsSL https://get.docker.com | sh
usermod -aG docker deploy

# Caddy
apt install -y debian-keyring apt-transport-https curl
# ... (standard Caddy apt install)

# App directory
mkdir -p /opt/testmarkaz && chown deploy:deploy /opt/testmarkaz
cd /opt/testmarkaz

# Create .env (use Doppler in practice)
cat > .env << 'EOF'
DATABASE_URL=postgresql://...
# ... all secrets
EOF
chmod 600 .env

docker compose pull && docker compose up -d
```

---

## 9. Deploying a New API Version

```bash
# Manual deploy to production (triggered from GitHub Actions with approval)
ssh deploy@<prod-ip>
cd /opt/testmarkaz
export IMAGE_TAG=<git-sha>
docker compose pull api
docker compose up -d --no-deps api
# Caddy health check routes to new container after /health returns 200
echo "Deployed $IMAGE_TAG"
```

---

## 10. Secrets Management

Use [Doppler](https://doppler.com) (free tier) for all secrets:

```bash
doppler setup --project testmarkaz --config production
doppler run -- docker compose up -d
```

GitHub Secrets needed for CI:
```
STAGING_HOST, STAGING_SSH_KEY
PROD_HOST, PROD_SSH_KEY
KEYSTORE_B64, KEY_ALIAS, KEY_PASSWORD, STORE_PASSWORD
IOS_CERT_P12_B64
R2_ACCOUNT_ID, R2_ACCESS_KEY, R2_SECRET_KEY
PROD_DATABASE_URL
TG_BOT_TOKEN, TG_CHAT_ID
```

---

## 11. Monitoring

- **UptimeRobot** (free): monitor `https://api.testmarkaz.uz/health` → Telegram alert if down
- **Sentry**: API errors + Flutter crash reports → Telegram alert on spike
- **Grafana Cloud free tier**: API request rate, memory/CPU on VPS
- **Custom Telegram bot**: deploys, pack publishes, daily active user count

---

## 12. Backups

The most important backup is the Postgres database (user accounts, session history, pack catalog). Supabase Pro includes daily automated backups. Additionally:

```bash
# Weekly manual Postgres dump (cron: 0 2 * * 0)
pg_dump $DATABASE_URL | gzip > /opt/backups/postgres-$(date +%Y%m%d).sql.gz
# Upload to R2 backup bucket
rclone copy /opt/backups r2:testmarkaz-backups/
```

Content packs (`.db` files on R2) are the output of the pipeline — they can be regenerated from the question bank in Postgres. No separate backup needed.
