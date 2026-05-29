package uz.testmarkaz.data.download

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import uz.testmarkaz.data.catalog.ContentPackInfo
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.entity.InstalledPackEntity
import uz.testmarkaz.data.db.entity.QuestionEntity
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

sealed class DownloadResult {
    object Success : DownloadResult()
    data class Error(val message: String) : DownloadResult()
}

@Singleton
class PackDownloadRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val questionDao: QuestionDao,
    private val progressDao: ProgressDao,
    private val okHttpClient: OkHttpClient
) {

    suspend fun download(pack: ContentPackInfo): DownloadResult = withContext(Dispatchers.IO) {
        val tmpFile = File(context.cacheDir, "${pack.packKey}.db")
        try {
            val ok = when {
                pack.driveFileId.isNotBlank() -> downloadFromDrive(pack.driveFileId, tmpFile)
                pack.driveFileName.isNotBlank() -> copyFromAssets(pack.driveFileName, tmpFile)
                else -> return@withContext DownloadResult.Error("No download source available")
            }
            if (!ok) return@withContext DownloadResult.Error("Failed to retrieve pack file")

            val questions = readQuestionsFromDb(tmpFile)
            if (questions.isEmpty()) return@withContext DownloadResult.Error("Pack contains no questions")
            questionDao.insertAll(questions)

            progressDao.upsertInstalledPack(
                InstalledPackEntity(
                    packKey = pack.packKey,
                    subjectCode = pack.subjectCode,
                    grade = pack.grade,
                    gradeMin = pack.gradeMin,
                    gradeMax = pack.gradeMax,
                    questionCount = questions.size,
                    version = pack.version
                )
            )
            DownloadResult.Success
        } catch (e: Exception) {
            DownloadResult.Error(e.message ?: "Unknown error")
        } finally {
            tmpFile.delete()
        }
    }

    private fun downloadFromDrive(fileId: String, dest: File): Boolean {
        val url = "https://drive.google.com/uc?export=download&id=$fileId"
        val response = okHttpClient.newCall(Request.Builder().url(url).build()).execute()
        if (!response.isSuccessful) return false
        response.body?.byteStream()?.use { input ->
            dest.outputStream().use { output -> input.copyTo(output) }
        } ?: return false
        return true
    }

    private fun copyFromAssets(fileName: String, dest: File): Boolean {
        return try {
            context.assets.open("packs/$fileName").use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun readQuestionsFromDb(file: File): List<QuestionEntity> {
        val db = SQLiteDatabase.openDatabase(file.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        val result = mutableListOf<QuestionEntity>()
        db.use {
            it.rawQuery(
                "SELECT subject,grade,topic,questionText,optionA,optionB,optionC,optionD," +
                        "correct,explanation,difficulty,sourceBook,lang,checksum,packKey FROM questions",
                null
            ).use { cursor ->
                while (cursor.moveToNext()) {
                    result += QuestionEntity(
                        subject = cursor.getString(0),
                        grade = cursor.getInt(1),
                        topic = cursor.getString(2) ?: "",
                        questionText = cursor.getString(3),
                        optionA = cursor.getString(4),
                        optionB = cursor.getString(5),
                        optionC = cursor.getString(6),
                        optionD = cursor.getString(7),
                        correct = cursor.getString(8),
                        explanation = cursor.getString(9) ?: "",
                        difficulty = cursor.getInt(10),
                        sourceBook = cursor.getString(11) ?: "",
                        lang = cursor.getString(12),
                        checksum = cursor.getString(13),
                        packKey = cursor.getString(14)
                    )
                }
            }
        }
        return result
    }
}
