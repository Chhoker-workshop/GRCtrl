package com.vanguard.grc.domain.assessment

import com.vanguard.grc.data.content.Question
import com.vanguard.grc.domain.competency.CompetencyDomain

/**
 * Definition of a formal assessment.
 */
data class AssessmentDefinition(
    val id: String,
    val title: String,
    val description: String,
    val kind: String,
    val domain: String,
    val questionCount: Int,
    val timeLimitMinutes: Int,
    val passMark: Int,
    val tags: List<String> = emptyList(),
    /** Restricts the question pool. An empty pool means every question is eligible. */
    val poolSelector: (List<Question>) -> List<Question> = { it }
)

/**
 * The assessment catalogue: domain examinations, certification-aligned
 * examinations and full mock examinations.
 *
 * Questions are drawn from the shared question bank and filtered at runtime,
 * so adding new questions to the corpus automatically strengthens every
 * examination.
 */
object AssessmentCatalog {

    private val byDomain = { domain: String ->
        { pool: List<Question> -> pool.filter { it.domain == domain } }
    }

    private val byTag = { tag: String ->
        { pool: List<Question> -> pool.filter { q -> q.tags.any { it.equals(tag, ignoreCase = true) } } }
    }

    private val all = { pool: List<Question> -> pool }

    val definitions: List<AssessmentDefinition> = buildList {
        // --- Domain examinations (10 competency domains) -------------------------
        CompetencyDomain.entries.forEach { domain ->
            add(
                AssessmentDefinition(
                    id = "dom_${domain.id.lowercase()}",
                    title = "${domain.title} Domain Examination",
                    description = "A full examination across ${domain.title.lowercase()}. " +
                        "Covers ${domain.description} Questions are drawn from every difficulty level, " +
                        "weighted towards applied and expert items.",
                    kind = "DOMAIN_EXAM",
                    domain = domain.id,
                    questionCount = 25,
                    timeLimitMinutes = 35,
                    passMark = 70,
                    tags = listOf(domain.title),
                    poolSelector = byDomain(domain.id)
                )
            )
        }

        // --- Certification-aligned examinations ---------------------------------
        add(
            AssessmentDefinition(
                id = "cert_cisa",
                title = "CISA-aligned Practice Examination",
                description = "A practice examination structured on the ISACA CISA domains: " +
                    "Information Systems Auditing Process, Governance and Management of IT, " +
                    "Acquisition Development and Implementation, Operations and Business Resilience, " +
                    "and Protection of Information Assets. These are original practice questions written for " +
                    "learning, not official ISACA examination content.",
                kind = "CERTIFICATION_PRACTICE",
                domain = "AUDIT",
                questionCount = 40,
                timeLimitMinutes = 55,
                passMark = 70,
                tags = listOf("CISA"),
                poolSelector = { pool -> pool.filter { it.domain == "AUDIT" || it.domain == "INFORMATION_SECURITY" } }
            )
        )
        add(
            AssessmentDefinition(
                id = "cert_cism",
                title = "CISM-aligned Practice Examination",
                description = "A practice examination across the four CISM domains: Information Security " +
                    "Governance, Information Risk Management, Information Security Program Development and " +
                    "Management, and Information Security Incident Management. Original learning content only.",
                kind = "CERTIFICATION_PRACTICE",
                domain = "INFORMATION_SECURITY",
                questionCount = 40,
                timeLimitMinutes = 55,
                passMark = 70,
                tags = listOf("CISM"),
                poolSelector = { pool -> pool.filter { it.domain == "INFORMATION_SECURITY" || it.domain == "RISK_MANAGEMENT" } }
            )
        )
        add(
            AssessmentDefinition(
                id = "cert_cissp",
                title = "CISSP-aligned Practice Examination",
                description = "A practice examination across the eight CISSP knowledge areas, weighted towards " +
                    "governance, risk and GRC-relevant security concepts rather than pure exploitation technique. " +
                    "Original learning content only - not official ISC2 material.",
                kind = "CERTIFICATION_PRACTICE",
                domain = "INFORMATION_SECURITY",
                questionCount = 45,
                timeLimitMinutes = 60,
                passMark = 70,
                tags = listOf("CISSP"),
                poolSelector = { pool ->
                    pool.filter {
                        it.domain == "INFORMATION_SECURITY" || it.domain == "RISK_MANAGEMENT" ||
                            it.domain == "PRIVACY" || it.domain == "BUSINESS_CONTINUITY"
                    }
                }
            )
        )
        add(
            AssessmentDefinition(
                id = "cert_iso27001",
                title = "ISO/IEC 27001 Practitioner Examination",
                description = "An examination across ISMS implementation, risk management, Annex A control themes, " +
                    "internal audit and the management review cycle.",
                kind = "CERTIFICATION_PRACTICE",
                domain = "COMPLIANCE",
                questionCount = 35,
                timeLimitMinutes = 50,
                passMark = 70,
                tags = listOf("ISO 27001"),
                poolSelector = { pool -> pool.filter { it.domain == "COMPLIANCE" || it.domain == "INFORMATION_SECURITY" } }
            )
        )
        add(
            AssessmentDefinition(
                id = "cert_leadership",
                title = "Enterprise GRC Leadership Examination",
                description = "A senior-level examination on operating model design, audit and compliance programme " +
                    "management, executive and board reporting, risk appetite and leadership judgement.",
                kind = "CERTIFICATION_PRACTICE",
                domain = "LEADERSHIP",
                questionCount = 30,
                timeLimitMinutes = 45,
                passMark = 75,
                tags = listOf("GRC Leadership"),
                poolSelector = { pool -> pool.filter { it.domain == "LEADERSHIP" || it.domain == "GOVERNANCE" } }
            )
        )

        // --- Mock examinations ----------------------------------------------------
        add(
            AssessmentDefinition(
                id = "mock_full_1",
                title = "Comprehensive Mock Examination 1 - Foundation",
                description = "A full-length mock across every domain at foundation and intermediate level.",
                kind = "MOCK_EXAM",
                domain = "GENERAL",
                questionCount = 50,
                timeLimitMinutes = 70,
                passMark = 70,
                tags = listOf("Mock"),
                poolSelector = all
            )
        )
        add(
            AssessmentDefinition(
                id = "mock_full_2",
                title = "Comprehensive Mock Examination 2 - Analyst",
                description = "A full-length mock weighted towards applied judgement across all ten domains.",
                kind = "MOCK_EXAM",
                domain = "GENERAL",
                questionCount = 60,
                timeLimitMinutes = 80,
                passMark = 72,
                tags = listOf("Mock"),
                poolSelector = all
            )
        )
        add(
            AssessmentDefinition(
                id = "mock_full_3",
                title = "Comprehensive Mock Examination 3 - Specialist",
                description = "A demanding mock with advanced and expert items, mixed across every domain.",
                kind = "MOCK_EXAM",
                domain = "GENERAL",
                questionCount = 65,
                timeLimitMinutes = 90,
                passMark = 74,
                tags = listOf("Mock"),
                poolSelector = all
            )
        )
        add(
            AssessmentDefinition(
                id = "mock_regulatory",
                title = "Mock Examination - Regulatory and Examination Management",
                description = "A mock focused on obligation management, examination handling and enforcement response.",
                kind = "MOCK_EXAM",
                domain = "REGULATORY",
                questionCount = 40,
                timeLimitMinutes = 55,
                passMark = 72,
                tags = listOf("Mock", "Regulatory"),
                poolSelector = byDomain("REGULATORY")
            )
        )
        add(
            AssessmentDefinition(
                id = "mock_leadership",
                title = "Mock Examination - Executive and Board Facing",
                description = "A mock built from management decision scenarios, board reporting and stakeholder conflict.",
                kind = "MOCK_EXAM",
                domain = "LEADERSHIP",
                questionCount = 35,
                timeLimitMinutes = 50,
                passMark = 75,
                tags = listOf("Mock", "Leadership"),
                poolSelector = byDomain("LEADERSHIP")
            )
        )

        // --- Practice modes --------------------------------------------------------
        add(
            AssessmentDefinition(
                id = "practice_random",
                title = "Randomised Practice Set",
                description = "A mixed set drawn from the whole question bank, balanced across difficulty.",
                kind = "PRACTICE",
                domain = "GENERAL",
                questionCount = 20,
                timeLimitMinutes = 0,
                passMark = 0,
                tags = listOf("Practice"),
                poolSelector = all
            )
        )
        add(
            AssessmentDefinition(
                id = "practice_weak",
                title = "Weak-Area Remediation",
                description = "Items drawn from domains where your recent accuracy has been lowest.",
                kind = "PRACTICE",
                domain = "GENERAL",
                questionCount = 20,
                timeLimitMinutes = 0,
                passMark = 0,
                tags = listOf("Practice"),
                poolSelector = all
            )
        )
    }

    fun byId(id: String): AssessmentDefinition? = definitions.firstOrNull { it.id == id }

    /**
     * Builds the daily assessment for an accelerator day, drawing from the
     * modules covered by that day.
     */
    fun dailyAssessment(day: Int, questionPool: List<Question>): AssessmentDefinition? {
        val target = questionPool.shuffled().take(8)
        if (target.isEmpty()) return null
        return AssessmentDefinition(
            id = "daily_$day",
            title = "Daily Assessment - Day $day",
            description = "A short assessment to close out the day's learning.",
            kind = "DAILY",
            domain = "GENERAL",
            questionCount = target.size,
            timeLimitMinutes = 12,
            passMark = 60,
            tags = listOf("Daily"),
            poolSelector = { pool ->
                if (pool.size <= target.size) pool else target
            }
        )
    }
}
