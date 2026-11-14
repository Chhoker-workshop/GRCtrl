package com.vanguard.grc.domain.mentor

import com.vanguard.grc.data.content.ContentIndex
import com.vanguard.grc.data.content.ContentSearch
import com.vanguard.grc.data.content.SearchKind
import com.vanguard.grc.data.content.SearchResult

/**
 * A cited answer produced by the offline mentor.
 */
data class MentorAnswer(
    val question: String,
    val headline: String,
    val body: String,
    val citations: List<Citation>,
    val suggestedActions: List<String>,
    val caveats: List<String>,
    val intent: String
)

data class Citation(
    val label: String,
    val route: String,
    val kind: SearchKind
)

/**
 * The offline GRC mentor.
 *
 * The mentor is a retrieval and synthesis engine over the verified curriculum
 * corpus. It never invents a clause reference, a control number or a
 * regulatory obligation - every statement it makes is traceable to content
 * shipped inside the application, and it says so when the corpus does not
 * contain a confident answer.
 *
 * An optional external model can be attached later (see the settings screen).
 * That path is deliberately layered on top of this engine so the platform is
 * fully functional with no network and no credentials.
 */
class MentorEngine(private val index: ContentIndex) {

    private val search = ContentSearch(index)

    fun ask(question: String): MentorAnswer {
        val trimmed = question.trim()
        if (trimmed.isEmpty()) {
            return MentorAnswer(
                question = trimmed,
                headline = "Ask a specific question",
                body = "Describe the concept, standard, control or decision you want to understand. " +
                    "For example: \"How do I justify an ISO 27001 exclusion?\" or \"What is the difference between risk appetite and risk tolerance?\"",
                citations = emptyList(),
                suggestedActions = emptyList(),
                caveats = emptyList(),
                intent = "empty"
            )
        }

        val intent = classifyIntent(trimmed)
        val results = search.search(trimmed, limit = 12)

        if (results.isEmpty()) {
            return MentorAnswer(
                question = trimmed,
                headline = "The curriculum does not cover that yet",
                body = "No content in the offline corpus matched that question closely enough to answer responsibly. " +
                    "Try the terminology used by the frameworks themselves - for example \"control effectiveness\", " +
                    "\"risk appetite\", \"Statement of Applicability\", \"DPIA\" or \"RTO\". You can also use global search " +
                    "to find the specific lesson yourself.",
                citations = emptyList(),
                suggestedActions = listOf(
                    "Search the glossary for the term",
                    "Open the Framework Explorer to check the relevant standard",
                    "Raise it in a simulator scenario, where you can practise the decision"
                ),
                caveats = listOf(
                    "This mentor answers only from the curriculum shipped in this application. It will not invent a standard clause or a regulatory obligation."
                ),
                intent = intent
            )
        }

        val lessonResults = results.filter { it.kind == SearchKind.LESSON }
        val controlResults = results.filter { it.kind == SearchKind.CONTROL }
        val frameworkResults = results.filter { it.kind == SearchKind.FRAMEWORK }
        val otherResults = results.filter {
            it.kind !in setOf(SearchKind.LESSON, SearchKind.CONTROL, SearchKind.FRAMEWORK)
        }

        val headline = buildHeadline(results.first(), intent)
        val body = buildBody(intent, lessonResults, controlResults, frameworkResults, otherResults)

        val citations = buildCitations(results)
        val actions = buildActions(intent, results)
        val caveats = buildCaveats(intent, results)

        return MentorAnswer(
            question = trimmed,
            headline = headline,
            body = body,
            citations = citations,
            suggestedActions = actions,
            caveats = caveats,
            intent = intent
        )
    }

    /**
     * Classifies the question into an intent so the answer can be framed
     * appropriately rather than being a generic summary.
     */
    private fun classifyIntent(question: String): String {
        val q = question.lowercase()
        return when {
            listOf("how do i", "how to", "steps", "process", "implement").any { it in q } -> "implementation"
            listOf("difference between", "vs ", "versus", "compare").any { it in q } -> "comparison"
            listOf("evidence", "audit", "prove", "demonstrate").any { it in q } -> "evidence"
            listOf("decision", "escalate", "approve", "reject", "accept", "justify").any { it in q } -> "decision"
            listOf("who", "role", "responsib").any { it in q } -> "roles"
            listOf("what is", "define", "meaning of", "explain").any { it in q } -> "definition"
            else -> "general"
        }
    }

    private fun buildHeadline(top: SearchResult, intent: String): String = when (intent) {
        "comparison" -> "Comparison: ${top.title}"
        "evidence" -> "Evidence approach for ${top.title}"
        "decision" -> "Decision guidance for ${top.title}"
        "implementation" -> "How to approach ${top.title}"
        else -> top.title
    }

