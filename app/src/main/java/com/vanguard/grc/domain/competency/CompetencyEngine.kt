package com.vanguard.grc.domain.competency

import com.vanguard.grc.data.content.Question
import com.vanguard.grc.data.local.entity.AssessmentResponseEntity
import com.vanguard.grc.data.local.entity.QuestionStatEntity

/**
 * The ten competency domains tracked by the personal learning engine.
 *
 * Competency is never derived from time spent. A domain score is a function of
 * demonstrated assessment accuracy, question difficulty and coverage, plus
 * practical project completion, and it is reported with a confidence band so a
 * single lucky answer does not manufacture a "proficient" rating.
 */
enum class CompetencyDomain(
    val id: String,
    val title: String,
    val description: String,
    val order: Int
) {
    GOVERNANCE(
        "GOVERNANCE", "Governance",
        "Governance structures, accountability, Three Lines Model, policy architecture and enterprise reporting.",
        1
    ),
    RISK_MANAGEMENT(
        "RISK_MANAGEMENT", "Risk Management",
        "Enterprise risk identification, analysis, evaluation, treatment, appetite and quantification.",
        2
    ),
    COMPLIANCE(
        "COMPLIANCE", "Compliance",
        "Obligation mapping, control programmes, evidence management, nonconformity and remediation.",
        3
    ),
    AUDIT(
        "AUDIT", "Audit",
        "Audit planning, evidence, ITGC testing, findings, root cause analysis and reporting.",
        4
    ),
    INFORMATION_SECURITY(
        "INFORMATION_SECURITY", "Information Security",
        "ISMS, control design, security operations governance and architecture review.",
        5
    ),
    PRIVACY(
        "PRIVACY", "Privacy",
        "Data protection principles, lifecycle management, DPIA, consent and data subject rights.",
        6
    ),
    BUSINESS_CONTINUITY(
        "BUSINESS_CONTINUITY", "Business Continuity",
        "BIA, RTO/RPO, continuity strategy, crisis management and recovery validation.",
        7
    ),
    THIRD_PARTY(
        "THIRD_PARTY", "Third-Party Risk",
        "Vendor due diligence, contracting, monitoring, concentration risk and exit management.",
        8
    ),
    REGULATORY(
        "REGULATORY", "Regulatory Management",
        "RBI, CERT-In, SEBI, IRDAI, GDPR, PCI DSS, SOC 2 and examination management.",
        9
    ),
    LEADERSHIP(
        "LEADERSHIP", "Leadership",
        "Operating model design, team and budget management, executive and board communication.",
        10
    );

    companion object {
        fun fromId(id: String): CompetencyDomain? = entries.firstOrNull { it.id == id }
    }
}

/**
 * The band a learner sits in for a given domain.
 */
enum class CompetencyBand(val label: String, val minScore: Int) {
    NOVICE("Novice", 0),
    DEVELOPING("Developing", 25),
    PROFICIENT("Proficient", 50),
    ADVANCED("Advanced", 70),
    EXPERT("Expert", 85)
}

data class DomainCompetency(
    val domain: CompetencyDomain,
    val score: Int,
    val band: CompetencyBand,
    val questionsAnswered: Int,
    val accuracy: Int,
    val confidence: Int,
    val strong: Boolean,
    val weak: Boolean
)

data class CompetencySnapshot(
    val domains: List<DomainCompetency>,
    val overall: Int,
    val overallBand: CompetencyBand,
    val totalQuestionsAnswered: Int,
    val coverage: Int
) {
    val strongest: List<DomainCompetency> get() = domains.filter { it.strong }.sortedByDescending { it.score }
    val weakest: List<DomainCompetency> get() = domains.filter { it.weak }.sortedBy { it.score }
}

/**
 * Computes competency from demonstrated evidence only.
 */
class CompetencyEngine {

