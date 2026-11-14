package com.vanguard.grc.domain.toolkit

import com.vanguard.grc.data.content.ToolDefinition
import com.vanguard.grc.data.content.ToolField
import com.vanguard.grc.data.content.ToolFieldType

/**
 * Registry of the practical GRC toolkit.
 *
 * Tool schemas live in code because the field types drive widget construction
 * and validation at runtime. The instructions, guidance and sample records in
 * each definition are written to be immediately usable as a working template.
 *
 * Every tool ships with:
 *  - a clear statement of purpose,
 *  - step-by-step usage guidance,
 *  - meaningful fields with help text,
 *  - a realistic sample record, and
 *  - export formats where meaningful.
 */
object ToolRegistry {

    const val CATEGORY_RISK = "Risk"
    const val CATEGORY_AUDIT = "Audit"
    const val CATEGORY_COMPLIANCE = "Compliance"
    const val CATEGORY_GOVERNANCE = "Governance"
    const val CATEGORY_THIRD_PARTY = "Third Party"
    const val CATEGORY_CONTINUITY = "Continuity"
    const val CATEGORY_PRIVACY = "Privacy"
    const val CATEGORY_LEADERSHIP = "Leadership"

    private val riskRatingOptions = listOf("Low", "Moderate", "High", "Very High", "Extreme")
    private val controlStatusOptions = listOf("Effective", "Partially effective", "Ineffective", "Not tested", "Not applicable")
    private val ownerOptions = listOf("CISO", "CIO", "CRO", "GRC Manager", "Head of IT", "Head of Operations", "Head of Finance", "Head of HR", "DPO", "Internal Audit")
    private val actionStatusOptions = listOf("Not started", "In progress", "Pending verification", "Closed", "Overdue")
    private val priorityOptions = listOf("Critical", "High", "Medium", "Low")

    fun all(): List<ToolDefinition> = listOf(
        riskRegister(),
        riskCalculator(),
        riskHeatmap(),
        riskTreatmentPlanner(),
        controlRegister(),
        statementOfApplicability(),
        isoGapAssessment(),
        nistCsfGapAssessment(),
        internalAuditPlanner(),
        auditChecklistBuilder(),
        auditFindingRegister(),
        capaTracker(),
        correctiveActionRegister(),
        policyRegister(),
        assetRegister(),
        classificationRegister(),
        vendorRiskAssessment(),
        vendorQuestionnaire(),
        businessImpactAnalysis(),
        rtoRpoPlanner(),
        businessContinuityChecklist(),
        complianceObligationRegister(),
        regulatoryChangeTracker(),
        kpiKriDashboard(),
        riskAcceptanceForm(),
        exceptionRegister(),
        managementReviewReport(),
        awarenessTracker(),
        evidenceRegister(),
        maturityAssessment()
    )

    fun byId(id: String): ToolDefinition? = all().firstOrNull { it.id == id }

    // =================================================================================
    // Risk
    // =================================================================================

