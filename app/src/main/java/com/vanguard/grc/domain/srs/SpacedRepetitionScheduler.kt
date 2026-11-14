package com.vanguard.grc.domain.srs

import com.vanguard.grc.data.local.entity.QuestionStatEntity
import kotlin.math.max
import kotlin.math.roundToInt

/**
 * A spaced-repetition scheduler modelled on the SM-2 algorithm, simplified for
 * use with multiple-choice GRC questions.
 *
 * The scheduler deliberately prioritises two kinds of item:
 *  1. items the learner has answered incorrectly (remediation), and
 *  2. items that are due for review (retention).
 *
 * It never schedules an item the learner has never seen into the same day as a
 * long backlog, so the daily list stays realistic.
 */
class SpacedRepetitionScheduler {

    data class Schedule(
        val questionId: String,
        val nextReviewAt: Long,
        val intervalDays: Int,
        val easeFactor: Double
    )

    /**
     * Computes the next review date for a question given the outcome.
     */
    fun schedule(
        stat: QuestionStatEntity,
        correct: Boolean,
        now: Long = System.currentTimeMillis(),
        relearning: Boolean = false
    ): Schedule {
        val currentEase = if (stat.easeFactor <= 0) 2.5 else stat.easeFactor
        val streak = if (correct) stat.streak + 1 else 0

        if (relearning) {
            return Schedule(
                questionId = stat.questionId,
                nextReviewAt = now + ONE_DAY,
                intervalDays = 1,
                easeFactor = (currentEase - 0.20).coerceAtLeast(1.3)
            )
        }

        if (!correct) {
            // Relearn: reset the interval but keep a floor so the item returns tomorrow.
            val newEase = (currentEase - 0.20).coerceAtLeast(1.3)
            val interval = 1
            return Schedule(
                questionId = stat.questionId,
                nextReviewAt = now + interval * ONE_DAY,
                intervalDays = interval,
                easeFactor = newEase
            )
        }

        val interval = when {
            streak <= 1 -> 1
            streak == 2 -> 3
            streak == 3 -> 7
            else -> max(1, (stat.intervalDays * currentEase).roundToInt())
        }.coerceAtMost(180)

        val ease = (currentEase + (0.1 - (5 - streak).coerceAtLeast(0) * (0.08 + (5 - streak).coerceAtLeast(0) * 0.02)))
            .coerceIn(1.3, 2.8)

        return Schedule(
            questionId = stat.questionId,
            nextReviewAt = now + interval * ONE_DAY,
            intervalDays = interval,
            easeFactor = ease
        )
    }

    /**
     * Builds the daily revision list: due items first (oldest due date first),
     * then unreviewed items the learner previously got wrong.
     */
    fun dailyRevisionList(
        stats: List<QuestionStatEntity>,
        now: Long = System.currentTimeMillis(),
        limit: Int = 20
    ): List<QuestionStatEntity> {
        val due = stats.filter { it.nextReviewAt != null && it.nextReviewAt <= now }
            .sortedBy { it.nextReviewAt }
        val wrongAndUnseenAgain = stats.filter { it.lastResultCorrect == false && it.nextReviewAt == null }
            .sortedByDescending { it.wrongCount }
        val repeatedWeak = stats.filter { it.wrongCount > 0 && it.seenCount >= 2 && it.nextReviewAt?.let { it > now } == true }
            .sortedByDescending { it.wrongCount }

        return (due + wrongAndUnseenAgain + repeatedWeak).distinctBy { it.questionId }.take(limit)
    }

    /**
     * Produces a revision plan for the learner, grouped by competency domain,
     * prioritising domains where accuracy is weakest.
     */
    fun revisionPlan(
        stats: List<QuestionStatEntity>,
        now: Long = System.currentTimeMillis()
    ): List<Pair<String, Int>> {
        val byDomain = stats.groupBy { it.domain }
        return byDomain.mapValues { (_, list) ->
            val answered = list.sumOf { it.seenCount }
            val correct = list.sumOf { it.correctCount }
            val due = list.count { it.nextReviewAt != null && it.nextReviewAt <= now }
            val accuracy = if (answered == 0) 100 else (correct * 100 / answered)
            // Priority: due items and low accuracy rank highest.
            (due * 3) + ((100 - accuracy) / 5)
        }.entries.sortedByDescending { it.value }
            .map { it.key to it.value }
    }

    companion object {
        const val ONE_DAY: Long = 24L * 60 * 60 * 1000
    }
}
