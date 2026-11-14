package com.vanguard.grc.domain.srs

import com.vanguard.grc.data.local.entity.QuestionStatEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SpacedRepetitionSchedulerTest {

    private val scheduler = SpacedRepetitionScheduler()
    private val now = 1_700_000_000_000L

    private fun stat(
        id: String = "q1",
        domain: String = "GOVERNANCE",
        streak: Int = 0,
        intervalDays: Int = 0,
        easeFactor: Double = 2.5,
        wrongCount: Int = 0,
        seenCount: Int = 1,
        lastResultCorrect: Boolean? = null,
        nextReviewAt: Long? = null
    ) = QuestionStatEntity(
        questionId = id,
        domain = domain,
        topic = "Topic",
        seenCount = seenCount,
        correctCount = seenCount - wrongCount,
        wrongCount = wrongCount,
        streak = streak,
        lastResultCorrect = lastResultCorrect,
        nextReviewAt = nextReviewAt,
        easeFactor = easeFactor,
        intervalDays = intervalDays
    )

    @Test
    fun `an incorrect answer returns the item to tomorrow`() {
        val schedule = scheduler.schedule(stat(), correct = false, now = now)
        assertEquals(1, schedule.intervalDays)
        assertEquals(now + SpacedRepetitionScheduler.ONE_DAY, schedule.nextReviewAt)
    }

    @Test
    fun `the first correct answer schedules the item for one day`() {
        val schedule = scheduler.schedule(stat(), correct = true, now = now)
        assertEquals(1, schedule.intervalDays)
    }

    @Test
    fun `the second consecutive correct answer schedules three days out`() {
        val schedule = scheduler.schedule(stat(streak = 1), correct = true, now = now)
        assertEquals(3, schedule.intervalDays)
    }

    @Test
    fun `the third consecutive correct answer schedules one week out`() {
        val schedule = scheduler.schedule(stat(streak = 2), correct = true, now = now)
        assertEquals(7, schedule.intervalDays)
    }

    @Test
    fun `the ease factor decreases after an incorrect answer but never below the floor`() {
        val schedule = scheduler.schedule(stat(easeFactor = 1.35), correct = false, now = now)
        assertTrue(schedule.easeFactor >= 1.3)
    }

    @Test
    fun `the ease factor is capped at the upper bound`() {
        var schedule = scheduler.schedule(stat(easeFactor = 2.75, streak = 10, intervalDays = 30), correct = true, now = now)
        assertTrue(schedule.easeFactor <= 2.8)
    }

    @Test
    fun `the interval never exceeds the maximum`() {
        val schedule = scheduler.schedule(
            stat(streak = 30, intervalDays = 365, easeFactor = 2.8),
            correct = true,
            now = now
        )
        assertTrue(schedule.intervalDays <= 180)
    }

    @Test
    fun `the daily revision list puts due items first`() {
        val stats = listOf(
            stat(id = "due", nextReviewAt = now - 1000, lastResultCorrect = false, wrongCount = 2, seenCount = 3),
            stat(id = "notdue", nextReviewAt = now + 100_000, lastResultCorrect = true)
        )
        val list = scheduler.dailyRevisionList(stats, now = now)
        assertTrue(list.isNotEmpty())
        assertEquals("due", list.first().questionId)
    }

    @Test
    fun `the daily revision list includes never-reviewed incorrect items`() {
        val stats = listOf(
            stat(id = "wrongunseen", wrongCount = 3, seenCount = 3, lastResultCorrect = false, nextReviewAt = null)
        )
        val list = scheduler.dailyRevisionList(stats, now = now)
        assertEquals("wrongunseen", list.first().questionId)
    }

    @Test
    fun `the daily revision list respects the limit`() {
        val stats = (1..50).map { stat(id = "q$it", nextReviewAt = now - 1000) }
        assertEquals(20, scheduler.dailyRevisionList(stats, now = now, limit = 20).size)
    }

    @Test
    fun `the revision plan prioritises weak domains`() {
        val stats = listOf(
            stat(id = "a", domain = "AUDIT", wrongCount = 8, seenCount = 10, nextReviewAt = now - 1),
            stat(id = "b", domain = "PRIVACY", wrongCount = 0, seenCount = 10, nextReviewAt = now + 100_000)
        )
        val plan = scheduler.revisionPlan(stats, now = now)
        assertEquals("AUDIT", plan.first().first)
    }
}
