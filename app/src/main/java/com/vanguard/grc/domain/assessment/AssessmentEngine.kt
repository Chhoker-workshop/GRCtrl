package com.vanguard.grc.domain.assessment

import com.vanguard.grc.data.content.Difficulty
import com.vanguard.grc.data.content.Question
import com.vanguard.grc.data.content.QuestionType
import kotlin.math.roundToInt
import kotlin.random.Random

/**
 * A learner's answer to a single question.
 */
data class Answer(
    val questionId: String,
    val selectedOptionIds: List<String> = emptyList(),
    val numericResponse: Double? = null,
    val matchMapping: Map<String, String> = emptyMap(),
    val freeText: String = "",
    val timeSpentSeconds: Int = 0
)

data class GradedAnswer(
    val question: Question,
    val answer: Answer,
    val correct: Boolean,
    val awardedPoints: Int,
    val maxPoints: Int,
    val feedback: String
)

data class ScoreResult(
    val graded: List<GradedAnswer>,
    val scorePercent: Int,
    val correctCount: Int,
    val totalCount: Int,
    val totalPoints: Int,
    val awardedPoints: Int,
    val domainBreakdown: Map<String, DomainBreakdown>,
    val passed: Boolean
) {
    data class DomainBreakdown(
        val domain: String,
        val correct: Int,
        val total: Int,
        val percent: Int
    )
}

/**
 * Scores a set of answers against a set of questions and produces the domain
 * breakdown used by the competency engine.
 */
class AssessmentEngine {

    fun score(questions: List<Question>, answers: List<Answer>, passMark: Int = 70): ScoreResult {
        val answerMap = answers.associateBy { it.questionId }
        val graded = questions.map { q -> grade(q, answerMap[q.id]) }

        val totalPoints = graded.sumOf { it.maxPoints }
        val awarded = graded.sumOf { it.awardedPoints }
        val percent = if (totalPoints == 0) 0 else ((awarded * 100f) / totalPoints).toInt()
        val correct = graded.count { it.correct }

        val breakdown = graded.groupBy { it.question.domain }
            .mapValues { (_, list) ->
                val total = list.size
                val right = list.count { it.correct }
                ScoreResult.DomainBreakdown(
                    domain = list.first().question.domain,
                    correct = right,
                    total = total,
                    percent = if (total == 0) 0 else (right * 100 / total)
                )
            }
            .mapKeys { it.key }

        return ScoreResult(
            graded = graded,
            scorePercent = percent,
            correctCount = correct,
            totalCount = graded.size,
            totalPoints = totalPoints,
            awardedPoints = awarded,
            domainBreakdown = breakdown,
            passed = percent >= passMark
        )
    }

    fun grade(question: Question, answer: Answer?): GradedAnswer {
        if (answer == null) {
            return GradedAnswer(
                question = question,
                answer = Answer(question.id),
                correct = false,
                awardedPoints = 0,
                maxPoints = question.points,
                feedback = "Not answered."
            )
        }
        return when (question.type) {
            QuestionType.MULTIPLE_CHOICE,
            QuestionType.TRUE_FALSE,
            QuestionType.MULTIPLE_SELECT,
            QuestionType.SCENARIO,
            QuestionType.EVIDENCE_IDENTIFICATION,
            QuestionType.COMPLIANCE_GAP,
            QuestionType.PRIORITISE_REMEDIATION,
            QuestionType.MANAGEMENT_DECISION -> gradeOption(question, answer)

            QuestionType.RISK_CALCULATION -> gradeNumeric(question, answer)
            QuestionType.MATCH_CONTROL -> gradeMatch(question, answer)
            QuestionType.SHORT_ANSWER,
            QuestionType.PRACTICAL_ASSIGNMENT -> GradedAnswer(
                question = question,
                answer = answer,
                correct = true,
                awardedPoints = if (answer.freeText.isNotBlank()) question.points else 0,
                maxPoints = question.points,
                feedback = if (answer.freeText.isNotBlank()) {
                    "Response recorded. Compare your answer against the model answer below."
                } else {
                    "No response recorded."
                }
            )
        }
    }

    private fun gradeOption(question: Question, answer: Answer): GradedAnswer {
        val expected = question.correctOptionIds.sorted()
        val given = answer.selectedOptionIds.sorted()
        val correct = expected == given
        val feedback = if (correct) {
            question.explanation
        } else {
            buildString {
                append(question.explanation)
                val wrong = given.filterNot { it in expected }
                if (wrong.isNotEmpty()) {
                    append("\n\nWhy your selection is weaker:")
                    wrong.forEach { id ->
                        val option = question.options.find { it.id == id }
                        val reason = question.whyOthersWrong[id] ?: option?.rationale
                        if (!reason.isNullOrBlank()) {
                            append("\n- ${option?.text ?: id}: $reason")
                        }
                    }
                }
                val missed = expected.filterNot { it in given }
                if (missed.isNotEmpty()) {
                    append("\n\nWhat the stronger answer was:")
                    missed.forEach { id ->
                        val option = question.options.find { it.id == id }
                        if (option != null) append("\n- ${option.text}")
                    }
                }
            }
        }
        return GradedAnswer(
            question = question,
            answer = answer,
            correct = correct,
            awardedPoints = if (correct) question.points else 0,
            maxPoints = question.points,
            feedback = feedback
        )
    }

