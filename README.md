# TestMarkaz 📚

**O'zbekiston maktab o'quvchilari va DTM abituriyentlari uchun oflayn test platformasi**

---

## Loyiha haqida

TestMarkaz — internet ulanishisiz ishlaydi. Savollar qurilmaga yuklab olinadi va to'liq oflayn rejimda ishlaydi. Har bir test aniq **25 ta savol**dan iborat.

## Asosiy xususiyatlar

- 📶 **Oflayn-first** — internet yo'q bo'lsa ham to'liq ishlaydi
- 📝 **25 ta savol** — har doim aniq shu miqdor
- 🎯 **4 xil test rejimi** — fan, sinf diapazoni, tasodifiy sinf, to'liq tasodifiy
- 🏫 **16 ta fan** — barcha maktab fanlari (1–11 sinf)
- 🏆 **DTM tayyorgarlik** — 9–11 sinf paketlari
- 📊 **Progress kuzatuvi** — XP, streak, zaif mavzular

## Texnologiyalar

- **Kotlin** + Jetpack Compose + Material 3
- **Room 2.6.1** (oflayn SQLite)
- **Hilt** (dependency injection)
- **Navigation Compose**
- **Coroutines + Flow**

## Tuzilma

```
TestMarkaz/
├── android/          # Native Kotlin Android app
│   └── app/src/main/kotlin/uz/testmarkaz/
│       ├── data/     # Room DB, entities, DAOs, MockDataSeeder
│       ├── domain/   # Models, UseCases
│       ├── di/       # Hilt modules
│       └── ui/       # Compose screens + ViewModels
└── docs/             # Architecture, API, UI/UX, roadmap docs
```

## Ishga tushurish

1. Android Studio-da `android/` papkasini oching
2. `./gradlew assembleDebug` yoki IDE dan Run tugmasini bosing
3. Ilova ishga tushganda **150+ mock savol** avtomatik yuklanadi

## Mock ma'lumotlar

Hozircha `MockDataSeeder.kt` orqali 150+ o'zbek tilli savol seed qilinadi:
- Matematika (9, 10, 11-sinf)
- Fizika, Kimyo, Biologiya (9-sinf)
- Ingliz tili, O'zbek tili, Tarix, Geografiya (9-sinf)

Kitoblar kelgach, `MockDataSeeder.kt` haqiqiy savollar bilan almashtiriladi.

## Hujjatlar

`docs/` papkasida barcha arxitektura, API, UI/UX va yo'l xaritalari mavjud.

---

*TestMarkaz MVP v1.0 — 2024*
