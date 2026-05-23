package uz.testmarkaz.domain.model

enum class Subject(
    val code: String,
    val displayName: String,
    val emoji: String,
    val grades: IntRange
) {
    MATEMATIKA("matematika",     "Matematika",         "📐", 1..11),
    FIZIKA("fizika",             "Fizika",             "⚡", 7..11),
    KIMYO("kimyo",               "Kimyo",              "🧪", 7..11),
    BIOLOGIYA("biologiya",       "Biologiya",          "🌿", 5..11),
    UZBEK_TILI("uzbek_tili",     "O'zbek tili",        "📖", 1..11),
    INGLIZ_TILI("ingliz_tili",   "Ingliz tili",        "🇬🇧", 1..11),
    TARIX("tarix",               "Tarix",              "🏛️", 5..11),
    GEOGRAFIYA("geografiya",     "Geografiya",         "🌍", 6..11),
    RUS_TILI("rus_tili",         "Rus tili",           "🇷🇺", 2..11),
    INFORMATIKA("informatika",   "Informatika",        "💻", 3..11),
    FUQAROLIK("fuqarolik",       "Fuqarolik ta'limi",  "⚖️", 5..9),
    TEXNOLOGIYA("texnologiya",   "Texnologiya",        "🔧", 1..9),
    TASVIRIY_SANAT("tasviriy_sanat", "Tasviriy san'at","🎨", 1..7),
    MUSIQA("musiqa",             "Musiqa",             "🎵", 1..7),
    JISMONIY_TARBIYA("jismoniy_tarbiya", "Jismoniy tarbiya", "🏃", 1..11),
    TARBIYA("tarbiya",           "Tarbiya",            "❤️", 1..4);

    companion object {
        fun fromCode(code: String): Subject? = entries.find { it.code == code }

        /** Subjects typically included in DTM (university entrance exam) */
        val dtmSubjects = listOf(
            MATEMATIKA, FIZIKA, KIMYO, BIOLOGIYA,
            UZBEK_TILI, INGLIZ_TILI, TARIX, GEOGRAFIYA
        )
    }
}
