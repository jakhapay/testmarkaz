package uz.testmarkaz.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import uz.testmarkaz.data.db.AppDatabase
import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.dao.QuestionDao
import uz.testmarkaz.data.db.dao.TestSessionDao
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "testmarkaz.db"
        )
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideQuestionDao(db: AppDatabase): QuestionDao = db.questionDao()

    @Provides
    fun provideTestSessionDao(db: AppDatabase): TestSessionDao = db.testSessionDao()

    @Provides
    fun provideProgressDao(db: AppDatabase): ProgressDao = db.progressDao()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()
}
