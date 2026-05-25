package uz.testmarkaz.domain.usecase

import uz.testmarkaz.data.db.dao.ProgressDao
import uz.testmarkaz.data.db.dao.TestSessionDao
import uz.testmarkaz.data.db.entity.SessionAnswerEntity
import uz.testmarkaz.data.db.entity.TestSessionEntity
import uz.testmarkaz.data.db.entity.TopicMasteryEntity
import uz.testmarkaz.data.db.entity.UserStatsEntity
import uz.testmarkaz.data.db.entity.WrongAnswerEntity
import uz.testmarkaz.domain.model.AnswerDetail
import uz.testmarkaz.domain.model.TestResult
import uz.testmarkaz.domain.model.TestSession
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CompleteTestUseCase @Inject constructor(
    private val testSessionDao: TestSessionDao,
    private val progressDao: ProgressDao
) {

    companion object {
        private const val XP_PER_CORRECT = 10
        private const val XP_COMPLETION_BONUS = 20
        private val EPOCH_MS = TimeUnit.DAYS.toMillis(1)
    }

    suspend fun invoke(session: TestSession): TestResult {
        val completedAt = System.currentTimeMillis()
        val durationSeconds = ((completedAt - session.startedAt) / 1000).toInt()

        val answerDetails = session.questions.map { question ->
            val selected = session.answers[question.id] ?: ""
            AnswerDetail(
                question = question,
                selectedOption = selected,
                isCorrect = selected == question.correct
            )
        }

        val score = answerDetails.count { it.isCorrect }
        val xpEarned = score * XP_PER_CORRECT + XP_COMPLETION_BONUS

        // 1. Persist the session entity
        testSessionDao.insertSession(
            TestSessionEntity(
                id = session.sessionId,
                subjectCode = session.config.subject?.code,
                gradeMin = session.config.gradeMin,
                gradeMax = session.config.gradeMax,
                mode = session.config.mode.name,
                score = score,
                total = session.totalQuestions,
                durationSeconds = durationSeconds,
                startedAt = session.startedAt,
                completedAt = completedAt
            )
        )

        // 2. Persist each answer
        testSessionDao.insertAnswers(
            answerDetails.map { detail ->
                SessionAnswerEntity(
                    sessionId = session.sessionId,
                    questionChecksum = detail.question.checksum,
                    questionText = detail.question.questionText,
                    selectedOption = detail.selectedOption,
                    correctOption = detail.question.correct,
                    isCorrect = detail.isCorrect,
                    explanation = detail.question.explanation
                )
            }
        )

        // 3. Update XP + streak
        val today = completedAt / EPOCH_MS
        val stats = progressDao.getStats()
        if (stats == null) {
            progressDao.upsertStats(
                UserStatsEntity(
                    totalXp = xpEarned,
                    totalTests = 1,
                    totalCorrect = score,
                    lastTestDay = today
                )
            )
        } else {
            progressDao.addTestResult(xp = xpEarned, correct = score, day = today)
            updateStreak(stats, today)
        }

        // 4. Update topic mastery per question
        answerDetails.forEach { detail ->
            val q = detail.question
            progressDao.upsertMastery(
                TopicMasteryEntity(
                    subjectCode = q.subject,
                    grade = q.grade,
                    topic = q.topic,
                    totalAnswered = 1,
                    correctCount = if (detail.isCorrect) 1 else 0,
                    eloRating = if (detail.isCorrect) 1515 else 1485,
                    lastPracticed = completedAt
                )
            )

            // 5. Track wrong answers
            if (!detail.isCorrect) {
                progressDao.upsertWrongAnswer(
                    WrongAnswerEntity(
                        questionChecksum = q.checksum,
                        subject = q.subject,
                        grade = q.grade,
                        topic = q.topic,
                        questionText = q.questionText,
                        correctOption = q.correct,
                        explanation = q.explanation,
                        wrongCount = 1,
                        lastWrongAt = completedAt
                    )
                )
            }
        }

        return TestResult(
            sessionId = session.sessionId,
            config = session.config,
            score = score,
            total = session.totalQuestions,
            durationSeconds = durationSeconds,
            answers = answerDetails,
            completedAt = completedAt
        )
    }

    private suspend fun updateStreak(stats: UserStatsEntity, today: Long) {
        val lastDay = stats.lastTestDay ?: return
        val diff = today - lastDay
        val newStreak = when {
            diff == 1L -> stats.currentStreak + 1   // consecutive day
            diff == 0L -> stats.currentStreak        // same day, no change
            else -> 1                                 // streak broken
        }
        val longestStreak = maxOf(stats.longestStreak, newStreak)
        progressDao.upsertStats(
            stats.copy(
                currentStreak = newStreak,
                longestStreak = longestStreak,
                lastTestDay = today
            )
        )
    }
}