    private fun riskRegister() = ToolDefinition(
        id = "risk_register",
        name = "Enterprise Risk Register",
        category = CATEGORY_RISK,
        description = "The central record of identified enterprise risks. Each entry links a risk to its owner, cause, consequence, rating, treatment strategy and current status so that the register can be reported to the risk committee without further analysis.",
        competencyDomain = "RISK_MANAGEMENT",
        referenceFramework = "ISO 31000, COSO ERM",
        howToUse = listOf(
            "Enter one line per risk. Use a short, event-shaped statement: what could happen, to what, because of what.",
            "Assign exactly one accountable risk owner. Shared ownership is a common failure mode - it means nobody owns it.",
            "Record the current rating using the 5x5 matrix, and separate inherent from residual rating.",
            "Review every entry at least quarterly. Anything not reviewed for two quarters should be challenged as stale.",
            "Link each accepted or transferred risk to the signed acceptance record so the audit trail is complete."
        ),
        fields = listOf(
            ToolField("riskId", "Risk ID", ToolFieldType.TEXT, true, helpText = "Stable identifier such as R-014. Never re-use an ID."),
            ToolField("category", "Risk category", ToolFieldType.SELECT, true, options = listOf("Cyber & Information Security", "Operational", "Financial", "Regulatory & Compliance", "Third Party", "Privacy & Data Protection", "Strategic", "People", "Resilience", "Reputational")),
            ToolField("riskStatement", "Risk statement", ToolFieldType.LONG_TEXT, true, helpText = "Cause -> event -> consequence, written in plain language."),
            ToolField("cause", "Cause / driver", ToolFieldType.LONG_TEXT, helpText = "Why the organisation is exposed. This drives control design."),
            ToolField("consequence", "Consequence", ToolFieldType.LONG_TEXT),
            ToolField("owner", "Risk owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("likelihood", "Likelihood (1-5)", ToolFieldType.SELECT, true, options = listOf("1 - Rare", "2 - Unlikely", "3 - Possible", "4 - Likely", "5 - Almost certain")),
            ToolField("impact", "Impact (1-5)", ToolFieldType.SELECT, true, options = listOf("1 - Negligible", "2 - Minor", "3 - Moderate", "4 - Major", "5 - Severe")),
            ToolField("inherentRating", "Inherent rating", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("controlRef", "Key controls", ToolFieldType.TEXT, helpText = "Control IDs from the control register."),
            ToolField("controlEffectiveness", "Control effectiveness %", ToolFieldType.NUMBER, helpText = "Measured effectiveness from the last control test, 0-100."),
            ToolField("residualRating", "Residual rating", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("treatment", "Treatment strategy", ToolFieldType.SELECT, true, options = listOf("Mitigate", "Transfer", "Avoid", "Accept", "Escalate")),
            ToolField("treatmentAction", "Treatment action and due date", ToolFieldType.TEXT),
            ToolField("kri", "Key risk indicator", ToolFieldType.TEXT, helpText = "A metric that tells you the risk is moving, with threshold."),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = listOf("Open", "Treatment in progress", "Monitoring", "Accepted", "Closed")),
            ToolField("nextReview", "Next review date", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "riskId" to "R-014",
            "category" to "Cyber & Information Security",
            "riskStatement" to "Unauthorised access to the internet banking platform could result from delayed revocation of privileged access following a role change, leading to fraudulent transactions and customer loss.",
            "cause" to "Joiner-mover-leaver process depends on manual HR notifications; privileged access reviews are performed semi-annually and inconsistently across business units.",
            "consequence" to "Fraudulent customer transactions, regulatory notification under RBI directions, customer trust erosion and potential reimbursement cost.",
            "owner" to "CISO",
            "likelihood" to "3 - Possible",
            "impact" to "5 - Severe",
            "inherentRating" to "High",
            "controlRef" to "AC-03, AC-07, HR-02",
            "controlEffectiveness" to "55",
            "residualRating" to "Moderate",
            "treatment" to "Mitigate",
            "treatmentAction" to "Automate HR-to-IAM feed and move privileged access review to monthly. Due 2026-04-30.",
            "kri" to "Privileged accounts overdue for review: green < 5%, amber 5-15%, red > 15%",
            "status" to "Treatment in progress",
            "nextReview" to "2026-04-15"
        )
    )

    private fun riskCalculator() = ToolDefinition(
        id = "risk_calculator",
        name = "Risk Assessment Calculator",
        category = CATEGORY_RISK,
        description = "Scores a risk on the 5x5 inherent and residual model, and supports a FAIR-style quantified estimate of loss expectancy. The tool records the reasoning so a reviewer can challenge the inputs rather than the arithmetic.",
        competencyDomain = "RISK_MANAGEMENT",
        referenceFramework = "ISO 31000, FAIR principles",
        howToUse = listOf(
            "Score likelihood and impact before considering controls. Anchors are provided so two assessors reach comparable answers.",
            "Enter measured control effectiveness (0-100) from a recent control test, not a confidence estimate.",
            "Use the quantified estimate when a single large loss could dominate the portfolio - for example ransomware, a data breach or a critical vendor failure.",
            "Read the worked output back to the risk owner. An estimate the owner has not seen is not a shared view of exposure.",
            "Record the assessment date and any assumptions so the score can be revalidated later."
        ),
        fields = listOf(
            ToolField("riskName", "Risk name", ToolFieldType.TEXT, true),
            ToolField("scope", "Scope / process", ToolFieldType.TEXT),
            ToolField("likelihood", "Likelihood", ToolFieldType.SELECT, true, options = listOf("1", "2", "3", "4", "5")),
            ToolField("impact", "Impact", ToolFieldType.SELECT, true, options = listOf("1", "2", "3", "4", "5")),
            ToolField("inherentScore", "Inherent score (L x I)", ToolFieldType.NUMBER, true),
            ToolField("inherentRating", "Inherent rating", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("controlEffectiveness", "Control effectiveness %", ToolFieldType.NUMBER, true, helpText = "Enter 0 if no controls are in place."),
            ToolField("residualScore", "Residual score", ToolFieldType.NUMBER, true),
            ToolField("residualRating", "Residual rating", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("eventFrequency", "Annual event frequency (quantified)", ToolFieldType.NUMBER, helpText = "Expected occurrences per year. Use 0.2 for a 1-in-5-year event."),
            ToolField("lossMagnitude", "Estimated loss magnitude per event", ToolFieldType.NUMBER),
            ToolField("ale", "Annualised loss expectancy", ToolFieldType.NUMBER),
            ToolField("assumptions", "Assumptions and judgement applied", ToolFieldType.LONG_TEXT),
            ToolField("assessedBy", "Assessed by", ToolFieldType.TEXT),
            ToolField("assessmentDate", "Assessment date", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "riskName" to "Ransomware encrypting the core payments platform",
            "scope" to "Retail and internet banking payment processing",
            "likelihood" to "3",
            "impact" to "5",
            "inherentScore" to "15",
            "inherentRating" to "Very High",
            "controlEffectiveness" to "45",
            "residualScore" to "4.5",
            "residualRating" to "Low",
            "eventFrequency" to "0.2",
            "lossMagnitude" to "4500000",
            "ale" to "900000",
            "assumptions" to "Assumes one disruptive event every five years; loss combines recovery cost, service credits and regulatory response. Excludes long-term brand damage.",
            "assessedBy" to "GRC Manager",
            "assessmentDate" to "2026-02-10"
        )
    )

    private fun riskHeatmap() = ToolDefinition(
        id = "risk_heatmap",
        name = "Risk Heatmap",
        category = CATEGORY_RISK,
        description = "Places risks on a 5x5 likelihood-impact grid for committee reporting, with inherent and residual positions shown together so treatment progress is visible at a glance.",
        competencyDomain = "RISK_MANAGEMENT",
        referenceFramework = "ISO 31000 risk evaluation",
        howToUse = listOf(
            "Enter every risk with its inherent likelihood and impact to see the inherent concentration.",
            "Overlay the residual position to show how much of the top-right quadrant treatment has actually moved.",
            "An empty top-right quadrant can indicate either effective treatment or under-reporting - challenge it explicitly in the committee.",
            "Overlay appetite lines before presenting: points outside appetite are the executive conversation.",
            "Freeze the heatmap for the reporting period and record the data set used so the numbers reconcile with the register."
        ),
        fields = listOf(
            ToolField("riskId", "Risk ID", ToolFieldType.TEXT, true),
            ToolField("riskName", "Risk", ToolFieldType.TEXT, true),
            ToolField("category", "Category", ToolFieldType.SELECT, true, options = listOf("Cyber", "Operational", "Financial", "Regulatory", "Third Party", "Privacy", "Strategic", "Resilience")),
            ToolField("inherentL", "Inherent likelihood", ToolFieldType.SELECT, true, options = listOf("1", "2", "3", "4", "5")),
            ToolField("inherentI", "Inherent impact", ToolFieldType.SELECT, true, options = listOf("1", "2", "3", "4", "5")),
            ToolField("residualL", "Residual likelihood", ToolFieldType.SELECT, true, options = listOf("1", "2", "3", "4", "5")),
            ToolField("residualI", "Residual impact", ToolFieldType.SELECT, true, options = listOf("1", "2", "3", "4", "5")),
            ToolField("appetiteStatus", "Within appetite?", ToolFieldType.SELECT, true, options = listOf("Within appetite", "At appetite limit", "Outside appetite")),
            ToolField("treatmentProgress", "Treatment progress", ToolFieldType.SELECT, true, options = listOf("Not started", "In progress", "Complete", "Accepted")),
            ToolField("owner", "Owner", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "riskId" to "R-014",
            "riskName" to "Privileged access not revoked promptly after role change",
            "category" to "Cyber",
            "inherentL" to "3",
            "inherentI" to "5",
            "residualL" to "2",
            "residualI" to "4",
            "appetiteStatus" to "Within appetite",
            "treatmentProgress" to "In progress",
            "owner" to "CISO"
        )
    )

    private fun riskTreatmentPlanner() = ToolDefinition(
        id = "risk_treatment",
        name = "Risk Treatment Planner",
        category = CATEGORY_RISK,
        description = "Converts a residual risk rating into a treatment decision with a named control action, owner, cost, deadline and verification method. Forces the discipline that treatment is a funded commitment rather than an intention.",
        competencyDomain = "RISK_MANAGEMENT",
        referenceFramework = "ISO 31000 risk treatment, NIST CSF 2.0 GOVERN",
        howToUse = listOf(
            "Start from the residual rating, not the inherent rating. Overstating inherent risk to justify spending is a common and costly error.",
            "Choose the strategy that matches the risk: avoid where the activity is not core, transfer where a counterparty is better able to absorb, mitigate where exposure is driven by controllable causes.",
            "Every treatment action needs a control owner who is accountable for making it operate, not just for funding it.",
            "Define how effectiveness will be verified and when. A treatment with no verification is an intention.",
            "Escalate where residual risk remains outside appetite - treatment alone will not close it."
        ),
        fields = listOf(
            ToolField("riskId", "Risk ID", ToolFieldType.TEXT, true),
            ToolField("riskName", "Risk", ToolFieldType.TEXT, true),
            ToolField("residualRating", "Residual rating", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("appetite", "Appetite statement", ToolFieldType.LONG_TEXT, true, helpText = "What the organisation is willing to accept for this risk category."),
            ToolField("strategy", "Treatment strategy", ToolFieldType.SELECT, true, options = listOf("Mitigate", "Transfer", "Avoid", "Accept", "Escalate")),
            ToolField("action", "Treatment action", ToolFieldType.LONG_TEXT, true),
            ToolField("controlAction", "Resulting control action", ToolFieldType.TEXT),
            ToolField("owner", "Action owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("targetResidual", "Target residual rating", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("cost", "Estimated cost", ToolFieldType.TEXT),
            ToolField("dueDate", "Due date", ToolFieldType.DATE, true),
            ToolField("verificationMethod", "How effectiveness will be verified", ToolFieldType.TEXT, true),
            ToolField("verificationDate", "Verification date", ToolFieldType.DATE),
            ToolField("escalateTo", "Escalation route if blocked", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "riskId" to "R-014",
            "riskName" to "Privileged access not revoked promptly after role change",
            "residualRating" to "Moderate",
            "appetite" to "The organisation tolerates moderate residual risk on privileged access provided no unaccounted privileged account persists beyond 5 business days.",
            "strategy" to "Mitigate",
            "action" to "Deploy automated HR-to-IAM leaver feed within 30 minutes of an HR record change, and move privileged access certification from semi-annual to monthly.",
            "controlAction" to "AC-02 (Automated deprovisioning), AC-07 (Monthly privileged access review)",
            "owner" to "CISO",
            "targetResidual" to "Low",
            "cost" to "INR 18L one-time, INR 2.4L per annum run cost",
            "dueDate" to "2026-06-30",
            "verificationMethod" to "Sample 30 leavers per month and confirm revocation within 30 minutes; review quarterly.",
            "verificationDate" to "2026-09-30",
            "escalateTo" to "CTO for delivery, CRO for residual risk acceptance"
        )
    )

    // =================================================================================
    // Compliance and controls
    // =================================================================================

    private fun controlRegister() = ToolDefinition(
        id = "control_register",
        name = "Control Assessment Register",
        category = CATEGORY_COMPLIANCE,
        description = "Records each control, its owner, the risk it addresses, the framework references it satisfies, its assessed design and operating effectiveness, and the evidence that supports the conclusion.",
        competencyDomain = "INFORMATION_SECURITY",
        referenceFramework = "ISO 27001 Annex A, NIST SP 800-53, CIS Controls",
        howToUse = listOf(
            "Assess design effectiveness and operating effectiveness separately. A control that is well designed but not operating provides no risk reduction.",
            "Cite evidence that a third party could inspect. Screenshots without context are weak evidence; a system-generated report with a date range is strong.",
            "Rate effectiveness as a percentage using the maturity model: 0-20 weak, 45 partial, 70 designed and operating, 85-95 managed.",
            "Link every control to at least one risk. Controls that address no identified risk are usually controls inherited from a framework without local analysis.",
            "Record the testing method so a reviewer can see how the conclusion was reached."
        ),
        fields = listOf(
            ToolField("controlId", "Control ID", ToolFieldType.TEXT, true),
            ToolField("controlTitle", "Control statement", ToolFieldType.TEXT, true),
            ToolField("domain", "Domain", ToolFieldType.SELECT, true, options = listOf("Organizational", "People", "Physical", "Technological")),
            ToolField("owner", "Control owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("riskRef", "Risk addressed", ToolFieldType.TEXT, true),
            ToolField("isoRefs", "ISO 27001 Annex A refs", ToolFieldType.TEXT),
            ToolField("nistRefs", "NIST SP 800-53 refs", ToolFieldType.TEXT),
            ToolField("cisRefs", "CIS Controls refs", ToolFieldType.TEXT),
            ToolField("testMethod", "Testing method", ToolFieldType.SELECT, true, options = listOf("Inspection", "Inquiry", "Observation", "Re-performance", "Analytical review")),
            ToolField("designEffective", "Design effective?", ToolFieldType.BOOLEAN, true),
            ToolField("operatingEffective", "Operating effective?", ToolFieldType.BOOLEAN, true),
            ToolField("effectivenessPct", "Effectiveness %", ToolFieldType.NUMBER, true),
            ToolField("status", "Assessment status", ToolFieldType.SELECT, true, options = controlStatusOptions),
            ToolField("evidenceRef", "Evidence reference", ToolFieldType.TEXT, true),
            ToolField("lastTested", "Last tested", ToolFieldType.DATE, true),
            ToolField("nextTest", "Next test due", ToolFieldType.DATE),
            ToolField("findingRef", "Linked finding", ToolFieldType.TEXT),
            ToolField("notes", "Assessor notes", ToolFieldType.LONG_TEXT)
        ),
        sampleData = mapOf(
            "controlId" to "AC-07",
            "controlTitle" to "Privileged access is reviewed monthly and non-compliant entitlements are removed within 5 business days.",
            "domain" to "Technological",
            "owner" to "CISO",
            "riskRef" to "R-014",
            "isoRefs" to "A.5.15, A.5.18",
            "nistRefs" to "AC-2, AC-6, AC-6(9)",
            "cisRefs" to "CIS 6.3, CIS 6.5",
            "testMethod" to "Inspection and re-performance",
            "designEffective" to "true",
            "operatingEffective" to "false",
            "effectivenessPct" to "55",
            "status" to "Partially effective",
            "evidenceRef" to "EV-2026-114 (access review export, Jan-Jun 2026), EV-2026-118 (exception log)",
            "lastTested" to "2026-06-28",
            "nextTest" to "2026-09-28",
            "findingRef" to "F-2026-07",
            "notes" to "Review executed on schedule but exceptions were closed on average 23 days after identification, outside the 5-day policy. Root cause traced to a manual tracking spreadsheet."
        )
    )

    private fun statementOfApplicability() = ToolDefinition(
        id = "soa_builder",
        name = "Statement of Applicability Builder",
        category = CATEGORY_COMPLIANCE,
        description = "Builds the ISO/IEC 27001:2022 Statement of Applicability - the mandatory record of which Annex A controls apply, which are excluded and why, together with the justification and implementation status for each.",
        competencyDomain = "COMPLIANCE",
        referenceFramework = "ISO/IEC 27001:2022 Clause 6.1.3",
        howToUse = listOf(
            "Start from the full Annex A control list and decide applicability against the ISMS scope, not against the organisation in general.",
            "An exclusion is permitted only where the risk is genuinely not applicable, and the reason must be recorded. 'Not applicable because we do not have a data centre' is acceptable; 'not applicable because it is difficult' is not.",
            "Where a control is excluded, substitute a documented alternative control or justify why none is needed - an exclusion without consideration is a nonconformity.",
            "Record implementation status honestly: Implemented, Partially implemented, Planned, or Not implemented. Overstating status is the most common cause of a failed surveillance audit.",
            "The SoA must reference the risk assessment that justified the applicability decisions."
        ),
        fields = listOf(
            ToolField("controlId", "Annex A control", ToolFieldType.TEXT, true),
            ToolField("controlTitle", "Control title", ToolFieldType.TEXT, true),
            ToolField("category", "Category", ToolFieldType.SELECT, true, options = listOf("Organizational", "People", "Physical", "Technological")),
            ToolField("applies", "Applicable?", ToolFieldType.BOOLEAN, true),
            ToolField("justification", "Justification / exclusion reason", ToolFieldType.LONG_TEXT, true),
            ToolField("alternativeControl", "Alternative control where excluded", ToolFieldType.TEXT),
            ToolField("implementationStatus", "Implementation status", ToolFieldType.SELECT, true, options = listOf("Implemented", "Partially implemented", "Planned", "Not implemented", "Not applicable - excluded")),
            ToolField("evidenceRef", "Evidence reference", ToolFieldType.TEXT),
            ToolField("riskRef", "Related risk", ToolFieldType.TEXT),
            ToolField("owner", "Owner", ToolFieldType.TEXT, true),
            ToolField("reviewDate", "Next SoA review", ToolFieldType.DATE, true),
            ToolField("linkedFinding", "Linked finding / gap", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "controlId" to "A.7.11",
            "controlTitle" to "Physical security - supporting utilities",
            "category" to "Physical",
            "applies" to "true",
            "justification" to "The organisation operates a colocation facility and an on-premise server room in its corporate office; utility resilience for both is in scope.",
            "alternativeControl" to "",
            "implementationStatus" to "Implemented",
            "evidenceRef" to "EV-UTIL-2026-01 (utility failure test report, May 2026)",
            "riskRef" to "R-022",
            "owner" to "Head of IT",
            "reviewDate" to "2027-01-31",
            "linkedFinding" to ""
        )
    )

    private fun isoGapAssessment() = ToolDefinition(
        id = "iso_gap",
        name = "ISO 27001 Gap Assessment",
        category = CATEGORY_COMPLIANCE,
        description = "A structured readiness assessment against the ISO/IEC 27001:2022 clauses and the Annex A control themes, producing a prioritised implementation plan rather than a binary pass/fail.",
        competencyDomain = "COMPLIANCE",
        referenceFramework = "ISO/IEC 27001:2022, ISO/IEC 27002:2022",
        howToUse = listOf(
            "Assess each clause and control theme against the evidence you can actually produce, not against what policy documents say.",
            "Score maturity on the five-level scale: 0 non-existent, 1 ad hoc, 2 repeatable, 3 defined, 4 managed, 5 optimised.",
            "A gap of two levels or more is a red flag for a certification timeline; one level is a managed improvement.",
            "Convert every red rating into a named action with an owner and a date. An unowned gap is not a gap assessment, it is a list.",
            "Re-run the assessment before the stage 1 audit and again before surveillance."
        ),
        fields = listOf(
            ToolField("area", "Clause / control area", ToolFieldType.TEXT, true),
            ToolField("requirement", "Requirement summary", ToolFieldType.LONG_TEXT, true),
            ToolField("currentState", "Current state", ToolFieldType.LONG_TEXT, true),
            ToolField("targetState", "Target state", ToolFieldType.LONG_TEXT, true),
            ToolField("maturityCurrent", "Current maturity (0-5)", ToolFieldType.SELECT, true, options = listOf("0", "1", "2", "3", "4", "5")),
            ToolField("maturityTarget", "Target maturity (0-5)", ToolFieldType.SELECT, true, options = listOf("0", "1", "2", "3", "4", "5")),
            ToolField("gap", "Gap (levels)", ToolFieldType.NUMBER, true),
            ToolField("evidenceAvailable", "Evidence available?", ToolFieldType.SELECT, true, options = listOf("Yes - sufficient", "Partial", "No")),
            ToolField("priority", "Priority", ToolFieldType.SELECT, true, options = priorityOptions),
            ToolField("action", "Required action", ToolFieldType.LONG_TEXT, true),
            ToolField("owner", "Owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("targetDate", "Target date", ToolFieldType.DATE, true),
            ToolField("effortDays", "Estimated effort (days)", ToolFieldType.NUMBER),
            ToolField("dependencies", "Dependencies", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "area" to "Clause 8.1 - Operational planning and control",
            "requirement" to "Information security risk assessment must be conducted and treated, with results recorded and reviewed.",
            "currentState" to "Risk assessment performed annually for the corporate estate only. The internet banking estate is assessed by a separate programme with a different methodology and no consolidated view.",
            "targetState" to "Single enterprise risk methodology applied across all in-scope estates, reviewed at least twice yearly and on material change, with results consolidated into one register.",
            "maturityCurrent" to "2",
            "maturityTarget" to "4",
            "gap" to "2",
            "evidenceAvailable" to "Partial",
            "priority" to "Critical",
            "action" to "Adopt a single risk methodology (5x5 with FAIR extension for top risks), extend scope to all in-scope estates, and run the first consolidated assessment.",
            "owner" to "GRC Manager",
            "targetDate" to "2026-08-31",
            "effortDays" to "25",
            "dependencies" to "Scope definition signed off by Executive sponsor"
        )
    )

    private fun nistCsfGapAssessment() = ToolDefinition(
        id = "nist_csf_gap",
        name = "NIST CSF 2.0 Gap Assessment",
        category = CATEGORY_COMPLIANCE,
        description = "Assesses the current profile against a target profile across the six NIST CSF 2.0 functions (GOVERN, IDENTIFY, PROTECT, DETECT, RESPOND, RECOVER) and produces the gap analysis required to build a roadmap.",
        competencyDomain = "INFORMATION_SECURITY",
        referenceFramework = "NIST Cybersecurity Framework 2.0, NIST CSF 2.0 Informative References",
        howToUse = listOf(
            "Work function by function, starting with GOVERN. The 2.0 framework is explicit that governance drives outcomes in the other five functions.",
            "Score each outcome as a maturity level (partial, risk informed, repeatable, adaptive) rather than yes/no. The CSF is outcome-based, not control-based.",
            "Record the Current Profile and Target Profile separately. Collapsing them is what makes CSF assessments useless for roadmapping.",
            "Map each gap to a CSF Informative Reference so the roadmap speaks the language of the framework.",
            "Sequence the roadmap by risk reduction per unit of effort, not by function order."
        ),
        fields = listOf(
            ToolField("function", "CSF 2.0 function", ToolFieldType.SELECT, true, options = listOf("GOVERN", "IDENTIFY", "PROTECT", "DETECT", "RESPOND", "RECOVER")),
            ToolField("outcome", "Subcategory / outcome", ToolFieldType.TEXT, true),
            ToolField("currentState", "Current profile", ToolFieldType.LONG_TEXT, true),
            ToolField("currentLevel", "Current level", ToolFieldType.SELECT, true, options = listOf("Not started", "Partial", "Risk informed", "Repeatable", "Adaptive")),
            ToolField("targetState", "Target profile", ToolFieldType.LONG_TEXT, true),
            ToolField("targetLevel", "Target level", ToolFieldType.SELECT, true, options = listOf("Not started", "Partial", "Risk informed", "Repeatable", "Adaptive")),
            ToolField("informativeRef", "Informative reference", ToolFieldType.TEXT),
            ToolField("gap", "Gap", ToolFieldType.SELECT, true, options = listOf("No gap", "Minor", "Moderate", "Major", "Critical")),
            ToolField("priority", "Roadmap priority", ToolFieldType.SELECT, true, options = priorityOptions),
            ToolField("action", "Roadmap action", ToolFieldType.LONG_TEXT, true),
            ToolField("owner", "Owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("targetDate", "Target date", ToolFieldType.DATE, true),
            ToolField("effortDays", "Effort (days)", ToolFieldType.NUMBER)
        ),
        sampleData = mapOf(
            "function" to "GOVERN",
            "outcome" to "GV.OC-03: Legal and regulatory requirements are understood and managed",
            "currentState" to "Regulatory obligations are tracked in a spreadsheet owned by Compliance. IT and Security are not consistently included, so technical requirements reach delivery teams late.",
            "currentLevel" to "Partial",
            "targetState" to "A single obligation register covers information security, privacy and sector regulation, mapped to accountable owners and mapped to controls, reviewed monthly for change.",
            "targetLevel" to "Risk informed",
            "informativeRef" to "CSF 2.0 GV.OC-03; ISO 27001:2022 Cl. 4.3, 6.3",
            "gap" to "Major",
            "priority" to "Critical",
            "action" to "Stand up an obligation register covering RBI, CERT-In, DPDP and PCI DSS, with a monthly change review and a control mapping column.",
            "owner" to "GRC Manager",
            "targetDate" to "2026-09-30",
            "effortDays" to "30"
        )
    )

    private fun policyRegister() = ToolDefinition(
        id = "policy_register",
        name = "Policy Management Register",
        category = CATEGORY_GOVERNANCE,
        description = "Tracks the enterprise policy estate: owner, approval, publication, review cycle, exception handling and the audience each policy is required to reach.",
        competencyDomain = "GOVERNANCE",
        referenceFramework = "ISO/IEC 27001:2022 Clause 5.2, 8.3, 8.4",
        howToUse = listOf(
            "Every policy needs a named owner with the authority to change it, not an IT contact who maintains the file.",
            "Set a review frequency based on risk and regulatory change, then actually review on cycle. A policy never reviewed is a policy that does not exist operationally.",
            "Record where the policy is published and how acknowledgement is captured. An unacknowledged policy has no evidential value in an audit.",
            "Link each policy to the controls it mandates so a control failure can be traced to a policy gap.",
            "Track exceptions separately - a policy with undocumented exceptions is a policy being quietly ignored."
        ),
        fields = listOf(
            ToolField("policyId", "Policy ID", ToolFieldType.TEXT, true),
            ToolField("title", "Policy title", ToolFieldType.TEXT, true),
            ToolField("category", "Category", ToolFieldType.SELECT, true, options = listOf("Information Security", "Privacy", "Risk", "Business Continuity", "Third Party", "HR", "Financial", "Operational", "Ethics", "Change Management")),
            ToolField("owner", "Policy owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("approver", "Approved by", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("version", "Version", ToolFieldType.TEXT, true),
            ToolField("effectiveDate", "Effective date", ToolFieldType.DATE, true),
            ToolField("nextReview", "Next review date", ToolFieldType.DATE, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = listOf("Draft", "In review", "Approved", "Published", "Superseded", "Retired")),
            ToolField("acknowledgement", "Acknowledgement method", ToolFieldType.SELECT, true, options = listOf("E-learning acknowledgement", "Read and sign", "Intranet publication only", "Not required")),
            ToolField("auditTrail", "Audit trail location", ToolFieldType.TEXT),
            ToolField("linkedControls", "Linked controls", ToolFieldType.TEXT),
            ToolField("openExceptions", "Open exceptions", ToolFieldType.NUMBER),
            ToolField("notes", "Notes", ToolFieldType.LONG_TEXT)
        ),
        sampleData = mapOf(
            "policyId" to "POL-IS-007",
            "title" to "Acceptable Use Policy",
            "category" to "Information Security",
            "owner" to "CISO",
            "approver" to "CIO",
            "version" to "3.2",
            "effectiveDate" to "2026-04-01",
            "nextReview" to "2027-04-01",
            "status" to "Published",
            "acknowledgement" to "E-learning acknowledgement",
            "auditTrail" to "LMS completion report retained in the compliance evidence repository",
            "linkedControls" to "A.6.1, A.6.3, A.7.4",
            "openExceptions" to "3",
            "notes" to "Revised to cover generative AI use and personal device access following the 2026 AI policy rollout."
        )
    )

    private fun exceptionRegister() = ToolDefinition(
        id = "exception_register",
        name = "Exception Management Register",
        category = CATEGORY_GOVERNANCE,
        description = "Records formal departures from policy, with compensating controls, expiry dates and the approving authority. Exceptions are a legitimate risk tool and a common audit failure when unmanaged.",
        competencyDomain = "GOVERNANCE",
        referenceFramework = "ISO 27001:2022 Clause 8.1, NIST SP 800-53 CA-3",
        howToUse = listOf(
            "Capture only formal exceptions. Informal workarounds are findings, not exceptions - they need to be surfaced and either approved or closed.",
            "Every exception needs a compensating control that addresses the same risk, not merely a note explaining why the risk is tolerable.",
            "Set a hard expiry date. Exceptions that renew indefinitely are unapproved policy breaches.",
            "Review exceptions at the same forum as the risk register, not in a separate meeting that never happens.",
            "Report the count, age and owner of exceptions to the executive - the number itself is a governance signal."
        ),
        fields = listOf(
            ToolField("exceptionId", "Exception ID", ToolFieldType.TEXT, true),
            ToolField("policyRef", "Policy / control reference", ToolFieldType.TEXT, true),
            ToolField("requirement", "Requirement not met", ToolFieldType.LONG_TEXT, true),
            ToolField("justification", "Business justification", ToolFieldType.LONG_TEXT, true),
            ToolField("riskIfNotApproved", "Risk if not approved", ToolFieldType.LONG_TEXT),
            ToolField("compensatingControl", "Compensating control", ToolFieldType.LONG_TEXT, true),
            ToolField("owner", "Exception owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("approver", "Approved by", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("raisedDate", "Date raised", ToolFieldType.DATE, true),
            ToolField("expiryDate", "Expiry date", ToolFieldType.DATE, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = listOf("Pending approval", "Approved", "Rejected", "Expired", "Renewed")),
            ToolField("reviewOutcome", "Latest review outcome", ToolFieldType.LONG_TEXT)
        ),
        sampleData = mapOf(
            "exceptionId" to "EXC-2026-019",
            "policyRef" to "POL-AC-002 Access Control Policy, section 4.2",
            "requirement" to "System accounts with privileged database access must have individually attributed credentials and named human owners.",
            "justification" to "Legacy ERP scheduler accounts were created as shared identities during the 2019 core banking migration. Re-platforming to the new payments platform is funded for Q3 2026 and would require a second change window.",
            "riskIfNotApproved" to "Shared privileged credentials prevent attribution of administrative actions on the ledger tables, weakening fraud detection and audit trail integrity.",
            "compensatingControl" to "Credential stored in the enterprise vault with break-glass access requiring two-person approval; session logging forwarded to the SIEM with alerting on out-of-hours activity; monthly administrator attribution review by the database team lead.",
            "owner" to "Head of IT",
            "approver" to "CISO",
            "raisedDate" to "2026-01-15",
            "expiryDate" to "2026-09-30",
            "status" to "Approved",
            "reviewOutcome" to "Reviewed 2026-07-15. Attribution review found 100% of privileged sessions traceable to an approved named account. Re-platforming confirmed for 2026-09-14. Exception extended to the new expiry date."
        )
    )

    private fun riskAcceptanceForm() = ToolDefinition(
        id = "risk_acceptance",
        name = "Risk Acceptance Form",
        category = CATEGORY_RISK,
        description = "Captures a formal, time-bound risk acceptance decision with the rationale, the acceptance level, the review date and the conditions attached to the acceptance.",
        competencyDomain = "RISK_MANAGEMENT",
        referenceFramework = "ISO 31000 Clause 6.4.2, ISO 27001:2022 Clause 6.1.3(d)",
        howToUse = listOf(
            "Only a person with the delegated authority to accept the risk may sign. Route the acceptance to the level of authority that owns the exposure.",
            "State the acceptance level explicitly: accept at the current residual rating, accept with conditions, or accept for a defined period only.",
            "Attach the compensating monitoring. An acceptance without monitoring is a decision to stop looking.",
            "Never accept a risk that breaches law, a licence condition or a regulatory direction. Record that as an escalation, not an acceptance.",
            "Set a review date inside the normal risk review cycle and diarise it."
        ),
        fields = listOf(
            ToolField("acceptanceId", "Acceptance ID", ToolFieldType.TEXT, true),
            ToolField("riskId", "Risk ID", ToolFieldType.TEXT, true),
            ToolField("riskStatement", "Risk statement", ToolFieldType.LONG_TEXT, true),
            ToolField("residualRating", "Residual rating accepted", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("appetite", "Appetite reference", ToolFieldType.LONG_TEXT, true),
            ToolField("acceptanceLevel", "Acceptance decision", ToolFieldType.SELECT, true, options = listOf("Accepted", "Accepted with conditions", "Accepted for defined period only", "Rejected", "Escalated")),
            ToolField("rationale", "Rationale", ToolFieldType.LONG_TEXT, true),
            ToolField("conditions", "Conditions attached", ToolFieldType.LONG_TEXT),
            ToolField("compensatingControl", "Compensating control / monitoring", ToolFieldType.LONG_TEXT),
            ToolField("financialImpact", "Quantified exposure accepted", ToolFieldType.TEXT),
            ToolField("acceptedBy", "Accepted by", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("acceptedDate", "Date accepted", ToolFieldType.DATE, true),
            ToolField("reviewDate", "Review date", ToolFieldType.DATE, true),
            ToolField("linkedControls", "Related controls and findings", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "acceptanceId" to "RA-2026-006",
            "riskId" to "R-031",
            "riskStatement" to "Prolonged unavailability of the retail payments platform during a major regional network outage could prevent customers from transacting.",
            "residualRating" to "High",
            "appetite" to "The organisation tolerates High residual risk for availability of retail payments for up to 4 hours in any 12-month period, with no single incident exceeding 8 hours.",
            "acceptanceLevel" to "Accepted with conditions",
            "rationale" to "A second active-active data centre would cost INR 42 Crore with an 18-month delivery. The board has approved a phased resilience programme; the remaining exposure is accepted for the intervening period because failover to the primary DR site delivers 94% of transaction volume.",
            "conditions" to "Monthly failover test must pass; customer communications plan maintained; recovery time objective formally capped at 4 hours with a named executive for breach notification.",
            "compensatingControl" to "Quarterly failover exercise; automated transaction queueing at the channel layer; daily reconciliation of failed transactions; customer notification template ready for activation.",
            "financialImpact" to "INR 1.8 Crore worst-case direct loss; 11,400 customer transactions affected in the modelled scenario",
            "acceptedBy" to "CRO",
            "acceptedDate" to "2026-03-20",
            "reviewDate" to "2026-09-30",
            "linkedControls" to "BC-04, BC-11; finding F-2026-04"
        )
    )

    // =================================================================================
    // Audit
    // =================================================================================

    private fun internalAuditPlanner() = ToolDefinition(
        id = "audit_planner",
        name = "Internal Audit Planner",
        category = CATEGORY_AUDIT,
        description = "Builds the annual internal audit plan: the audit universe, risk-based allocation of days, coverage of mandatory audits, and the schedule of fieldwork and reporting milestones.",
        competencyDomain = "AUDIT",
        referenceFramework = "IIA Global Internal Audit Standards, ISO 19011",
        howToUse = listOf(
            "Define the audit universe first - every auditable unit in scope - then allocate days. Allocation without a universe produces a plan that quietly omits something.",
            "Allocate days using a risk model: business criticality, inherent risk score, prior audit findings, regulatory obligation and change volume.",
            "Reserve capacity for unplanned and regulatory-driven audits, typically 15-25% of annual effort.",
            "Ensure mandatory audits are covered: financial statement support, regulatory examinations, certification surveillance.",
            "Publish the plan to executives with the rationale attached - a plan without rationale is a list of dates."
        ),
        fields = listOf(
            ToolField("auditId", "Audit reference", ToolFieldType.TEXT, true),
            ToolField("title", "Audit title", ToolFieldType.TEXT, true),
            ToolField("auditee", "Auditee / area", ToolFieldType.TEXT, true),
            ToolField("objective", "Audit objective", ToolFieldType.LONG_TEXT, true),
            ToolField("riskScore", "Inherent risk score", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("auditType", "Audit type", ToolFieldType.SELECT, true, options = listOf("Risk-based", "Mandatory / regulatory", "Follow-up", "Advisory", "Certification support", "For-cause")),
            ToolField("daysAllocated", "Days allocated", ToolFieldType.NUMBER, true),
            ToolField("teamSize", "Team size", ToolFieldType.NUMBER),
            ToolField("leadAuditor", "Lead auditor", ToolFieldType.TEXT, true),
            ToolField("fieldworkStart", "Fieldwork start", ToolFieldType.DATE, true),
            ToolField("fieldworkEnd", "Fieldwork end", ToolFieldType.DATE, true),
            ToolField("draftReportDue", "Draft report due", ToolFieldType.DATE, true),
            ToolField("finalReportDue", "Final report due", ToolFieldType.DATE, true),
            ToolField("managementResponseDue", "Management response due", ToolFieldType.DATE, true),
            ToolField("priority", "Priority", ToolFieldType.SELECT, true, options = priorityOptions),
            ToolField("basis", "Basis of allocation (rationale)", ToolFieldType.LONG_TEXT, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = listOf("Planned", "In fieldwork", "Reporting", "Closed"))
        ),
        sampleData = mapOf(
            "auditId" to "IA-2026-04",
            "title" to "IT General Controls review - Retail Banking core",
            "auditee" to "Retail Banking Technology",
            "objective" to "Test the design and operating effectiveness of access control, change management, job scheduling and program development controls over the core banking platform for the period July 2025 to June 2026.",
            "riskScore" to "High",
            "auditType" to "Risk-based",
            "daysAllocated" to "45",
            "teamSize" to "3",
            "leadAuditor" to "Senior IT Auditor",
            "fieldworkStart" to "2026-08-04",
            "fieldworkEnd" to "2026-09-12",
            "draftReportDue" to "2026-09-26",
            "finalReportDue" to "2026-10-10",
            "managementResponseDue" to "2026-10-24",
            "priority" to "Critical",
            "basis" to "Allocated 45 days on the basis of high inherent risk from the 2025 payment platform migration, two open high findings from IA-2025-09, and an RBI observation on privileged access. Highest-risk area in the plan.",
            "status" to "Planned"
        )
    )

    private fun auditChecklistBuilder() = ToolDefinition(
        id = "audit_checklist",
        name = "Audit Checklist Builder",
        category = CATEGORY_AUDIT,
        description = "A reusable test-programme checklist covering ITGC, application controls, security, third party and cloud, with the evidence request and test procedure for each step.",
        competencyDomain = "AUDIT",
        referenceFramework = "ISACA CISA domain 4 & 5, COBIT",
        howToUse = listOf(
            "Start from the control objective, not the control description. Objectives make the checklist reusable across systems.",
            "For each step record the evidence requested, the test procedure and the expected result. A step without a test procedure is a meeting agenda.",
            "Mark results as Pass, Fail, Exception, or Not tested. 'Not tested' must carry a reason - it is a scope limitation that belongs in the report.",
            "Sample sizes should be documented in the programme, with the rationale. Random and judgemental samples must both be justified.",
            "Keep the checklist under version control and archive the version used for each report."
        ),
        fields = listOf(
            ToolField("checklistId", "Checklist ID", ToolFieldType.TEXT, true),
            ToolField("controlArea", "Control area", ToolFieldType.SELECT, true, options = listOf("Access Control", "Change Management", "Job Scheduling", "Program Development", "Operations & Monitoring", "Backup & Recovery", "Third Party", "Cloud", "ITGC Governance", "Data Migration", "Interfaces", "Physical & Environmental")),
            ToolField("objective", "Control objective", ToolFieldType.LONG_TEXT, true),
            ToolField("reference", "Framework reference", ToolFieldType.TEXT, true),
            ToolField("evidenceRequested", "Evidence requested", ToolFieldType.TEXT, true),
            ToolField("testProcedure", "Test procedure", ToolFieldType.LONG_TEXT, true),
            ToolField("sampleSize", "Sample size", ToolFieldType.TEXT),
            ToolField("samplingMethod", "Sampling method", ToolFieldType.SELECT, true, options = listOf("Random", "Judgemental", "Hundred percent", "Not applicable")),
            ToolField("expectedResult", "Expected result", ToolFieldType.LONG_TEXT, true),
            ToolField("result", "Result", ToolFieldType.SELECT, true, options = listOf("Pass", "Fail", "Exception", "Not tested")),
            ToolField("evidenceHeld", "Evidence held", ToolFieldType.TEXT),
            ToolField("findingRef", "Finding reference", ToolFieldType.TEXT),
            ToolField("auditorNotes", "Auditor notes", ToolFieldType.LONG_TEXT)
        ),
        sampleData = mapOf(
            "checklistId" to "AC-01",
            "controlArea" to "Access Control",
            "objective" to "Access to systems and data is granted on the basis of approved authorisation requests and is removed promptly when no longer required.",
            "reference" to "COBIT 2019 EDM-DSS 06.01; NIST SP 800-53 AC-2, AC-6; ISO 27001 A.5.15",
            "evidenceRequested" to "HR leaver file, joiner-mover-leaver ticket sample, IAM provisioning logs, quarterly user access review sign-off, privileged account inventory",
            "testProcedure" to "Select 30 leavers from the July 2025 to June 2026 population. For each, trace the HR exit record to the deprovisioning ticket and to the system account record, and confirm the account was disabled within the policy timeframe. Separately, confirm 100% of privileged accounts on the platform appear in the access review pack.",
            "sampleSize" to "30 leavers; 100% of privileged accounts",
            "samplingMethod" to "Random",
            "expectedResult" to "All accounts disabled within 5 business days of the HR exit record; all privileged accounts present in the certification population; exceptions carried to the exception log with compensating controls.",
            "result" to "Exception",
            "evidenceHeld" to "EV-2026-114, EV-2026-118, EV-2026-121",
            "findingRef" to "F-2026-07",
            "auditorNotes" to "27 of 30 leavers compliant. Three accounts disabled at 9, 14 and 23 days respectively. Exceptions logged but compensating control evidence missing for one."
        )
    )

    private fun auditFindingRegister() = ToolDefinition(
        id = "findings_register",
        name = "Audit Finding Register",
        category = CATEGORY_AUDIT,
        description = "Records audit and assurance findings with criteria, condition, cause, effect and recommendation, supporting consistent, defensible write-ups and a single remediation tracker.",
        competencyDomain = "AUDIT",
        referenceFramework = "IIA Global Internal Audit Standards",
        howToUse = listOf(
            "State the criteria - what should have happened and where is it defined - before stating the condition. Without criteria the finding is an opinion.",
            "Write the condition as fact with evidence, not as accusation. '27 of 30 accounts were disabled late' is a condition; 'the team is careless' is not.",
            "Separate cause from effect. Cause drives the remediation; effect justifies the priority.",
            "Rate severity by impact and likelihood of recurrence, not by how awkward the conversation will be.",
            "Ensure every recommendation is addressed by a named owner with a date before the report is finalised."
        ),
        fields = listOf(
            ToolField("findingId", "Finding ID", ToolFieldType.TEXT, true),
            ToolField("source", "Source", ToolFieldType.SELECT, true, options = listOf("Internal audit", "External audit", "Certification surveillance", "Regulatory exam", "Penetration test", "Vendor assessment", "Internal control review", "Incident review")),
            ToolField("title", "Finding title", ToolFieldType.TEXT, true),
            ToolField("criteria", "Criteria (what should be)", ToolFieldType.LONG_TEXT, true),
            ToolField("condition", "Condition (what is)", ToolFieldType.LONG_TEXT, true),
            ToolField("cause", "Cause", ToolFieldType.LONG_TEXT, true),
            ToolField("effect", "Effect / impact", ToolFieldType.LONG_TEXT, true),
            ToolField("evidence", "Evidence reference", ToolFieldType.TEXT, true),
            ToolField("severity", "Severity", ToolFieldType.SELECT, true, options = priorityOptions),
            ToolField("recommendation", "Recommendation", ToolFieldType.LONG_TEXT, true),
            ToolField("owner", "Management action owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("dueDate", "Due date", ToolFieldType.DATE, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = actionStatusOptions),
            ToolField("managementResponse", "Management response", ToolFieldType.LONG_TEXT),
            ToolField("validation", "Validation outcome", ToolFieldType.TEXT),
            ToolField("validatedBy", "Validated by", ToolFieldType.TEXT),
            ToolField("validatedDate", "Validation date", ToolFieldType.DATE)
        ),
        sampleData = mapOf(
            "findingId" to "F-2026-07",
            "source" to "Internal audit",
            "title" to "Privileged account revocation exceeded the 5-day policy threshold",
            "criteria" to "POL-AC-002 Access Control Policy section 4.2 requires privileged access to be removed within 5 business days of an approved leaver record.",
            "condition" to "3 of 30 leavers sampled had their privileged access revoked after 9, 14 and 23 business days respectively. The quarterly access review correctly identified 11 of the 14 late accounts but recorded the exception without a compensating control in 2 cases.",
            "cause" to "Deprovisioning is initiated manually by a single role administrator, and there is no system-generated ageing report for open leaver tickets. The exception log records the exception but does not require a compensating control entry.",
            "effect" to "Former employees and contractors retained privileged production access for up to 23 business days. The 2025 unauthorised configuration change on the payments platform was executed using an account belonging to a contractor who had left the organisation 11 days earlier.",
            "evidence" to "EV-2026-114, EV-2026-118, EV-2026-121",
            "severity" to "High",
            "recommendation" to "Implement an automated HR-to-IAM deprovisioning feed with a 30-minute SLA, introduce a daily ageing report for open leaver tickets, and require a compensating control reference for every access review exception.",
            "owner" to "CISO",
            "dueDate" to "2026-06-30",
            "status" to "In progress",
            "managementResponse" to "Agreed. Automation build is funded under the FY26 IAM programme with a delivery date of 2026-05-31. Ageing report to be added to the existing IAM operations dashboard in the interim. Compensating control requirement will be incorporated into the access review standard by 2026-04-15.",
            "validation" to "Re-test scheduled",
            "validatedBy" to "Internal Audit",
            "validatedDate" to ""
        )
    )

    private fun capaTracker() = ToolDefinition(
        id = "capa_tracker",
        name = "CAPA Tracker",
        category = CATEGORY_AUDIT,
        description = "Tracks corrective and preventive actions arising from audits, incidents, certification nonconformities and regulatory observations, with root cause, effectiveness check and closure approval.",
        competencyDomain = "AUDIT",
        referenceFramework = "ISO 19011, ISO/IEC 27001:2022 Clause 10.2, 8.7",
        howToUse = listOf(
            "Separate corrective action (fix the cause of what happened) from corrective action that only fixes the symptom. A retrained user is not a root cause fix.",
            "Perform and record a root cause analysis for every high and critical action. Use 5 Whys, fishbone, or fault tree as appropriate.",
            "Define the effectiveness check before the action starts, including who performs it and when. An action with no effectiveness check is a task, not a CAPA.",
            "Do not close a CAPA on evidence that the action was performed. Close it on evidence that the condition no longer recurs.",
            "Escalate overdue actions using the agreed escalation path rather than quietly extending dates."
        ),
        fields = listOf(
            ToolField("capaId", "CAPA ID", ToolFieldType.TEXT, true),
            ToolField("source", "Source", ToolFieldType.SELECT, true, options = listOf("Internal audit", "External audit", "Certification nonconformity", "Incident", "Regulatory observation", "Risk assessment", "Vendor assessment", "Management review")),
            ToolField("title", "Action title", ToolFieldType.TEXT, true),
            ToolField("rootCause", "Root cause analysis", ToolFieldType.LONG_TEXT, true),
            ToolField("rootCauseMethod", "Analysis method", ToolFieldType.SELECT, true, options = listOf("5 Whys", "Fishbone", "Fault tree", "Pareto", "Direct determination")),
            ToolField("actionType", "Action type", ToolFieldType.SELECT, true, options = listOf("Corrective", "Preventive", "Corrective action effectiveness")),
            ToolField("actionDescription", "Action taken", ToolFieldType.LONG_TEXT, true),
            ToolField("owner", "Owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("dueDate", "Due date", ToolFieldType.DATE, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = actionStatusOptions),
            ToolField("effectivenessCheck", "Effectiveness check", ToolFieldType.LONG_TEXT, true),
            ToolField("effectivenessDate", "Effectiveness check date", ToolFieldType.DATE),
            ToolField("verifiedBy", "Verified by", ToolFieldType.TEXT),
            ToolField("closureApproval", "Closure approved by", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("closureDate", "Closure date", ToolFieldType.DATE),
            ToolField("overdueDays", "Days overdue", ToolFieldType.NUMBER)
        ),
        sampleData = mapOf(
            "capaId" to "CAPA-2026-031",
            "source" to "Certification nonconformity",
            "title" to "Supplier security review process not applied to two new SaaS providers",
            "rootCauseMethod" to "5 Whys",
            "rootCause" to "The supplier onboarding checklist in the third-party procedure was last revised in 2023 and does not include a step to trigger a security questionnaire for SaaS subscriptions below the INR 10L annual threshold. Because the spend threshold was the only trigger, low-cost SaaS tools were onboarded with no security evaluation. The absence of a system trigger meant the gap was invisible until the surveillance audit sampled it.",
            "actionType" to "Corrective",
            "actionDescription" to "Amend the third-party onboarding procedure so that all SaaS and cloud subscriptions require a proportionate security assessment regardless of spend value. Implement a procurement workflow gate that blocks purchase order approval where the assessment record is absent. Re-assess the 14 SaaS services onboarded since January 2025.",
            "owner" to "GRC Manager",
            "dueDate" to "2026-05-31",
            "status" to "In progress",
            "effectivenessCheck" to "Re-sample 20 procurement records after 2026-06-30 and confirm 100% have a completed proportionate assessment before purchase order approval. Confirm with Internal Audit by review of the workflow exception report.",
            "effectivenessDate" to "2026-07-31",
            "verifiedBy" to "Internal Audit",
            "closureApproval" to "CISO",
            "closureDate" to "",
            "overdueDays" to "0"
        )
    )

    private fun correctiveActionRegister() = ToolDefinition(
        id = "corrective_actions",
        name = "Corrective Action Register",
        category = CATEGORY_AUDIT,
        description = "A forward-looking register of preventive and corrective actions agreed by management, distinguishing committed actions from actions already delivered and validated.",
        competencyDomain = "AUDIT",
        referenceFramework = "ISO/IEC 27001:2022 Clause 8.7, 10.2",
        howToUse = listOf(
            "Capture the action, the owner and the date agreed with management - the register is the management commitment, not the auditor's wish list.",
            "Distinguish actions that need budget or headcount from actions that need a decision. The two need completely different escalation routes.",
            "Track overdue actions by age and severity. Age is a better signal of a broken system than raw counts.",
            "Report the register to the management review meeting with an explicit decision on every overdue item: accept the new date, escalate, or re-resource.",
            "Archive closed actions with the validation evidence so the history is auditable."
        ),
        fields = listOf(
            ToolField("actionId", "Action ID", ToolFieldType.TEXT, true),
            ToolField("linkedFinding", "Linked finding / risk", ToolFieldType.TEXT, true),
            ToolField("action", "Agreed action", ToolFieldType.LONG_TEXT, true),
            ToolField("actionType", "Type", ToolFieldType.SELECT, true, options = listOf("Preventive", "Corrective", "Directive / decision required", "Risk accepted")),
            ToolField("owner", "Owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("agreedDate", "Date agreed", ToolFieldType.DATE, true),
            ToolField("dueDate", "Due date", ToolFieldType.DATE, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = actionStatusOptions),
            ToolField("progressNote", "Progress note", ToolFieldType.LONG_TEXT),
            ToolField("budgetRequired", "Budget required?", ToolFieldType.BOOLEAN),
            ToolField("decisionRequired", "Executive decision required?", ToolFieldType.BOOLEAN),
            ToolField("escalatedTo", "Escalated to", ToolFieldType.TEXT),
            ToolField("closedDate", "Closed date", ToolFieldType.DATE),
            ToolField("closureEvidence", "Closure evidence reference", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "actionId" to "CA-2026-044",
            "linkedFinding" to "F-2026-07",
            "action" to "Deploy automated HR-to-IAM deprovisioning with a 30-minute SLA and introduce a daily ageing report for open leaver tickets.",
            "actionType" to "Corrective",
            "owner" to "CISO",
            "agreedDate" to "2026-02-28",
            "dueDate" to "2026-06-30",
            "status" to "In progress",
            "progressNote" to "Integration build completed and deployed to the non-production IAM environment on 2026-05-14. Production deployment scheduled for 2026-06-12 subject to the change advisory board. Daily ageing report released to the IAM operations team on 2026-04-02.",
            "budgetRequired" to "true",
            "decisionRequired" to "false",
            "escalatedTo" to "",
            "closedDate" to "",
            "closureEvidence" to ""
        )
    )

    private fun evidenceRegister() = ToolDefinition(
        id = "evidence_register",
        name = "Audit Evidence Register",
        category = CATEGORY_AUDIT,
        description = "Indexes evidence held for assurance purposes with its provenance, period covered, custodian, integrity confirmation and the retention rule applied.",
        competencyDomain = "AUDIT",
        referenceFramework = "ISO 19011 Clause 6.5, NIST SP 800-53 AU-2, AU-11",
        howToUse = listOf(
            "Record the period the evidence covers, not just the date it was produced. A screenshot without a period cannot be tested.",
            "Confirm provenance - who produced it, from which system, and whether it can be regenerated on demand.",
            "Protect integrity with a hash or a system-generated export reference where the source is a database or log file.",
            "Link evidence to the controls and findings it supports so an examiner's request can be answered from the index rather than by hunting.",
            "Apply the retention rule from the records schedule. Deleting evidence early is a finding in its own right."
        ),
        fields = listOf(
            ToolField("evidenceId", "Evidence ID", ToolFieldType.TEXT, true),
            ToolField("title", "Evidence description", ToolFieldType.TEXT, true),
            ToolField("relatedControl", "Related control / finding", ToolFieldType.TEXT, true),
            ToolField("type", "Evidence type", ToolFieldType.SELECT, true, options = listOf("System-generated report", "Screenshot", "Signed document", "Configuration extract", "Log extract", "Attestation", "Survey response", "Third-party report", "Interview record", "Physical inspection")),
            ToolField("sourceSystem", "Source system / originator", ToolFieldType.TEXT, true),
            ToolField("periodCovered", "Period covered", ToolFieldType.TEXT, true),
            ToolField("dateProduced", "Date produced", ToolFieldType.DATE, true),
            ToolField("dateCollected", "Date collected", ToolFieldType.DATE, true),
            ToolField("collectedBy", "Collected by", ToolFieldType.TEXT, true),
            ToolField("custodian", "Custodian", ToolFieldType.TEXT),
            ToolField("integrityCheck", "Integrity confirmation", ToolFieldType.TEXT),
            ToolField("storageLocation", "Storage location", ToolFieldType.TEXT, true),
            ToolField("retentionRule", "Retention rule", ToolFieldType.TEXT),
            ToolField("reviewStatus", "Review status", ToolFieldType.SELECT, true, options = listOf("Accepted", "Accepted with query", "Pending review", "Rejected"))
        ),
        sampleData = mapOf(
            "evidenceId" to "EV-2026-114",
            "title" to "Quarterly privileged access certification pack - platform production",
            "relatedControl" to "AC-07; F-2026-07",
            "type" to "System-generated report",
            "sourceSystem" to "Identity Governance Platform (IGP) - Certification module export, signed by the certification owner",
            "periodCovered" to "2026-01-01 to 2026-03-31",
            "dateProduced" to "2026-04-02",
            "dateCollected" to "2026-06-28",
            "collectedBy" to "Internal Audit",
            "custodian" to "GRC Evidence Repository",
            "integrityCheck" to "SHA-256 recorded at export; re-export on 2026-07-01 produced identical document set",
            "storageLocation" to "GRC Evidence Repository / IA-2026-04 / EV-2026-114",
            "retentionRule" to "Audit evidence retained for 7 years from audit closure in line with the enterprise records schedule",
            "reviewStatus" to "Accepted"
        )
    )

    // =================================================================================
    // Third party
    // =================================================================================

    private fun vendorRiskAssessment() = ToolDefinition(
        id = "vendor_assessment",
        name = "Vendor Risk Assessment",
        category = CATEGORY_THIRD_PARTY,
        description = "A tiered third-party assessment combining inherent risk, service criticality, data sensitivity and control maturity into a vendor risk tier and a required due diligence scope.",
        competencyDomain = "THIRD_PARTY",
        referenceFramework = "NIST SP 800-161, ISO 27001:2022 A.5.19-5.22, CAIQ",
        howToUse = listOf(
            "Tier the vendor before assessing: criticality to the business, data accessed, integration depth and regulatory exposure.",
            "Inherent risk comes from the service and the data, not from the vendor's own questionnaire answers.",
            "Set the assessment scope from the tier. Tier 1 needs a questionnaire and evidence review; Tier 4 needs a full assessment with independent assurance review and continuous monitoring.",
            "Review the SOC 2 report carefully - check the period covered, whether it is Type 1 or Type 2, and every exception listed in the system description.",
            "Reassess on a fixed cycle or on trigger events: a breach at the vendor, an acquisition, a new service line, a regulatory change."
        ),
        fields = listOf(
            ToolField("vendorId", "Vendor ID", ToolFieldType.TEXT, true),
            ToolField("vendorName", "Vendor name", ToolFieldType.TEXT, true),
            ToolField("service", "Service provided", ToolFieldType.TEXT, true),
            ToolField("businessUnit", "Requesting business unit", ToolFieldType.TEXT, true),
            ToolField("criticality", "Business criticality", ToolFieldType.SELECT, true, options = listOf("Low", "Medium", "High", "Critical")),
            ToolField("dataAccess", "Data access", ToolFieldType.SELECT, true, options = listOf("None", "Public data", "Internal data", "Confidential data", "Customer personal data", "Regulated financial data")),
            ToolField("integration", "Integration depth", ToolFieldType.SELECT, true, options = listOf("Isolated / manual", "File exchange", "API integration", "Direct production access", "Embedded in customer journey")),
            ToolField("regulatoryScope", "Regulatory scope", ToolFieldType.MULTI_SELECT, true, options = listOf("RBI", "CERT-In", "DPDP Act", "PCI DSS", "GDPR", "SEBI", "IRDAI", "SOC 2 relied upon", "ISO certification required", "None")),
            ToolField("inherentRisk", "Inherent vendor risk", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("tier", "Assessed tier", ToolFieldType.SELECT, true, options = listOf("Tier 1 - Low", "Tier 2 - Moderate", "Tier 3 - High", "Tier 4 - Critical")),
            ToolField("dueDiligence", "Due diligence performed", ToolFieldType.MULTI_SELECT, true, options = listOf("Security questionnaire", "Independent assurance report (SOC 2 / ISO)", "Penetration test summary", "On-site assessment", "Financial health review", "Certification check", "Privacy assessment", "Subcontractor review")),
            ToolField("assuranceType", "Assurance type", ToolFieldType.SELECT, true, options = listOf("SOC 2 Type 1", "SOC 2 Type 2", "ISO 27001 certificate", "ISO 27017/27018", "None available", "Not yet obtained")),
            ToolField("assuranceExceptions", "Assurance exceptions noted", ToolFieldType.LONG_TEXT),
            ToolField("residualRisk", "Residual vendor risk", ToolFieldType.SELECT, true, options = riskRatingOptions),
            ToolField("requiredContractClauses", "Required contract clauses", ToolFieldType.LONG_TEXT),
            ToolField("monitoring", "Ongoing monitoring", ToolFieldType.TEXT),
            ToolField("owner", "Vendor owner", ToolFieldType.TEXT, true),
            ToolField("reviewDate", "Next reassessment date", ToolFieldType.DATE, true),
            ToolField("decision", "Onboarding decision", ToolFieldType.SELECT, true, options = listOf("Approved", "Approved with conditions", "Rejected", "Pending review", "Renewed", "Terminated"))
        ),
        sampleData = mapOf(
            "vendorId" to "V-2026-041",
            "vendorName" to "Northbridge Cloud Services Ltd.",
            "service" to "Managed hosting of the retail internet banking web tier",
            "businessUnit" to "Retail Banking",
            "criticality" to "Critical",
            "dataAccess" to "Regulated financial data",
            "integration" to "Direct production access",
            "regulatoryScope" to "RBI, CERT-In, PCI DSS, DPDP Act",
            "inherentRisk" to "Extreme",
            "tier" to "Tier 4 - Critical",
            "dueDiligence" to "Security questionnaire, Independent assurance report (SOC 2 / ISO), Penetration test summary, Financial health review, Certification check, Privacy assessment",
            "assuranceType" to "SOC 2 Type 2",
            "assuranceExceptions" to "Type 2 report covers 2025-07-01 to 2026-06-30 with no qualified opinion. Two exceptions in the system description: (1) privileged access reviews performed manually with a mean completion of 12 days against a 5-day policy, (2) log retention for administrative sessions is 90 days against a 12-month policy.",
            "residualRisk" to "High",
            "requiredContractClauses" to "Right to audit with 10 business days notice; incident notification within 24 hours of discovery; data localisation within India; unconditional subcontractor approval; termination for repeated SLA failure; 30-day transition assistance; unlimited confidentiality with survival clause.",
            "monitoring" to "Quarterly SOC 2 review, annual on-site assessment, continuous rating monitoring, monthly SLA reporting.",
            "owner" to "Head of IT",
            "reviewDate" to "2027-01-31",
            "decision" to "Approved with conditions"
        )
    )

    private fun vendorQuestionnaire() = ToolDefinition(
        id = "vendor_questionnaire",
        name = "Third-Party Security Questionnaire",
        category = CATEGORY_THIRD_PARTY,
        description = "A structured security questionnaire with the question, why it is asked, the evidence a vendor should supply and the scoring guidance, so vendor assessments are consistent and defensible.",
        competencyDomain = "THIRD_PARTY",
        referenceFramework = "NIST SP 800-161, CAIQ, CIS Controls",
        howToUse = listOf(
            "Select the question set that matches the vendor tier and the data the vendor will touch. Sending a Tier 1 vendor a 120-question pack guarantees poor-quality answers.",
            "Always ask for evidence, not assertions. 'Yes' is not an answer; 'Please see our SOC 2 Type 2 report, section X' is.",
            "Score the response, not the vendor's confidence. Where evidence is missing, record it as a gap and reflect it in the tier decision.",
            "Reconcile answers against the independent assurance report - contradictions are a red flag that requires escalation.",
            "Version the questionnaire and record the version sent with each assessment."
        ),
        fields = listOf(
            ToolField("questionId", "Question ID", ToolFieldType.TEXT, true),
            ToolField("section", "Section", ToolFieldType.SELECT, true, options = listOf("Governance", "Access Control", "Cryptography", "Vulnerability & Patch Management", "Logging & Monitoring", "Incident Response", "Business Continuity", "Personnel Security", "Data Protection & Privacy", "Cloud & Infrastructure", "Third Party Management", "Physical Security")),
            ToolField("question", "Question", ToolFieldType.LONG_TEXT, true),
            ToolField("whyAsked", "Why this is asked", ToolFieldType.LONG_TEXT, true),
            ToolField("evidenceRequested", "Evidence requested", ToolFieldType.TEXT, true),
            ToolField("vendorResponse", "Vendor response", ToolFieldType.LONG_TEXT),
            ToolField("score", "Score (0-3)", ToolFieldType.SELECT, true, options = listOf("0 - Not implemented", "1 - Partial", "2 - Largely implemented", "3 - Fully implemented and evidenced")),
            ToolField("risk", "Risk if not satisfied", ToolFieldType.LONG_TEXT),
            ToolField("followUp", "Follow-up required", ToolFieldType.TEXT),
            ToolField("tierApplicability", "Applies to tier", ToolFieldType.MULTI_SELECT, true, options = listOf("Tier 1", "Tier 2", "Tier 3", "Tier 4"))
        ),
        sampleData = mapOf(
            "questionId" to "Q-LOG-02",
            "section" to "Logging & Monitoring",
            "question" to "How are administrative actions on customer data systems logged, and for how long is the log retained in an immutable form?",
            "whyAsked" to "Administrative logs are the primary means of detecting and investigating fraud and unauthorised access. Where they are incomplete or mutable, the organisation cannot demonstrate accountability for privileged activity, and a regulatory examination of access rights cannot be satisfied.",
            "evidenceRequested" to "Sample log extract showing the fields recorded for administrative actions; log retention configuration; confirmation of immutability (write-once store or equivalent); statement of the retention period and the archive location.",
            "vendorResponse" to "Administrative actions are logged to the SIEM with a 12-month hot retention and 7-year archive. Logs are routed through the cloud provider log pipeline to a dedicated storage account with object lock enabled.",
            "score" to "2 - Largely implemented",
            "risk" to "A 90-day retention on administrative sessions in the vendor SOC 2 exception would prevent investigation of events older than one quarter and weaken attribution for slow-moving fraud. The annual supervisory examination would likely raise this as an observation.",
            "followUp" to "Request confirmation of the 12-month retention configuration and the object lock policy. Update the assurance exception record.",
            "tierApplicability" to "Tier 3, Tier 4"
        )
    )

    // =================================================================================
    // Continuity
    // =================================================================================

    private fun businessImpactAnalysis() = ToolDefinition(
        id = "bia_tool",
        name = "Business Impact Analysis Tool",
        category = CATEGORY_CONTINUITY,
        description = "Captures business impact analysis for a critical business function: the maximum tolerable period of disruption, financial and regulatory impact, dependencies and the recovery assumptions the RTO and RPO are built on.",
        competencyDomain = "BUSINESS_CONTINUITY",
        referenceFramework = "ISO 22301:2019, ISO/IEC 27001:2022 A.5.29-5.30",
        howToUse = listOf(
            "Start with the business function, not the system. Systems support functions; recovery is prioritised by business consequence.",
            "Agree the maximum tolerable period of disruption with the business owner. It is their decision, not the GRC team's.",
            "Capture the dependencies - people, suppliers, premises, data, and other functions. Single points of dependency are where continuity plans break.",
            "Quantify impact per hour of outage so that recovery investment can be prioritised against loss.",
            "Review the BIA at least annually and after any significant change to the function."
        ),
        fields = listOf(
            ToolField("biaId", "BIA reference", ToolFieldType.TEXT, true),
            ToolField("function", "Critical business function", ToolFieldType.TEXT, true),
            ToolField("processOwner", "Process owner", ToolFieldType.TEXT, true),
            ToolField("description", "What the function does", ToolFieldType.LONG_TEXT, true),
            ToolField("mtpd", "Maximum tolerable period of disruption (hours)", ToolFieldType.NUMBER, true),
            ToolField("maxFinancialLoss", "Maximum financial loss over MTPD", ToolFieldType.TEXT, true),
            ToolField("impactPerHour", "Impact per hour of disruption", ToolFieldType.TEXT),
            ToolField("nonFinancialImpact", "Non-financial impact (regulatory, customer, reputational)", ToolFieldType.LONG_TEXT, true),
            ToolField("regulatoryImpact", "Regulatory reporting obligation", ToolFieldType.TEXT),
            ToolField("dependencies", "Dependencies (people, suppliers, systems, premises)", ToolFieldType.LONG_TEXT, true),
            ToolField("singlePointsOfFailure", "Single points of failure identified", ToolFieldType.LONG_TEXT),
            ToolField("peakPeriods", "Peak periods with higher tolerance", ToolFieldType.TEXT),
            ToolField("recoveryStrategy", "Recovery strategy", ToolFieldType.LONG_TEXT, true),
            ToolField("lastReviewed", "Last reviewed", ToolFieldType.DATE, true),
            ToolField("nextReview", "Next review", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "biaId" to "BIA-RET-004",
            "function" to "Retail payment authorisation",
            "processOwner" to "Head of Retail Payments",
            "description" to "Authorisation of card and UPI payment transactions for retail and internet banking customers, including scheme switch routing, fraud screening and limit checks.",
            "mtpd" to "4",
            "maxFinancialLoss" to "INR 1.8 Crore over 4 hours including service credits and manual processing recovery",
            "impactPerHour" to "INR 45 Lakh per hour of full outage, rising to INR 90 Lakh during peak festive hours",
            "nonFinancialImpact" to "Customer trust erosion in a retail bank with 4.2 million active users; adverse media coverage during a major festival period; RBI supervisory interest in the availability of digital payment infrastructure.",
            "regulatoryImpact" to "RBI directions on digital payment security and uptime expectations; customer grievance volumes become regulator-visible if extended.",
            "dependencies" to "Card scheme switch connectivity, UPI sponsor bank, core banking ledger, fraud scoring service (third party), network provider, 2 specialist run-book staff for manual fallback.",
            "singlePointsOfFailure" to "Fraud scoring service is a single external dependency with a 4-hour RTO that matches our MTPD - any outage consumes the entire tolerance. Primary network path has a diverse backup but both terminate at the same edge router.",
            "peakPeriods" to "October to November and April to May see 3x transaction volume; tolerance effectively halves in these periods.",
            "recoveryStrategy" to "Active-active across two data centres with automatic transaction queueing at the channel layer and a documented manual authorisation fallback with a 2-hour capacity ceiling.",
            "lastReviewed" to "2026-03-12",
            "nextReview" to "2027-03-12"
        )
    )

    private fun rtoRpoPlanner() = ToolDefinition(
        id = "rtpo_planner",
        name = "RTO / RPO Planning Tool",
        category = CATEGORY_CONTINUITY,
        description = "Plans recovery time objective, recovery point objective and maximum tolerable downtime per system, and checks whether the technical capability actually meets the stated business tolerance.",
        competencyDomain = "BUSINESS_CONTINUITY",
        referenceFramework = "ISO 22301:2019 Clause 6.4.4, NIST SP 800-34",
        howToUse = listOf(
            "RTO and RPO are business commitments. Technology must be designed to meet them, not the other way round.",
            "Keep MTPD greater than RTO. If RTO equals or exceeds MTPD, the recovery target is meaningless.",
            "State the RPO in business terms - how much work is lost and what that costs - so the investment case is visible.",
            "Test the actual capability. Documented targets that have never been tested are aspirations.",
            "Record the gap where technical capability is weaker than the business target, and link it to the treatment plan."
        ),
        fields = listOf(
            ToolField("systemId", "System / service ID", ToolFieldType.TEXT, true),
            ToolField("systemName", "System or service", ToolFieldType.TEXT, true),
            ToolField("linkedBia", "Linked BIA function", ToolFieldType.TEXT, true),
            ToolField("businessOwner", "Business owner", ToolFieldType.TEXT, true),
            ToolField("mtpdHours", "MTPD (hours)", ToolFieldType.NUMBER, true),
            ToolField("rtoHours", "RTO (hours)", ToolFieldType.NUMBER, true),
            ToolField("rpoMinutes", "RPO (minutes)", ToolFieldType.NUMBER, true),
            ToolField("rtoBaseline", "Baseline", ToolFieldType.SELECT, true, options = listOf("Automated failover", "Automated restore", "Manual failover", "Manual rebuild", "Vendor dependent")),
            ToolField("backupType", "Backup type", ToolFieldType.SELECT, true, options = listOf("Full only", "Full + incremental", "Full + differential", "Continuous / journal", "Replicated storage", "Not backed up")),
            ToolField("backupFrequency", "Backup frequency", ToolFieldType.TEXT, true),
            ToolField("offSiteCopy", "Off-site / immutable copy", ToolFieldType.BOOLEAN, true),
            ToolField("lastTestDate", "Last recovery test date", ToolFieldType.DATE, true),
            ToolField("lastTestResult", "Last recovery test result", ToolFieldType.TEXT),
            ToolField("capabilityMeetsTarget", "Capability meets target?", ToolFieldType.BOOLEAN, true),
            ToolField("gap", "Gap and action", ToolFieldType.LONG_TEXT),
            ToolField("owner", "Owner", ToolFieldType.TEXT, true),
            ToolField("nextTestDate", "Next test due", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "systemId" to "SYS-PAY-001",
            "systemName" to "Payment authorisation and routing platform",
            "linkedBia" to "BIA-RET-004",
            "businessOwner" to "Head of Retail Payments",
            "mtpdHours" to "4",
            "rtoHours" to "1",
            "rpoMinutes" to "5",
            "rtoBaseline" to "Automated failover",
            "backupType" to "Replicated storage",
            "backupFrequency" to "Synchronous replication to the DR site with 5-minute log shipping",
            "offSiteCopy" to "true",
            "lastTestDate" to "2026-04-18",
            "lastTestResult" to "Failover completed in 38 minutes with zero transaction loss. Two manual steps required that are not yet in the automated run-book.",
            "capabilityMeetsTarget" to "true",
            "gap" to "Automated failover meets the 1-hour RTO with margin. Residual gap: the manual fallback process during a fraud-scoring outage is untested at peak volume and has a 2-hour capacity ceiling.",
            "owner" to "Head of IT",
            "nextTestDate" to "2026-10-16"
        )
    )

    private fun businessContinuityChecklist() = ToolDefinition(
        id = "bc_checklist",
        name = "Business Continuity Checklist",
        category = CATEGORY_CONTINUITY,
        description = "A programme-level checklist for establishing and maintaining business continuity: plan components, testing, crisis management, invocation criteria and integration with incident response.",
        competencyDomain = "BUSINESS_CONTINUITY",
        referenceFramework = "ISO 22301:2019",
        howToUse = listOf(
            "Work through the checklist once during plan development, then again as a standing annual assurance checklist.",
            "Require an exercise at least annually for each critical function, and a full crisis exercise for the organisation.",
            "A plan that has never been exercised is a document, not a capability. Record the exercise date and the outcome, including what failed.",
            "Cross-check with the incident response plan: a major cyber incident is simultaneously a continuity event.",
            "Record gaps as actions with owners, not as notes in the plan."
        ),
        fields = listOf(
            ToolField("itemId", "Item ID", ToolFieldType.TEXT, true),
            ToolField("area", "Area", ToolFieldType.SELECT, true, options = listOf("Business Continuity Management System", "Business Impact Analysis", "Continuity Plan", "Crisis Management", "Testing & Exercises", "Communications", "Training & Awareness", "Supplier Continuity", "Integration with Incident Response", "Review & Improvement")),
            ToolField("requirement", "Requirement", ToolFieldType.LONG_TEXT, true),
            ToolField("reference", "ISO 22301 clause", ToolFieldType.TEXT, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = listOf("Complete", "Partially complete", "Not started", "Not applicable")),
            ToolField("evidence", "Evidence held", ToolFieldType.TEXT),
            ToolField("lastTested", "Last tested", ToolFieldType.DATE),
            ToolField("gap", "Gap identified", ToolFieldType.LONG_TEXT),
            ToolField("action", "Action", ToolFieldType.LONG_TEXT),
            ToolField("owner", "Owner", ToolFieldType.TEXT, true),
            ToolField("dueDate", "Due date", ToolFieldType.DATE),
            ToolField("exerciseResult", "Exercise result", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "itemId" to "BC-TEST-02",
            "area" to "Testing & Exercises",
            "requirement" to "Each critical business function must be exercised at least annually using realistic scenarios, and the exercise must include the loss of the primary processing site.",
            "reference" to "ISO 22301:2019 Clause 8.5",
            "status" to "Partially complete",
            "evidence" to "Exercise schedule FY26, exercise report for the customer-onboarding function (2026-02-11), scenario pack for the August payments exercise (draft)",
            "lastTested" to "2026-02-11",
            "gap" to "Four of eleven critical functions were exercised in FY25. The payments function exercise is planned for 2026-08-21 but the scenario pack has not been approved by the crisis management team, so the exercise scope is not yet agreed.",
            "action" to "Approve the payments exercise scenario pack, extend the exercise to include the loss of the primary processing site, and schedule the remaining seven function exercises before 2027-03-31.",
            "owner" to "Business Continuity Manager",
            "dueDate" to "2026-08-21",
            "exerciseResult" to "Pending"
        )
    )

    // =================================================================================
    // Compliance and regulatory
    // =================================================================================

    private fun complianceObligationRegister() = ToolDefinition(
        id = "obligation_register",
        name = "Compliance Obligation Register",
        category = CATEGORY_COMPLIANCE,
        description = "Maps each legal and regulatory obligation to an accountable owner, the controls that satisfy it, the evidence that demonstrates compliance and the consequence of breach.",
        competencyDomain = "REGULATORY",
        referenceFramework = "ISO 19600 concepts, RBI / CERT-In / DPDP requirements",
        howToUse = listOf(
            "Decompose each obligation into testable statements. 'Comply with the RBI Framework' is not an obligation; 'maintain a cyber security framework approved by the board' is.",
            "Name one accountable owner per obligation. Shared ownership is the most common reason an obligation is missed at examination.",
            "Map obligations to controls and evidence so a regulatory request can be answered from the register.",
            "Record the consequence of breach for each obligation - this drives the priority order.",
            "Review the register monthly for regulatory change. Requirements change more often than most programmes admit."
        ),
        fields = listOf(
            ToolField("obligationId", "Obligation ID", ToolFieldType.TEXT, true),
            ToolField("source", "Source", ToolFieldType.SELECT, true, options = listOf("Law", "Regulation / Direction", "Industry guideline", "Contractual requirement", "Internal policy", "Standard / Framework")),
            ToolField("instrument", "Instrument (name and reference)", ToolFieldType.TEXT, true),
            ToolField("jurisdiction", "Jurisdiction", ToolFieldType.TEXT, true),
            ToolField("obligation", "Testable obligation", ToolFieldType.LONG_TEXT, true),
            ToolField("appliesTo", "Applies to", ToolFieldType.TEXT, true),
            ToolField("owner", "Accountable owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("controls", "Controls satisfying the obligation", ToolFieldType.TEXT, true),
            ToolField("evidence", "Evidence of compliance", ToolFieldType.TEXT, true),
            ToolField("frequency", "Compliance frequency", ToolFieldType.SELECT, true, options = listOf("Continuous", "Monthly", "Quarterly", "Semi-annual", "Annual", "Event-driven", "On change")),
            ToolField("lastReviewed", "Last reviewed", ToolFieldType.DATE, true),
            ToolField("nextReview", "Next review", ToolFieldType.DATE, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = listOf("Compliant", "Partially compliant", "Non-compliant", "Not assessed", "Not applicable")),
            ToolField("consequence", "Consequence of breach", ToolFieldType.LONG_TEXT, true),
            ToolField("sourceUrl", "Official source URL", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "obligationId" to "OBL-RBI-014",
            "source" to "Regulation / Direction",
            "instrument" to "RBI Master Direction - Management of Cyber Security Framework (reference to be verified against the current consolidated direction before use)",
            "jurisdiction" to "India",
            "obligation" to "The board of directors or a senior management committee must review the organisation's cyber security posture at least quarterly and record the review in the minutes.",
            "appliesTo" to "All scheduled commercial banks and their material subsidiaries",
            "owner" to "CISO",
            "controls" to "GV-04 (Board risk oversight), GRC-09 (Quarterly board reporting)",
            "evidence" to "Signed board minutes, quarterly cyber security posture report, attendance record",
            "frequency" to "Quarterly",
            "lastReviewed" to "2026-07-08",
            "nextReview" to "2026-10-08",
            "status" to "Compliant",
            "consequence" to "Adverse observation by the supervisor, supervisory reporting requirements, and reputational damage in a sector where trust in cyber governance is under active scrutiny.",
            "sourceUrl" to "https://www.rbi.org.in - verify the current consolidated Master Direction before relying on this entry."
        )
    )

    private fun regulatoryChangeTracker() = ToolDefinition(
        id = "regulatory_tracker",
        name = "Regulatory Change Tracker",
        category = CATEGORY_COMPLIANCE,
        description = "Tracks regulatory and standards changes from publication through impact assessment, decision, implementation and evidence of completion.",
        competencyDomain = "REGULATORY",
        referenceFramework = "Regulatory change management",
        howToUse = listOf(
            "Capture the change at the point of publication, with the source URL and the effective date. A tracker started after the gap appears is a tracker that will miss changes.",
            "Assess impact per business area, not once for the whole organisation. A change usually hits one function hard and the rest marginally.",
            "Record the decision explicitly - implement, adopt voluntarily, monitor, or not applicable - with a rationale.",
            "Track the gap between the effective date and the implementation date. That interval is where regulatory findings are generated.",
            "Link implemented changes to the controls and policies updated, so evidence of change is automatically available at examination.",
            "Re-verify every entry before an examination. Superseded and withdrawn instruments are the most common source of stale guidance."
        ),
        fields = listOf(
            ToolField("changeId", "Change ID", ToolFieldType.TEXT, true),
            ToolField("instrument", "Instrument", ToolFieldType.TEXT, true),
            ToolField("publisher", "Publisher", ToolFieldType.TEXT, true),
            ToolField("changeType", "Change type", ToolFieldType.SELECT, true, options = listOf("New instrument", "Amendment", "Replacement / superseding", "Consultation", "Enforcement action", "Advisory", "Withdrawal", "Industry practice")),
            ToolField("summary", "What changed", ToolFieldType.LONG_TEXT, true),
            ToolField("publishedDate", "Publication date", ToolFieldType.DATE, true),
            ToolField("effectiveDate", "Effective date", ToolFieldType.DATE, true),
            ToolField("transitionPeriod", "Transition period", ToolFieldType.TEXT),
            ToolField("impact", "Impact assessment", ToolFieldType.LONG_TEXT, true),
            ToolField("impactedAreas", "Impacted business areas", ToolFieldType.TEXT, true),
            ToolField("decision", "Decision", ToolFieldType.SELECT, true, options = listOf("Implement", "Implement voluntarily", "Monitor", "Not applicable", "Pending assessment")),
            ToolField("gapAnalysis", "Gap identified", ToolFieldType.LONG_TEXT),
            ToolField("actions", "Required actions", ToolFieldType.LONG_TEXT, true),
            ToolField("owner", "Owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("dueDate", "Implementation due", ToolFieldType.DATE, true),
            ToolField("status", "Status", ToolFieldType.SELECT, true, options = listOf("Not started", "Assessment in progress", "Gap analysis complete", "Implementation in progress", "Implemented", "Verified", "Closed")),
            ToolField("sourceUrl", "Official source URL", ToolFieldType.TEXT, true),
            ToolField("lastVerified", "Last verified", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "changeId" to "RC-2026-019",
            "instrument" to "RBI directions on digital payment security and data protection (current consolidated version to be confirmed)",
            "publisher" to "Reserve Bank of India",
            "changeType" to "Amendment",
            "summary" to "Strengthened expectations around digital payment authentication, transaction monitoring and the storage of card data, with a requirement for an increased level of oversight of third-party payment aggregators and a reduced dependence on static credentials for high-value transactions.",
            "publishedDate" to "2026-02-17",
            "effectiveDate" to "2026-04-01",
            "transitionPeriod" to "Existing arrangements to be aligned by 2026-10-01",
            "impact" to "The internet banking estate relies on static credentials for step-up authentication on certain high-value transfers. The channel must be re-engineered to support tokenised, transaction-bound authentication. The card data storage arrangement also requires re-examination given the new expectations on retention and tokenisation.",
            "impactedAreas" to "Retail Payments, Digital Banking, Information Security, Compliance",
            "decision" to "Implement",
            "gapAnalysis" to "Step-up authentication currently uses a one-time password issued by SMS, which does not satisfy transaction binding. Estimated effort 180 person-days plus vendor dependency.",
            "actions" to "1) Procure and pilot transaction-bound authentication; 2) update the payment security policy; 3) retrain the service desk on the new step-up flow; 4) update the channel risk assessment and SoA entries; 5) provide the compliance evidence pack for the interim period.",
            "owner" to "CISO",
            "dueDate" to "2026-10-01",
            "status" to "Implementation in progress",
            "sourceUrl" to "https://www.rbi.org.in/Scripts/BS_PressReleaseDisplay.aspx - locate the current consolidated direction.",
            "lastVerified" to "2026-07-01"
        )
    )

    // =================================================================================
    // Governance and leadership    // =================================================================================

    private fun kpiKriDashboard() = ToolDefinition(
        id = "kpi_dashboard",
        name = "GRC KPI / KRI Dashboard",
        category = CATEGORY_LEADERSHIP,
        description = "Defines and tracks the measures a GRC function reports: programme health indicators and risk indicators, each with a threshold, a target, a trend and the decision the measure drives.",
        competencyDomain = "LEADERSHIP",
        referenceFramework = "NIST CSF 2.0 GV.OC, IIA Performance Measurement",
        howToUse = listOf(
            "Every measure must drive a decision. If no decision changes when the number moves, delete the measure.",
            "Keep the dashboard to between 8 and 15 measures. Executives stop reading dashboards that carry every possible metric.",
            "Show a trend, not a point value. A single month tells the executive nothing about direction.",
            "Set thresholds that map to appetite so the executive can see where the organisation is outside its stated tolerance.",
            "Include the data quality caveat where the measure depends on manual reporting."
        ),
        fields = listOf(
            ToolField("measureId", "Measure ID", ToolFieldType.TEXT, true),
            ToolField("name", "Measure", ToolFieldType.TEXT, true),
            ToolField("type", "Type", ToolFieldType.SELECT, true, options = listOf("KPI - programme health", "KRI - risk indicator", "KRI - control performance", "KPI - outcome")),
            ToolField("definition", "Precise definition (numerator / denominator)", ToolFieldType.LONG_TEXT, true),
            ToolField("dataSource", "Data source", ToolFieldType.TEXT, true),
            ToolField("owner", "Measure owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("target", "Target", ToolFieldType.TEXT, true),
            ToolField("amberThreshold", "Amber threshold", ToolFieldType.TEXT, true),
            ToolField("redThreshold", "Red threshold", ToolFieldType.TEXT, true),
            ToolField("currentValue", "Current value", ToolFieldType.TEXT, true),
            ToolField("trend", "Trend", ToolFieldType.SELECT, true, options = listOf("Improving", "Stable", "Deteriorating", "New measure", "Insufficient data")),
            ToolField("period", "Reporting period", ToolFieldType.TEXT, true),
            ToolField("decisionDriven", "Decision this measure drives", ToolFieldType.LONG_TEXT, true),
            ToolField("caveat", "Data quality caveat", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "measureId" to "KRI-04",
            "name" to "High-severity risks outside stated appetite",
            "type" to "KRI - risk indicator",
            "definition" to "Count of risks rated High, Very High or Extreme residual whose risk category statement is recorded as outside appetite, divided by total risks rated High or above. Expressed as a percentage.",
            "dataSource" to "Enterprise Risk Register, quarterly extract validated against the board risk appetite statement",
            "owner" to "GRC Manager",
            "target" to "0%",
            "amberThreshold" to "1 - 5%",
            "redThreshold" to "Above 5%",
            "currentValue" to "3.4% (4 of 118 high-rated risks)",
            "trend" to "Stable",
            "period" to "Q2 FY26",
            "decisionDriven" to "Determines whether the CRO must present a specific remediation plan or a risk acceptance request to the Executive Risk Committee, and whether the number is reported as a board-level exception.",
            "caveat" to "Appetite status is asserted by risk owners and has not been independently validated for the four risks concerned; validation is scheduled for Q3 FY26."
        )
    )

    private fun managementReviewReport() = ToolDefinition(
        id = "management_review",
        name = "Management Review Report Builder",
        category = CATEGORY_LEADERSHIP,
        description = "Structures the input and record of an ISO 27001 management review: the mandated agenda items, performance data, changes affecting the ISMS, opportunities for improvement and the decisions taken.",
        competencyDomain = "GOVERNANCE",
        referenceFramework = "ISO/IEC 27001:2022 Clause 9.3, ISO 22301:2019 Clause 9.3",
        howToUse = listOf(
            "Cover every mandated input. A management review that omits an agenda item is a nonconformity, however good the rest of the meeting was.",
            "Bring analysis, not status. '9 of 12 controls effective' is a status; '3 of 12 controls ineffective, all in privileged access, with a common root cause of unreviewed role changes' is analysis.",
            "Record decisions, actions, owners and due dates. Minutes without decisions produce no improvement.",
            "Present the resource needs explicitly - management review is where budget for ISMS improvement is decided.",
            "Retain the input pack, the minutes and the action log as evidence; auditors ask for all three."
        ),
        fields = listOf(
            ToolField("reviewId", "Review ID", ToolFieldType.TEXT, true),
            ToolField("date", "Review date", ToolFieldType.DATE, true),
            ToolField("chair", "Chair", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("attendees", "Attendees", ToolFieldType.LONG_TEXT, true),
            ToolField("scope", "Scope of the ISMS reviewed", ToolFieldType.LONG_TEXT, true),
            ToolField("agenda", "Agenda items covered", ToolFieldType.MULTI_SELECT, true, options = listOf("Status of management review actions", "ISMS performance and effectiveness", "ISMS changes", "Nonconformities and corrective actions", "Audit results", "Risk assessment and treatment results", "Interested party requirements", "Adequacy of resources", "Communication with interested parties", "Opportunities for improvement", "ISMS scope and SoA changes", "Supplier performance", "Continuity of the ISMS", "Risk appetite input")),
            ToolField("performanceSummary", "Performance and effectiveness summary", ToolFieldType.LONG_TEXT, true),
            ToolField("changes", "Changes affecting the ISMS", ToolFieldType.LONG_TEXT, true),
            ToolField("opportunities", "Opportunities for improvement", ToolFieldType.LONG_TEXT, true),
            ToolField("resourceNeeds", "Resource needs", ToolFieldType.LONG_TEXT, true),
            ToolField("decisions", "Decisions taken", ToolFieldType.LONG_TEXT, true),
            ToolField("actions", "Actions with owners and dates", ToolFieldType.LONG_TEXT, true),
            ToolField("nextReview", "Next review date", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "reviewId" to "MR-2026-02",
            "date" to "2026-06-24",
            "chair" to "CISO",
            "attendees" to "CISO (chair), CIO, Head of IT, Head of Retail Banking, Head of Operations, GRC Manager, Internal Audit observer (for agenda items 5 and 6 only)",
            "scope" to "Retail banking and corporate IT estates, payment processing platform, and the associated business units. Excludes the market-risk trading platform, which is governed separately under the enterprise risk framework.",
            "agenda" to "Status of management review actions, ISMS performance and effectiveness, ISMS changes, Nonconformities and corrective actions, Audit results, Risk assessment and treatment results, Adequacy of resources, Communication with interested parties, Opportunities for improvement, Supplier performance, Continuity of the ISMS, Risk appetite input",
            "performanceSummary" to "11 of 12 Annex A control themes rated effective or partially effective; 1 theme (organizational aspects of information security) rated ineffective pending the 2026 organisational change programme. 4 high-severity findings open, 2 overdue. Privileged access effectiveness improved from 55% to 78% following the automation deployment. Two audit findings were closed in the quarter. Incident volume flat while near-miss reporting rose 22%, indicating improved reporting culture rather than deterioration.",
            "changes" to "Cloud migration of the customer onboarding service entered phase 2. The DPDP Rules implementation programme added a consent management workstream. A new CISO was appointed in February 2026. Board approved the enterprise GRC operating model in April.",
            "opportunities" to "Consolidate the three separate assessment registers into a single obligation register with control mapping. Introduce continuous control monitoring for privileged access. Extend the near-miss reporting incentive into business units.",
            "resourceNeeds" to "Approved headcount for two GRC analysts to support the obligation register consolidation and the certification surveillance preparation. Budget for a GRC platform pilot approved at INR 65 L for FY27 subject to a business case review.",
            "decisions" to "1) The obligation register consolidation proceeds with a Q4 FY26 completion date. 2) Continuous control monitoring pilot approved for privileged access in the digital channels. 3) The ineffective organisational controls theme is escalated to a board-level action with the CISO accountable.",
            "actions" to "A1 - Build consolidated obligation register - GRC Manager - 2026-12-15. A2 - Scope CCM pilot for privileged access - Head of IT - 2026-09-30. A3 - Board-level escalation of organisational control theme - CISO - 2026-07-31. A4 - Extend near-miss incentive scheme - Head of Operations - 2026-10-31.",
            "nextReview" to "2026-09-24"
        )
    )

    private fun awarenessTracker() = ToolDefinition(
        id = "awareness_tracker",
        name = "Security Awareness Tracker",
        category = CATEGORY_GOVERNANCE,
        description = "Tracks security and privacy awareness activity by population: completion, assessment scores, targeted campaigns and role-specific training for privileged, developer and regulatory-facing staff.",
        competencyDomain = "INFORMATION_SECURITY",
        referenceFramework = "ISO/IEC 27001:2022 A.6.3, NIST SP 800-50",
        howToUse = listOf(
            "Track completion and assessed understanding separately. A 100% completion rate with an average score of 45% is a problem, not a success.",
            "Segment the population. Privileged users, developers, customer-facing staff and finance staff need materially different content.",
            "Set a retraining trigger for anyone who fails an assessment, rather than only on an annual cycle.",
            "Measure behaviour change through simulated exercises and phishing simulation, not only course completion.",
            "Report to management review with the failed populations named, because aggregate figures hide the concentrated risk."
        ),
        fields = listOf(
            ToolField("campaignId", "Campaign / module ID", ToolFieldType.TEXT, true),
            ToolField("title", "Module title", ToolFieldType.TEXT, true),
            ToolField("population", "Target population", ToolFieldType.SELECT, true, options = listOf("All staff", "Privileged users", "Developers", "Customer-facing", "Finance & treasury", "Contractors", "Board members", "Managers")),
            ToolField("headcount", "Population headcount", ToolFieldType.NUMBER, true),
            ToolField("completed", "Completed", ToolFieldType.NUMBER, true),
            ToolField("completionPct", "Completion %", ToolFieldType.NUMBER, true),
            ToolField("averageScore", "Average assessment score %", ToolFieldType.NUMBER, true),
            ToolField("passThreshold", "Pass threshold %", ToolFieldType.NUMBER, true),
            ToolField("failedPopulation", "Failed and pending retraining", ToolFieldType.NUMBER, true),
            ToolField("deliveryDate", "Delivery date", ToolFieldType.DATE, true),
            ToolField("retrainingDue", "Retraining due", ToolFieldType.DATE, true),
            ToolField("behaviouralMeasure", "Behavioural measure used", ToolFieldType.TEXT),
            ToolField("owner", "Owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("notes", "Notes and remediation", ToolFieldType.LONG_TEXT)
        ),
        sampleData = mapOf(
            "campaignId" to "AW-2026-03",
            "title" to "Secure coding and secure SDLC for engineering",
            "population" to "Developers",
            "headcount" to "412",
            "completed" to "368",
            "completionPct" to "89",
            "averageScore" to "78",
            "passThreshold" to "75",
            "failedPopulation" to "27",
            "deliveryDate" to "2026-05-04",
            "retrainingDue" to "2027-05-04",
            "behaviouralMeasure" to "Percentage of production code changes with a passing peer security review, measured from the repository pull request metadata",
            "owner" to "Head of IT",
            "notes" to "27 engineers below threshold scheduled for a targeted re-sit on 2026-06-15. Engineers in the payments platform team show a lower average (71%) than the enterprise mean; a team-level session has been scheduled with the platform lead."
        )
    )

    private fun maturityAssessment() = ToolDefinition(
        id = "maturity_assessment",
        name = "GRC Maturity Assessment",
        category = CATEGORY_LEADERSHIP,
        description = "A five-level maturity assessment across the GRC capability domains, used to baseline a function, identify the constraint on progress and shape the transformation roadmap.",
        competencyDomain = "LEADERSHIP",
        referenceFramework = "CMMI-style maturity model, ISO 31000 Clause 4, COSO ERM",
        howToUse = listOf(
            "Use the five levels: Initial, Managed, Defined, Managed measurably, Optimising. Assess the organisation as it actually operates today.",
            "Assess each capability separately. A function can be Defined in risk management and Initial in privacy.",
            "Score the weakest evidence, not the best practice. One well-run process does not make a capability 'Defined'.",
            "Identify the constraint - the lowest-scoring capability that is blocking the others. Transforming a non-constraint wastes money.",
            "Re-baseline annually and after any material organisational or regulatory change, and report the delta to the executive."
        ),
        fields = listOf(
            ToolField("capability", "GRC capability", ToolFieldType.TEXT, true),
            ToolField("currentLevel", "Current level (1-5)", ToolFieldType.SELECT, true, options = listOf("1 - Initial", "2 - Managed", "3 - Defined", "4 - Managed measurably", "5 - Optimising")),
            ToolField("evidence", "Evidence supporting the rating", ToolFieldType.LONG_TEXT, true),
            ToolField("targetLevel", "Target level", ToolFieldType.SELECT, true, options = listOf("1 - Initial", "2 - Managed", "3 - Defined", "4 - Managed measurably", "5 - Optimising")),
            ToolField("gap", "Gap (levels)", ToolFieldType.NUMBER, true),
            ToolField("isConstraint", "Is this the current constraint?", ToolFieldType.BOOLEAN, true),
            ToolField("transformationAction", "Transformation action", ToolFieldType.LONG_TEXT, true),
            ToolField("investment", "Investment required", ToolFieldType.TEXT),
            ToolField("owner", "Owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("targetDate", "Target date", ToolFieldType.DATE, true),
            ToolField("quickWin", "Quick win available", ToolFieldType.TEXT)
        ),
        sampleData = mapOf(
            "capability" to "Third-Party Risk Management",
            "currentLevel" to "2 - Managed",
            "evidence" to "A third-party inventory exists and is maintained for tier 1 and 2 suppliers. Due diligence is performed at onboarding with a standard questionnaire. Tier 3 and 4 suppliers are handled inconsistently across business units, contract clauses are not standardised, and there is no continuous monitoring. One contract in the sample did not contain the right-to-audit clause despite the vendor being tier 4.",
            "targetLevel" to "4 - Managed measurably",
            "gap" to "2",
            "isConstraint" to "false",
            "transformationAction" to "Consolidate the supplier inventory into a single system of record, tier every supplier on a single criteria set, standardise contract clauses by tier, and implement continuous monitoring for tier 3 and 4 suppliers.",
            "investment" to "INR 1.2 Crore over 18 months, partly offset by reduced external audit effort",
            "owner" to "GRC Manager",
            "targetDate" to "2027-06-30",
            "quickWin" to "Issue the standard tier 4 contract clause set within 30 days - closes the highest-severity gap identified in the current inventory review"
        )
    )

    // =================================================================================
    // Privacy, assets and classification
    // =================================================================================
    private fun assetRegister() = ToolDefinition(
        id = "asset_register",
        name = "Asset Register",
        category = CATEGORY_GOVERNANCE,
        description = "Registers information assets and the systems, services, people and premises that support them, with ownership, classification and the controls that protect them.",
        competencyDomain = "INFORMATION_SECURITY",
        referenceFramework = "ISO/IEC 27001:2022 A.5.9, A.5.12, A.5.13",
        howToUse = listOf(
            "Register information assets first. A list of servers without knowing what data they hold cannot support classification or impact analysis.",
            "Name an owner for every asset. An unowned asset cannot be defended, and its risk will be assumed rather than managed.",
            "Link each asset to the business function it supports so loss impact can be expressed in business terms.",
            "Keep the register live by tying it to onboarding and decommissioning processes. A quarterly sweep that adds nothing will decay.",
            "Record where the asset actually lives, including copies in development, test, backup and vendor environments. Copy sprawl is where classification fails."
        ),
        fields = listOf(
            ToolField("assetId", "Asset ID", ToolFieldType.TEXT, true),
            ToolField("assetName", "Asset name", ToolFieldType.TEXT, true),
            ToolField("assetType", "Asset type", ToolFieldType.SELECT, true, options = listOf("Information asset / data set", "Application", "Infrastructure", "Service", "Software", "Personnel knowledge", "Facility", "Supplier", "Process")),
            ToolField("description", "Description", ToolFieldType.LONG_TEXT, true),
            ToolField("businessFunction", "Supports business function", ToolFieldType.TEXT, true),
            ToolField("owner", "Asset owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("custodian", "Custodian / operator", ToolFieldType.TEXT),
            ToolField("location", "Location / hosting", ToolFieldType.TEXT, true),
            ToolField("copies", "Number and location of copies", ToolFieldType.TEXT),
            ToolField("classification", "Classification", ToolFieldType.SELECT, true, options = listOf("Public", "Internal", "Confidential", "Restricted")),
            ToolField("personalData", "Contains personal data?", ToolFieldType.BOOLEAN, true),
            ToolField("criticality", "Criticality", ToolFieldType.SELECT, true, options = listOf("Low", "Medium", "High", "Critical")),
            ToolField("controls", "Applied controls", ToolFieldType.TEXT),
            ToolField("retentionRule", "Retention rule", ToolFieldType.TEXT),
            ToolField("lastReviewed", "Last reviewed", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "assetId" to "AST-DATA-021",
            "assetName" to "Customer KYC record set",
            "assetType" to "Information asset / data set",
            "description" to "Identity, address, PAN, occupation and risk rating for all retail and SME banking customers, with supporting documents and screening results.",
            "businessFunction" to "Customer Onboarding and KYC",
            "owner" to "Head of Retail Banking",
            "custodian" to "Head of Operations",
            "location" to "On-premise data centre (Chennai) with encrypted off-site backup and a copy in the vendor-hosted KYC platform",
            "copies" to "3 copies: production, DR site, KYC vendor platform. 1 derived copy in the analytics sandbox (masked).",
            "classification" to "Restricted",
            "personalData" to "true",
            "criticality" to "Critical",
            "controls" to "AC-01, AC-02, AC-03, AC-09, AC-11, CR-02, CR-04",
            "retentionRule" to "5 years after relationship closure, then deletion with certificate, per the enterprise records schedule and RBI KYC directions",
            "lastReviewed" to "2026-05-20"
        )
    )

    private fun classificationRegister() = ToolDefinition(
        id = "classification_register",
        name = "Information Classification Register",
        category = CATEGORY_PRIVACY,
        description = "Applies the enterprise classification scheme to information types, defining the handling, sharing, storage, transmission and disposal requirements for each level.",
        competencyDomain = "PRIVACY",
        referenceFramework = "ISO/IEC 27001:2022 A.5.12-5.13, A.8.10-8.13",
        howToUse = listOf(
            "Classify by the harm that unauthorised disclosure or modification would cause, not by how sensitive the business thinks the data feels.",
            "Define handling rules for each level: labelling, storage, transmission, sharing, printing, disposal and access.",
            "Cover derived data. Aggregated and anonymised data usually inherits the source classification until it has been formally reviewed.",
            "Make the labelling mechanism practical. If the label cannot be applied automatically in the main systems, it will not be applied.",
            "Map the classification to the regulatory context - PCI data, personal data and confidential financial data often sit in different levels with different rules."
        ),
        fields = listOf(
            ToolField("classificationId", "Classification ID", ToolFieldType.TEXT, true),
            ToolField("informationType", "Information type", ToolFieldType.TEXT, true),
            ToolField("level", "Classification level", ToolFieldType.SELECT, true, options = listOf("Public", "Internal", "Confidential", "Restricted")),
            ToolField("examples", "Examples", ToolFieldType.LONG_TEXT, true),
            ToolField("handling", "Handling requirements", ToolFieldType.LONG_TEXT, true),
            ToolField("sharing", "Sharing rules", ToolFieldType.LONG_TEXT, true),
            ToolField("storage", "Storage requirements", ToolFieldType.LONG_TEXT, true),
            ToolField("transmission", "Transmission requirements", ToolFieldType.LONG_TEXT, true),
            ToolField("disposal", "Disposal requirements", ToolFieldType.LONG_TEXT, true),
            ToolField("regulatoryTag", "Regulatory tagging (PCI / personal data / etc.)", ToolFieldType.TEXT),
            ToolField("labelMechanism", "How it is labelled", ToolFieldType.TEXT, true),
            ToolField("owner", "Classification owner", ToolFieldType.SELECT, true, options = ownerOptions),
            ToolField("reviewDate", "Review date", ToolFieldType.DATE, true)
        ),
        sampleData = mapOf(
            "classificationId" to "CLS-003",
            "informationType" to "Customer card data (PAN, expiry, CVV)",
            "level" to "Restricted",
            "examples" to "Primary account number, card verification value, track data from payment terminals, cardholder name combined with PAN, and any storage of sensitive authentication data.",
            "handling" to "May be processed only in a PCI DSS compliant environment. Masked display (first six and last four digits) permitted in all other systems. CVV may never be stored after authorisation, in any form.",
            "sharing" to "Sharing outside the card scheme or the PCI DSS compliant environment is prohibited. Tokenisation is mandatory for storage and analytics. Third-party transfer requires a merchant agreement and evidence of PCI DSS compliance.",
            "storage" to "Encrypted at rest with keys held in the enterprise HSM. Storage permitted only in systems within the cardholder data environment, which must be enumerated in the PCI scope and maintained quarterly.",
            "transmission" to "Strong cryptography in transit across all untrusted networks. Public key cryptography required for cardholder data in transit over open, public networks. Cleartext transmission of cardholder data is prohibited.",
            "disposal" to "Secure overwrite or physical destruction of media containing cardholder data, with a certificate of destruction retained for at least 12 months.",
            "regulatoryTag" to "PCI DSS 4.0.1 requirement 3",
            "labelMechanism" to "Tokenised at the point of capture by the payment gateway; the authoritative cardholder data environment is identified in the PCI scope document rather than by end-user labelling.",
            "owner" to "CISO",
            "reviewDate" to "2027-03-31"
        )
    )

    // =================================================================================
    // Additional registers
    // =================================================================================
}
