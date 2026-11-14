package com.vanguard.grc.data.repo

import com.vanguard.grc.data.content.ContentRepository
import com.vanguard.grc.data.content.Question
import com.vanguard.grc.data.local.dao.AssessmentDao
import com.vanguard.grc.data.local.entity.AssessmentAttemptEntity
import com.vanguard.grc.data.local.entity.AssessmentResponseEntity
import com.vanguard.grc.data.local.entity.CompetencyEntity
import com.vanguard.grc.data.local.entity.QuestionStatEntity
import com.vanguard.grc.domain.assessment.Answer
import com.vanguard.grc.domain.assessment.AssessmentEngine
import com.vanguard.grc.domain.assessment.ScoreResult
import com.vanguard.grc.domain.competency.CompetencyEngine
import com.vanguard.grc.domain.competency.CompetencySnapshot
import com.vanguard.grc.domain.srs.SpacedRepetitionScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

/**
 * Assessment attempts, question history, spaced repetition and competency.
 */
class AssessmentRepository(
    private val dao: AssessmentDao,
    private val content: ContentRepository
) {

    private val scheduler = SpacedRepetitionScheduler()

    fun attempts(): Flow<List<AssessmentAttemptEntity>> = dao.observeAttempts()

    fun allResponses(): Flow<List<AssessmentResponseEntity>> = dao.observeAllResponses()

    fun questionStats(): Flow<List<QuestionStatEntity>> = dao.observeAllQuestionStats()

    fun weakQuestions(limit: Int = 20): Flow<List<QuestionStatEntity>> = dao.observeWeakQuestions(limit)

    fun dueForReview(now: Long = System.currentTimeMillis(), limit: Int = 20): Flow<List<QuestionStatEntity>> =
        dao.observeDueForReview(now, limit)

    suspend fun getAttempt(id: Long): AssessmentAttemptEntity? = dao.getAttempt(id)

    suspend fun getResponses(attemptId: Long): List<AssessmentResponseEntity> = dao.getResponses(attemptId)

    /**
     * Starts an attempt and returns its identifier.
     */
    suspend fun startAttempt(
        assessmentId: String,
        title: String,
        kind: String,
        domain: String,
        passMark: Int = 70
    ): Long = dao.insertAttempt(
        AssessmentAttemptEntity(
            assessmentId = assessmentId,
            title = title,
            kind = kind,
            domain = domain,
            startedAt = System.currentTimeMillis(),
            passMark = passMark
        )
    )

    /**
     * Finalises an attempt: stores the responses, updates longitudinal question
     * statistics and returns the scored result.
     */
    suspend fun finaliseAttempt(
        attemptId: Long,
        questions: List<Question>,
        answers: List<Answer>,
        engine: AssessmentEngine,
        timeSpentSeconds: Int,
        passMark: Int = 70
    ): ScoreResult {
        val result = engine.score(questions, answers, passMark)

        val responses = result.graded.map { graded ->
            AssessmentResponseEntity(
                attemptId = attemptId,
                questionId = graded.question.id,
                domain = graded.question.domain,
                difficulty = graded.question.difficulty.name,
                selectedOptionIds = graded.answer.selectedOptionIds.joinToString("|"),
                isCorrect = graded.correct,
                timeSpentSeconds = graded.answer.timeSpentSeconds
            )
        }
        dao.insertResponses(responses)

        result.graded.forEach { graded ->
            val existing = dao.getQuestionStat(graded.question.id)
            val stat = existing ?: QuestionStatEntity(
                questionId = graded.question.id,
                domain = graded.question.domain,
                topic = graded.question.topic
            )
            val updated = stat.copy(
                domain = graded.question.domain,
                topic = graded.question.topic,
                seenCount = stat.seenCount + 1,
                correctCount = stat.correctCount + if (graded.correct) 1 else 0,
                wrongCount = stat.wrongCount + if (graded.correct) 0 else 1,
                streak = if (graded.correct) stat.streak + 1 else 0,
                lastResultCorrect = graded.correct,
                lastSeenAt = System.currentTimeMillis()
            )
            val schedule = scheduler.schedule(updated, graded.correct)
            dao.upsertQuestionStat(
                updated.copy(
                    nextReviewAt = schedule.nextReviewAt,
                    intervalDays = schedule.intervalDays,
                    easeFactor = schedule.easeFactor
                )
            )
        }

        dao.finalizeAttempt(
            id = attemptId,
            at = System.currentTimeMillis(),
            score = result.scorePercent,
            correct = result.correctCount,
            total = result.totalCount,
            seconds = timeSpentSeconds,
            passed = result.passed
        )

        return result
    }

    /**
     * Live competency computed from every recorded response and the
     * longitudinal question statistics.
     */
    fun competency(
        competencyEngine: CompetencyEngine,
        practicalScores: Map<String, Int> = emptyMap()
    ): Flow<CompetencySnapshot> =
        combine(dao.observeAllResponses(), dao.observeAllQuestionStats()) { responses, stats ->
            competencyEngine.compute(
                responses = responses,
                stats = stats,
                questionsById = content.index.value.questionsById,
                practicalScores = practicalScores
            )
        }

    suspend fun refreshCompetencyCache(competencyEngine: CompetencyEngine) {
        val snapshot = competencyEngine.compute(
            responses = dao.queryAllResponses(),
            stats = dao.queryAllQuestionStats(),
            questionsById = content.index.value.questionsById
        )
        snapshot.domains.forEach { domain ->
            dao.upsertCompetency(
                CompetencyEntity(
                    domain = domain.domain.id,
                    score = domain.score,
                    confidenceWeight = domain.confidence,
                    questionsAnswered = domain.questionsAnswered,
                    accuracy = domain.accuracy,
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun cachedCompetencies(): Flow<List<CompetencyEntity>> = dao.observeCompetencies()

    suspend fun clearHistory() {
        dao.clearAttempts()
        dao.clearResponses()
        dao.clearQuestionStats()
        dao.clearCompetencies()
    }
}
