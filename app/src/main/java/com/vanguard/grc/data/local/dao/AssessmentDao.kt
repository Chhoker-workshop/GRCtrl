package com.vanguard.grc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vanguard.grc.data.local.entity.AssessmentAttemptEntity
import com.vanguard.grc.data.local.entity.AssessmentResponseEntity
import com.vanguard.grc.data.local.entity.CompetencyEntity
import com.vanguard.grc.data.local.entity.QuestionStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AssessmentDao {

    // --- Attempts ---------------------------------------------------------------

    @Insert
    suspend fun insertAttempt(attempt: AssessmentAttemptEntity): Long

    @Insert
    suspend fun insertResponses(responses: List<AssessmentResponseEntity>)

    @Query("UPDATE assessment_attempts SET completedAt = :at, scorePercent = :score, correctCount = :correct, totalCount = :total, timeSpentSeconds = :seconds, passed = :passed WHERE id = :id")
    suspend fun finalizeAttempt(
        id: Long,
        at: Long,
        score: Int,
        correct: Int,
        total: Int,
        seconds: Int,
        passed: Boolean
    )

    @Query("SELECT * FROM assessment_attempts ORDER BY COALESCE(completedAt, startedAt) DESC")
    fun observeAttempts(): Flow<List<AssessmentAttemptEntity>>

    @Query("SELECT * FROM assessment_attempts WHERE id = :id")
    suspend fun getAttempt(id: Long): AssessmentAttemptEntity?

    @Query("SELECT * FROM assessment_attempts WHERE assessmentId = :assessmentId ORDER BY startedAt DESC")
    suspend fun getAttemptsFor(assessmentId: String): List<AssessmentAttemptEntity>

    @Query("SELECT * FROM assessment_responses WHERE attemptId = :attemptId ORDER BY id ASC")
    suspend fun getResponses(attemptId: Long): List<AssessmentResponseEntity>

    @Query("SELECT * FROM assessment_responses")
    fun observeAllResponses(): Flow<List<AssessmentResponseEntity>>

    @Query("SELECT * FROM assessment_responses")
    suspend fun queryAllResponses(): List<AssessmentResponseEntity>

    @Query("DELETE FROM assessment_attempts")
    suspend fun clearAttempts()

    @Query("DELETE FROM assessment_responses")
    suspend fun clearResponses()

    // --- Question statistics ----------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertQuestionStat(stat: QuestionStatEntity)

    @Query("SELECT * FROM question_stats")
    fun observeAllQuestionStats(): Flow<List<QuestionStatEntity>>

    @Query("SELECT * FROM question_stats")
    suspend fun queryAllQuestionStats(): List<QuestionStatEntity>

    @Query("SELECT * FROM question_stats WHERE questionId = :questionId")
    suspend fun getQuestionStat(questionId: String): QuestionStatEntity?

    @Query("SELECT * FROM question_stats WHERE wrongCount > 0 AND seenCount >= 2 ORDER BY (CAST(wrongCount AS REAL) / seenCount) DESC, lastSeenAt DESC LIMIT :limit")
    fun observeWeakQuestions(limit: Int): Flow<List<QuestionStatEntity>>

    @Query("SELECT * FROM question_stats WHERE nextReviewAt IS NOT NULL AND nextReviewAt <= :now ORDER BY nextReviewAt ASC LIMIT :limit")
    fun observeDueForReview(now: Long, limit: Int): Flow<List<QuestionStatEntity>>

    @Query("DELETE FROM question_stats")
    suspend fun clearQuestionStats()

    // --- Competency -------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCompetency(entity: CompetencyEntity)

    @Query("SELECT * FROM competency_scores")
    fun observeCompetencies(): Flow<List<CompetencyEntity>>

    @Query("DELETE FROM competency_scores")
    suspend fun clearCompetencies()
}
