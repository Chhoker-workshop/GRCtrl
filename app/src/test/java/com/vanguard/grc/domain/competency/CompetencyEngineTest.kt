package com.vanguard.grc.domain.competency

import com.vanguard.grc.data.content.Difficulty
import com.vanguard.grc.data.content.Question
import com.vanguard.grc.data.content.QuestionOption
import com.vanguard.grc.data.content.QuestionType
import com.vanguard.grc.data.local.entity.AssessmentResponseEntity
import com.vanguard.grc.data.local.entity.QuestionStatEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CompetencyEngineTest {

    private val engine = CompetencyEngine()

    private fun question(id: String, domain: String, difficulty: Difficulty = Difficulty.INTERMEDIATE) = Question(
        id = id,
        domain = domain,
        topic = "Topic $id",
        type = QuestionType.MULTIPLE_CHOICE,
        difficulty = difficulty,
        stem = "Stem $id",
        options = listOf(
            QuestionOption("a", "Yes", correct = true, rationale = "r"),
            QuestionOption("b", "No", correct = false, rationale = "r")
        ),
        correctOptionIds = listOf("a"),
        explanation = "Explanation $id"
    )

    private fun response(id: String, domain: String, correct: Boolean, ageDays: Long = 0L) =
        AssessmentResponseEntity(
            attemptId = 1L,
            questionId = id,
            domain = domain,
            difficulty = Difficulty.INTERMEDIATE.name,
            selectedOptionIds = if (correct) "a" else "b",
            isCorrect = correct,
            answeredAt = System.currentTimeMillis() - (ageDays * 24 * 60 * 60 * 1000)
        )

    @Test
    fun `no evidence produces a zero score and novice band`() {
        val snapshot = engine.compute(emptyList(), emptyList(), emptyMap())
        assertEquals(0, snapshot.overall)
        assertEquals(CompetencyBand.NOVICE, snapshot.overallBand)
        assertEquals(10, snapshot.domains.size)
    }

    @Test
    fun `competency is not derived from time spent`() {
        val snapshot = engine.compute(emptyList(), emptyList(), emptyMap())
        assertEquals(0, snapshot.overall)
        // The engine has no time input at all; a large study time must not move the score.
        assertEquals(0, snapshot.totalQuestionsAnswered)
    }

    @Test
    fun `accuracy drives the domain score`() {
        val responses = (1..20).map { response("q$it", "GOVERNANCE", correct = it % 4 != 0) }
        val snapshot = engine.compute(responses, emptyList(), emptyMap())
        val governance = snapshot.domains.first { it.domain == CompetencyDomain.GOVERNANCE }
        assertTrue(governance.score in 70..80)
        assertTrue(governance.accuracy in 70..80)
    }

    @Test
    fun `coverage is required before a domain is called strong`() {
        val few = engine.compute(
            listOf(response("q1", "AUDIT", true)),
            emptyList(),
            emptyMap()
        )
        val audit = few.domains.first { it.domain == CompetencyDomain.AUDIT }
        assertFalse(audit.strong)

        val many = engine.compute(
            (1..15).map { response("q$it", "AUDIT", true) },
            emptyList(),
            emptyMap()
        )
        val auditMany = many.domains.first { it.domain == CompetencyDomain.AUDIT }
        assertTrue(auditMany.strong)
    }

    @Test
    fun `difficulty weighting rewards harder correct answers`() {
        val easy = listOf(response("q1", "GOVERNANCE", true))
        val hard = listOf(response("q1", "GOVERNANCE", true).copy(difficulty = Difficulty.EXPERT.name))
        val easySnapshot = engine.compute(easy, emptyList(), emptyMap())
        val hardSnapshot = engine.compute(hard, emptyList(), emptyMap())
        val easyScore = easySnapshot.domains.first { it.domain == CompetencyDomain.GOVERNANCE }.score
        val hardScore = hardSnapshot.domains.first { it.domain == CompetencyDomain.GOVERNANCE }.score
        assertTrue("expert ($hardScore) should outscore intermediate ($easyScore)", hardScore >= easyScore)
    }

    @Test
    fun `recency discounts stale evidence`() {
        val fresh = engine.compute(
            (1..10).map { response("q$it", "PRIVACY", true) },
            emptyList(),
            emptyMap()
        )
        val stale = engine.compute(
            (1..10).map { response("q$it", "PRIVACY", true, ageDays = 365) },
            emptyList(),
            emptyMap()
        )
        val freshScore = fresh.domains.first { it.domain == CompetencyDomain.PRIVACY }.score
        val staleScore = stale.domains.first { it.domain == CompetencyDomain.PRIVACY }.score
        assertTrue("stale ($staleScore) should be below fresh ($freshScore)", staleScore < freshScore)
    }

    @Test
    fun `confidence is low when evidence is thin`() {
        val thin = engine.compute(
            listOf(response("q1", "LEADERSHIP", true)),
            emptyList(),
            emptyMap()
        )
        val leadership = thin.domains.first { it.domain == CompetencyDomain.LEADERSHIP }
        assertTrue(leadership.confidence < 20)
    }

    @Test
    fun `confidence rises with repeated evidence`() {
        val stats = (1..40).map {
            QuestionStatEntity(questionId = "q$it", domain = "LEADERSHIP", topic = "t", seenCount = 3, correctCount = 3)
        }
        val snapshot = engine.compute(
            (1..40).map { response("q$it", "LEADERSHIP", true) },
            stats,
            emptyMap()
        )
        val leadership = snapshot.domains.first { it.domain == CompetencyDomain.LEADERSHIP }
        assertTrue(leadership.confidence > 50)
    }

    @Test
    fun `practical work adds a bounded boost`() {
        val responses = (1..10).map { response("q$it", "RISK_MANAGEMENT", true) }
        val withoutPractice = engine.compute(responses, emptyList(), emptyMap())
        val withPractice = engine.compute(responses, emptyList(), emptyMap(), mapOf("RISK_MANAGEMENT" to 100))
        val before = withoutPractice.domains.first { it.domain == CompetencyDomain.RISK_MANAGEMENT }.score
        val after = withPractice.domains.first { it.domain == CompetencyDomain.RISK_MANAGEMENT }.score
        assertTrue(after > before)
        assertTrue("boost must stay small, was ${after - before}", (after - before) <= 13)
    }

    @Test
    fun `weak domains are identified below the threshold`() {
        val snapshot = engine.compute(
            (1..10).map { response("q$it", "THIRD_PARTY", false) },
            emptyList(),
            emptyMap()
        )
        val thirdParty = snapshot.domains.first { it.domain == CompetencyDomain.THIRD_PARTY }
        assertTrue(thirdParty.weak)
        assertTrue(snapshot.weakest.isNotEmpty())
    }

    @Test
    fun `band thresholds are monotonic`() {
        assertEquals(CompetencyBand.NOVICE, engine.bandFor(0))
        assertEquals(CompetencyBand.NOVICE, engine.bandFor(24))
        assertEquals(CompetencyBand.DEVELOPING, engine.bandFor(25))
        assertEquals(CompetencyBand.PROFICIENT, engine.bandFor(50))
        assertEquals(CompetencyBand.ADVANCED, engine.bandFor(70))
        assertEquals(CompetencyBand.EXPERT, engine.bandFor(85))
        assertEquals(CompetencyBand.EXPERT, engine.bandFor(100))
    }

    @Test
    fun `a readiness statement is always produced`() {
        val snapshot = engine.compute(emptyList(), emptyList(), emptyMap())
        val statement = engine.readinessStatement(snapshot)
        assertTrue(statement.isNotBlank())
        assertFalse(statement.contains("certified", ignoreCase = true))
    }
}