    private fun gradeNumeric(question: Question, answer: Answer): GradedAnswer {
        val expected = question.numericAnswer
        val given = answer.numericResponse
        if (expected == null || given == null) {
            return GradedAnswer(question, answer, false, 0, question.points, "No calculation supplied.")
        }
        val tolerance = expected.tolerance.coerceAtLeast(0.0001)
        val correct = kotlin.math.abs(given - expected.expected) <= tolerance
        val feedback = buildString {
            append(if (correct) "Correct. " else "Not quite. ")
            if (expected.formula.isNotBlank()) append("Method: ${expected.formula}\n")
            append("Expected: ${formatNumber(expected.expected)}${if (expected.unit.isNotBlank()) " ${expected.unit}" else ""}")
            if (!correct) {
                append("\nYour answer: ${formatNumber(given)}")
            }
            if (expected.workedExample.isNotBlank()) {
                append("\n\nWorked example:\n${expected.workedExample}")
            }
            if (question.explanation.isNotBlank()) append("\n\n${question.explanation}")
        }
        return GradedAnswer(
            question = question,
            answer = answer,
            correct = correct,
            awardedPoints = if (correct) question.points else 0,
            maxPoints = question.points,
            feedback = feedback
        )
    }

    private fun gradeMatch(question: Question, answer: Answer): GradedAnswer {
        val expected = question.matchPairs.associate { it.leftId to it.right }
        val given = answer.matchMapping
        val correctCount = expected.count { (left, right) -> given[left] == right }
        val fullyCorrect = correctCount == expected.size && expected.isNotEmpty()
        val feedback = buildString {
            append("Matched $correctCount of ${expected.size} items correctly.\n\n")
            question.matchPairs.forEach { pair ->
                val mark = if (given[pair.leftId] == pair.right) "correct" else "incorrect"
                append("- ${pair.left} -> ${pair.right} ($mark)")
                if (pair.note.isNotBlank()) append(": ${pair.note}")
                append("\n")
            }
            if (question.explanation.isNotBlank()) append("\n${question.explanation}")
        }
        val proportional = if (expected.isEmpty()) 0
            else if (fullyCorrect) question.points
            else ((correctCount.toDouble() / expected.size) * question.points).toInt()
        return GradedAnswer(
            question = question,
            answer = answer,
            correct = fullyCorrect,
            awardedPoints = proportional,
            maxPoints = question.points,
            feedback = feedback
        )
    }

    private fun formatNumber(value: Double): String =
        if (value == value.toLong().toDouble()) value.toLong().toString()
        else String.format("%.2f", value)

    /**
     * Builds a balanced exam from a question pool. Selection is randomised but
     * deterministic per attempt seed, and difficulty distribution is enforced
     * so a learner is not simply served the easiest items.
     */
    fun buildExam(
        pool: List<Question>,
        count: Int,
        seed: Long = System.nanoTime(),
        difficultyMix: Map<Difficulty, Double> = defaultMix(count)
    ): List<Question> {
        if (pool.isEmpty() || count <= 0) return emptyList()
        val random = Random(seed)
        val shuffled = pool.shuffled(random)
        val selected = mutableListOf<Question>()
        val usedIds = mutableSetOf<String>()

        difficultyMix.forEach { (difficulty, proportion) ->
            val target = (count * proportion).toInt().coerceAtLeast(if (proportion > 0) 1 else 0)
            val candidates = shuffled.filter { it.difficulty == difficulty && it.id !in usedIds }
            candidates.take(target).forEach { selected += it; usedIds += it.id }
        }
        if (selected.size < count) {
            shuffled.filter { it.id !in usedIds }.take(count - selected.size).forEach {
                selected += it; usedIds += it.id
            }
        }
        if (selected.size > count) {
            val byDifficulty = selected.groupBy { it.difficulty }.toMutableMap()
            val trimmed = mutableListOf<Question>()
            difficultyMix.forEach { (difficulty, proportion) ->
                val target = (count * proportion).toInt().coerceAtLeast(if (proportion > 0) 1 else 0)
                val bucket = byDifficulty[difficulty].orEmpty().shuffled(random)
                trimmed += bucket.take(target.coerceAtMost(bucket.size))
                byDifficulty[difficulty] = bucket.drop(target.coerceAtMost(bucket.size))
            }
            if (trimmed.size < count) {
                val remaining = byDifficulty.values.flatten().shuffled(random)
                trimmed += remaining.take(count - trimmed.size)
            }
            return trimmed.shuffled(random).take(count)
        }
        return selected.shuffled(random).take(count)
    }

    private fun defaultMix(count: Int) = mapOf(
        Difficulty.BEGINNER to 0.20,
        Difficulty.INTERMEDIATE to 0.40,
        Difficulty.ADVANCED to 0.30,
        Difficulty.EXPERT to 0.10
    )
}