    /**
     * @param responses every graded response the learner has submitted.
     * @param stats per-question longitudinal statistics.
     * @param practicalScores practical work completed, keyed by domain.
     */
    fun compute(
        responses: List<AssessmentResponseEntity>,
        stats: List<QuestionStatEntity>,
        questionsById: Map<String, Question>,
        practicalScores: Map<String, Int> = emptyMap()
    ): CompetencySnapshot {

        val responsesByDomain = responses.groupBy { it.domain }
        val statsByDomain = stats.groupBy { it.domain }

        val domains = CompetencyDomain.entries.map { domain ->
            val domainResponses = responsesByDomain[domain.id].orEmpty()
            val domainStats = statsByDomain[domain.id].orEmpty()

            val answered = domainResponses.size
            val correct = domainResponses.count { it.isCorrect }

            // Weighted accuracy: harder questions contribute more.
            var weightedPoints = 0.0
            var possiblePoints = 0.0
            domainResponses.forEach { r ->
                val q = questionsById[r.questionId]
                val weight = when (q?.difficulty) {
                    com.vanguard.grc.data.content.Difficulty.EXPERT -> 1.6
                    com.vanguard.grc.data.content.Difficulty.ADVANCED -> 1.3
                    com.vanguard.grc.data.content.Difficulty.INTERMEDIATE -> 1.0
                    com.vanguard.grc.data.content.Difficulty.BEGINNER -> 0.8
                    null -> 1.0
                }
                possiblePoints += weight
                if (r.isCorrect) weightedPoints += weight
            }
            val weightedAccuracy = if (possiblePoints == 0.0) 0.0 else (weightedPoints / possiblePoints)

            // Recency: evidence from more than 120 days ago is discounted.
            val now = System.currentTimeMillis()
            val recencyFactor = if (domainResponses.isEmpty()) 0.0 else {
                val avgAge = domainResponses.map { now - it.answeredAt }.average()
                when {
                    avgAge <= 30L * 24 * 3600 * 1000 -> 1.0
                    avgAge <= 90L * 24 * 3600 * 1000 -> 0.9
                    avgAge <= 180L * 24 * 3600 * 1000 -> 0.8
                    else -> 0.7
                }
            }

            // Coverage: a domain needs breadth, not depth in one topic.
            val topicsCovered = domainResponses.map { it.difficulty }.distinct().size
            val breadth = when {
                answered >= 40 -> 1.0
                answered >= 20 -> 0.85
                answered >= 10 -> 0.7
                answered >= 5 -> 0.5
                answered > 0 -> 0.3
                else -> 0.0
            }

            val practiceBoost = practicalScores[domain.id]?.let { it / 100.0 * 0.12 } ?: 0.0

            val raw = (weightedAccuracy * 0.70 + breadth * 0.30) * recencyFactor + practiceBoost
            val score = (raw * 100).toInt().coerceIn(0, 100)

            val accuracy = if (answered == 0) 0 else (correct * 100 / answered)
            val confidence = computeConfidence(answered, domainStats.size, recencyFactor)

            DomainCompetency(
                domain = domain,
                score = score,
                band = bandFor(score),
                questionsAnswered = answered,
                accuracy = accuracy,
                confidence = confidence,
                strong = answered >= 10 && score >= 70,
                weak = answered >= 5 && score < 55
            )
        }

        val overallRaw = domains.sumOf { it.score } / domains.size
        val overall = (overallRaw * 0.85 + coverageBonus(domains) * 0.15).toInt().coerceIn(0, 100)

        return CompetencySnapshot(
            domains = domains,
            overall = overall,
            overallBand = bandFor(overall),
            totalQuestionsAnswered = responses.size,
            coverage = coverageBonus(domains)
        )
    }

    /**
     * Confidence reflects how much evidence supports the estimate, expressed as
     * a percentage. It is deliberately visible so a learner understands that a
     * high score based on four questions is not a reliable signal.
     */
    private fun computeConfidence(answered: Int, seen: Int, recency: Double): Int {
        val evidence = (answered.toDouble() / 30.0).coerceAtMost(1.0)
        val repetition = (seen.toDouble() / 60.0).coerceAtMost(1.0)
        return ((evidence * 0.6 + repetition * 0.4) * recency * 100).toInt().coerceIn(0, 100)
    }

    /**
     * Coverage is the percentage of competency domains where the learner has
     * demonstrated at least a developing level of evidence.
     */
    private fun coverageBonus(domains: List<DomainCompetency>): Int {
        if (domains.isEmpty()) return 0
        val covered = domains.count { it.questionsAnswered >= 5 }
        return (covered * 100 / domains.size)
    }

    fun bandFor(score: Int): CompetencyBand = CompetencyBand.entries
        .last { score >= it.minScore }

    /**
     * Converts a competency score into a plain-language readiness statement.
     * The wording deliberately avoids implying professional certification.
     */
    fun readinessStatement(snapshot: CompetencySnapshot): String {
        val band = snapshot.overallBand
        return when (band) {
            CompetencyBand.NOVICE ->
                "Foundational stage. You can recognise GRC terminology, but decisions still need close supervision. Complete the first two weeks of the accelerator and re-assess."
            CompetencyBand.DEVELOPING ->
                "Developing analyst stage. You understand the frameworks and can support risk and compliance activities. Focus on the practical exercises to turn knowledge into repeatable method."
            CompetencyBand.PROFICIENT ->
                "Practitioner stage. You can run risk assessments, control reviews and audit support independently. Next, work on the leadership scenarios where judgement, not process, is the differentiator."
            CompetencyBand.ADVANCED ->
                "Senior practitioner stage. You can design control frameworks and lead assurance activity. Extend into operating-model design, executive reporting and examination management."
            CompetencyBand.EXPERT ->
                "Enterprise GRC lead stage on the evidence available. Validate by running the capstone simulations under time pressure and seeking review from a practising GRC leader."
        }
    }
}
