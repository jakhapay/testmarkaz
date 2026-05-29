package uz.testmarkaz.data.catalog

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CatalogRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val dbFile: File get() = File(context.filesDir, "catalog.db")

    fun loadAll(): List<ContentPackInfo> {
        ensureCopied()
        val db = SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        return db.use { readPacks(it) }
    }

    private fun readPacks(db: SQLiteDatabase): List<ContentPackInfo> {
        val result = mutableListOf<ContentPackInfo>()
        db.rawQuery(
            "SELECT packKey,subjectCode,grade,gradeMin,gradeMax,nameUz,questionCount," +
                    "driveFileId,driveFileName,driveFileSize,isPublished,version FROM content_packs" +
                    " ORDER BY subjectCode, grade",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                result += ContentPackInfo(
                    packKey = cursor.getString(0),
                    subjectCode = cursor.getString(1),
                    grade = cursor.getInt(2).takeIf { !cursor.isNull(2) },
                    gradeMin = cursor.getInt(3).takeIf { !cursor.isNull(3) },
                    gradeMax = cursor.getInt(4).takeIf { !cursor.isNull(4) },
                    nameUz = cursor.getString(5),
                    questionCount = cursor.getInt(6),
                    driveFileId = cursor.getString(7) ?: "",
                    driveFileName = cursor.getString(8) ?: "",
                    driveFileSize = cursor.getLong(9),
                    isPublished = cursor.getInt(10) == 1,
                    version = cursor.getInt(11)
                )
            }
        }
        return result
    }

    private fun ensureCopied() {
        if (dbFile.exists()) return
        context.assets.open("catalog.db").use { input ->
            dbFile.outputStream().use { output -> input.copyTo(output) }
        }
    }
}
