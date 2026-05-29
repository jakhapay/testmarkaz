package uz.testmarkaz.data.catalog

data class ContentPackInfo(
    val packKey: String,
    val subjectCode: String,
    val grade: Int?,
    val gradeMin: Int?,
    val gradeMax: Int?,
    val nameUz: String,
    val questionCount: Int,
    val driveFileId: String,
    val driveFileName: String,
    val driveFileSize: Long,
    val isPublished: Boolean,
    val version: Int
)