    private fun buildBody(
        intent: String,
        lessons: List<SearchResult>,
        controls: List<SearchResult>,
        frameworks: List<SearchResult>,
        others: List<SearchResult>
    ): String {
        val sb = StringBuilder()

        lessons.firstOrNull()?.let { lesson ->
            val detail = index.lessonsById[lesson.id]
            sb.append(lesson.body.ifBlank { lesson.subtitle }.take(1400))
            if (detail != null) {
                sb.append("\n\n")
                detail.practicalExercise?.let { exercise ->
                    sb.append("Practical application. ").append(exercise.brief).append("\n")
                }
            }
        }

        if (controls.isNotEmpty()) {
            sb.append("\n\nControls to consider:\n")
            controls.take(4).forEach { control ->
                val detail = index.controlsById[control.id]
                sb.append("- ${control.title}")
                detail?.let {
                    sb.append(" - ").append(it.objective)
                }
                sb.append("\n")
            }
        }

        if (frameworks.isNotEmpty()) {
            sb.append("\n\nAuthoritative reference:\n")
            frameworks.take(3).forEach { fw ->
                val detail = index.frameworksById[fw.id]
                sb.append("- ${fw.title}")
                detail?.let {
                    sb.append(" (").append(it.publisher)
                    if (it.edition.isNotBlank()) sb.append(", ").append(it.edition)
                    sb.append("; verified ").append(it.lastVerified).append(")")
                }
                sb.append("\n")
            }
        }

        if (others.isNotEmpty()) {
            sb.append("\n\nAlso relevant:\n")
            others.take(4).forEach { sb.append("- ${it.title}\n") }
        }

        return sb.toString().trim()
    }

    private fun buildCitations(results: List<SearchResult>): List<Citation> =
        results.take(8).map {
            Citation(
                label = "${it.kind.label}: ${it.title}",
                route = it.route,
                kind = it.kind
            )
        }

    private fun buildActions(intent: String, results: List<SearchResult>): List<String> {
        val actions = mutableListOf<String>()
        when (intent) {
            "implementation" -> actions += "Turn this into a work instruction: name the owner, the trigger and the evidence you will hold."
            "evidence" -> actions += "Identify who can produce the evidence, the period it covers, and who validated it."
            "decision" -> actions += "Write down the decision, the options considered, the criteria applied and the review date."
            "comparison" -> actions += "Check what the second framework adds before assuming your existing evidence satisfies it."
            "roles" -> actions += "Confirm the decision is within the role's delegated authority, and escalate if it is not."
            else -> actions += "Open the linked lesson and work through its practical exercise."
        }
        results.firstOrNull { it.kind == SearchKind.LESSON }?.let {
            actions += "Read \"${it.title}\" and complete its exercise."
        }
        results.firstOrNull { it.kind == SearchKind.CONTROL }?.let {
            actions += "Review Annex A control ${it.title} for implementation guidance and common gaps."
        }
        results.firstOrNull { it.kind == SearchKind.SCENARIO }?.let {
            actions += "Practise the decision in the simulator scenario \"${it.title}\"."
        }
        return actions.distinct().take(5)
    }

    private fun buildCaveats(intent: String, results: List<SearchResult>): List<String> {
        val caveats = mutableListOf<String>()
        caveats += "This answer is generated from the GRC VANGUARD curriculum corpus. It is a teaching aid, not professional advice."
        if (intent == "comparison") {
            caveats += "A mapping shows a shared objective. It does not establish compliance with either framework."
        }
        val hasFramework = results.any { it.kind == SearchKind.FRAMEWORK }
        if (!hasFramework) {
            caveats += "No authoritative source in the corpus matched this question closely. Verify any standard or regulatory point against the current published text before relying on it."
        }
        caveats += "An AI-generated answer is not formal audit approval and is not evidence of conformity."
        return caveats
    }

    /**
     * Suggested questions shown before the learner types anything.
     */
    fun starterQuestions(): List<String> = listOf(
        "What is the difference between risk appetite and risk tolerance?",
        "How do I justify excluding a control from the Statement of Applicability?",
        "What evidence proves a control is operating effectively?",
        "How do the Three Lines Model and ISO 27001 relate?",
        "When should I escalate a risk instead of accepting it?",
        "How do I build a risk register for a new business line?",
        "What makes a good audit finding?",
        "How do I map ISO 27001 controls to NIST CSF outcomes?",
        "What is a DPIA and when is one required?",
        "How should I respond to a failed vendor security assessment?"
    )
}
