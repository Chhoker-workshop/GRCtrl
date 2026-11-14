package com.vanguard.grc.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "assessment_attempts", indices = [Index("assessmentId"), Index("completedAt")])
data class AssessmentAttemptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val assessmentId: String,
    val title: String,
    val kind: String,
    val domain: String,
    val startedAt: Long,
    val completedAt: Long? = null,
    val scorePercent: Int? = null,
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val timeSpentSeconds: Int = 0,
    /** Pass threshold applied at scoring time, 0..100. */
    val passMark: Int = 70,
    val passed: Boolean = false
)

@Entity(
    tableName = "assessment_responses",
    indices = [Index("attemptId"), Index("questionId"), Index("domain"), Index("isCorrect")]
)
data class AssessmentResponseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val attemptId: Long,
    val questionId: String,
    val domain: String,
    val difficulty: String,
    val selectedOptionIds: String,
    val isCorrect: Boolean,
    val timeSpentSeconds: Int = 0,
    val answeredAt: Long = System.currentTimeMillis()
)

/**
 * Longitudinal statistics for every question the learner has seen. Drives
 * spaced repetition, weakness detection and the daily revision list.
 */
@Entity(tableName = "question_stats", indices = [Index("lastResultCorrect"), Index("nextReviewAt")])
data class QuestionStatEntity(
    @PrimaryKey val questionId: String,
    val domain: String,
    val topic: String,
    val seenCount: Int = 0,
    val correctCount: Int = 0,
    val wrongCount: Int = 0,
    val streak: Int = 0,
    val lastResultCorrect: Boolean? = null,
    val lastSeenAt: Long? = null,
    val nextReviewAt: Long? = null,
    val easeFactor: Double = 2.5,
    val intervalDays: Int = 0
)

/**
 * Cached competency score per competency domain. The authoritative value is
 * always recomputed from attempts and responses; this table exists so the
 * dashboard can render instantly and survive offline usage.
 */
@Entity(tableName = "competency_scores")
data class CompetencyEntity(
    @PrimaryKey val domain: String,
    val score: Int,
    val confidenceWeight: Int,
    val questionsAnswered: Int,
    val accuracy: Int,
    val updatedAt: Long = System.currentTimeMillis()
)
