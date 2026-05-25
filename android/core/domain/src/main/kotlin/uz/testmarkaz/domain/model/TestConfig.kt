package uz.testmarkaz.domain.model

enum class TestMode {
    SUBJECT,        // Single subject, single grade
    RANGE,          // Single subject, grade range (e.g. 7–9)
    RANDOM_CLASS,   // All subjects for one grade
    FULL_RANDOM     // Fully random from everything downloaded
}

data class TestConfig(
    val mode: TestMode,
    val subject: Subject? = null,       // null for RANDOM_CLASS / FULL_RANDOM
    val gradeMin: Int,
    val gradeMax: Int
) {
    val isSingleGrade: Boolean get() = gradeMin == gradeMax

    companion object {
        fun singleSubject(subject: Subject, grade: Int) = TestConfig(
            mode = TestMode.SUBJECT,
            subject = subject,
            gradeMin = grade,
            gradeMax = grade
        )

        fun subjectRange(subject: Subject, gradeMin: Int, gradeMax: Int) = TestConfig(
            mode = TestMode.RANGE,
            subject = subject,
            gradeMin = gradeMin,
            gradeMax = gradeMax
        )

        fun randomClass(grade: Int) = TestConfig(
            mode = TestMode.RANDOM_CLASS,
            subject = null,
            gradeMin = grade,
            gradeMax = grade
        )

        fun fullRandom() = TestConfig(
            mode = TestMode.FULL_RANDOM,
            subject = null,
            gradeMin = 1,
            gradeMax = 11
        )
    }
}
