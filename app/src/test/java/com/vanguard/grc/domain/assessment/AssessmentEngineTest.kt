package com.vanguard.grc.domain.assessment

import com.vanguard.grc.data.content.Difficulty
import com.vanguard.grc.data.content.Question
import com.vanguard.grc.data.content.QuestionOption
import com.vanguard.grc.data.content.QuestionType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AssessmentEngineTest {

    private val engine = AssessmentEngine()

    private fun mc(id: String, correct: String = "a", difficulty: Difficulty = Difficulty.INTERMEDIATE) = Question(
        id = id,
        domain = "GOVERNANCE",
        topic = "Test",
        type = QuestionType.MULTIPLE_CHOICE,
        difficulty = difficulty,
        stem = "Stem for $id",
        options = listOf(
            QuestionOption("a", "Correct", correct = correct == "a", rationale = "Because it is right"),
            QuestionOption("b", "Wrong", correct = correct == "b", rationale = "Because it is wrong")
        ),
        correctOptionIds = listOf(correct),
        explanation = "Full explanation for $id"
    )

    @Test
    fun `a correct multiple choice answer scores full points`() {
        val question = mc("q1")
        val result = engine.score(listOf(question), listOf(Answer("q1", listOf("a"))))
        assertTrue(result.passed)
        assertEquals(100, result.scorePercent)
        assertEquals(1, result.correctCount)
    }

    @Test
    fun `an incorrect answer scores zero and explains the better option`() {
        val question = mc("q1")
        val result = engine.score(listOf(question), listOf(Answer("q1", listOf("b"))))
        assertFalse(result.passed)
        assertEquals(0, result.scorePercent)
        assertTrue(result.graded.first().feedback.contains("Why your selection is weaker"))
        assertTrue(result.graded.first().feedback.contains("What the stronger answer was"))
    }

    @Test
    fun `an unanswered question is reported rather than skipped`() {
        val question = mc("q1")
        val result = engine.score(listOf(question), emptyList())
        assertEquals(0, result.correctCount)
        assertEquals(1, result.totalCount)
        assertEquals("Not answered.", result.graded.first().feedback)
    }

    @Test
    fun `multiple select requires exactly the right combination`() {
        val question = Question(
            id = "ms1",
            domain = "AUDIT",
            topic = "Test",
            type = QuestionType.MULTIPLE_SELECT,
            stem = "Select all that apply",
            options = listOf(
                QuestionOption("a", "One", correct = true, rationale = "r"),
                QuestionOption("b", "Two", correct = true, rationale = "r"),
                QuestionOption("c", "Three", correct = false, rationale = "r")
            ),
            correctOptionIds = listOf("a", "b"),
            explanation = "Both are required"
        )
        val partial = engine.score(listOf(question), listOf(Answer("ms1", listOf("a"))))
        assertFalse(partial.passed)
        assertEquals(0, partial.correctCount)

        val full = engine.score(listOf(question), listOf(Answer("ms1", listOf("a", "b"))))
        assertTrue(full.passed)
    }

    @Test
    fun `a risk calculation is graded against a tolerance`() {
        val question = Question(
            id = "rc1",
            domain = "RISK_MANAGEMENT",
            topic = "Test",
            type = QuestionType.RISK_CALCULATION,
            stem = "Calculate the inherent score",
            options = listOf(
                QuestionOption("a", "15", rationale = "r"),
                QuestionOption("b", "9", rationale = "r")
            ),
            correctOptionIds = listOf("a"),
            explanation = "3 x 5",
            numericAnswer = com.vanguard.grc.data.content.NumericAnswer(
                expected = 15.0,
                tolerance = 0.01,
                unit = "score",
                formula = "likelihood x impact",
                workedExample = "3 x 5 = 15"
            )
        )
        val correct = engine.score(listOf(question), listOf(Answer("rc1", numericResponse = 15.0)))
        assertTrue(correct.passed)
        assertTrue(correct.graded.first().feedback.contains("Correct"))

        val wrong = engine.score(listOf(question), listOf(Answer("rc1", numericResponse = 12.0)))
        assertFalse(wrong.passed)
        assertTrue(wrong.graded.first().feedback.contains("Your answer"))
    }

    @Test
    fun `a matching question is graded proportionally`() {
        val question = Question(
            id = "mc1",
            domain = "INFORMATION_SECURITY",
            topic = "Test",
            type = QuestionType.MATCH_CONTROL,
            stem = "Match the controls",
            options = emptyList(),
            correctOptionIds = listOf("l1", "l2"),
            explanation = "Straightforward pairing",
            matchPairs = listOf(
                com.vanguard.grc.data.content.MatchPair("l1", "Access control", "A.5.15"),
                com.vanguard.grc.data.content.MatchPair("l2", "Cryptography", "A.8.24")
            )
        )
        val half = engine.score(
            listOf(question),
            listOf(Answer("mc1", matchMapping = mapOf("l1" to "A.5.15", "l2" to "wrong")))
        )
        assertFalse(half.passed)
        assertEquals(0, half.graded.first().awardedPoints)
        assertTrue(half.graded.first().feedback.contains("Matched 1 of 2"))

        val full = engine.score(
            listOf(question),
            listOf(Answer("mc1", matchMapping = mapOf("l1" to "A.5.15", "l2" to "A.8.24")))
        )
        assertTrue(full.passed)
        assertEquals(1, full.graded.first().awardedPoints)
    }

    @Test
    fun `domain breakdown is produced for scoring`() {
        val questions = listOf(
            mc("q1").copy(domain = "GOVERNANCE"),
            mc("q2").copy(domain = "AUDIT")
        )
        val result = engine.score(questions, listOf(Answer("q1", listOf("a")), Answer("q2", listOf("b"))))
        assertEquals(2, result.domainBreakdown.size)
        assertEquals(100, result.domainBreakdown["GOVERNANCE"]?.percent)
        assertEquals(0, result.domainBreakdown["AUDIT"]?.percent)
    }

    @Test
    fun `pass mark is respected`() {
        val questions = listOf(mc("q1"), mc("q2"))
        val answers = listOf(Answer("q1", listOf("a")), Answer("q2", listOf("b")))
        assertTrue(engine.score(questions, answers, passMark = 50).passed)
        assertFalse(engine.score(questions, answers, passMark = 80).passed)
    }

    @Test
    fun `exam construction respects the requested count`() {
        val pool = (1..60).map { mc("q$it") }
        val exam = engine.buildExam(pool, count = 20, seed = 42L)
        assertEquals(20, exam.size)
        assertEquals(20, exam.map { it.id }.distinct().size)
    }

    @Test
    fun `exam construction applies a difficulty mix`() {
        val pool = (1..40).map { mc("b$it", difficulty = Difficulty.BEGINNER) } +
            (1..40).map { mc("i$it", difficulty = Difficulty.INTERMEDIATE) } +
            (1..40).map { mc("a$it", difficulty = Difficulty.ADVANCED) } +
            (1..20).map { mc("e$it", difficulty = Difficulty.EXPERT) }
        val exam = engine.buildExam(pool, count = 40, seed = 7L)
        assertEquals(40, exam.size)
        val advancedish = exam.count { it.difficulty == Difficulty.ADVANCED || it.difficulty == Difficulty.EXPERT }
        assertTrue("expected advanced items to be represented, got $advancedish", advancedish > 0)
    }

    @Test
    fun `exam construction is deterministic for a given seed`() {
        val pool = (1..30).map { mc("q$it") }
        val first = engine.buildExam(pool, count = 15, seed = 99L).map { it.id }
        val second = engine.buildExam(pool, count = 15, seed = 99L).map { it.id }
        assertEquals(first, second)
    }

    @Test
    fun `exam construction on an empty pool returns nothing`() {
        assertTrue(engine.buildExam(emptyList(), count = 10).isEmpty())
    }
}
