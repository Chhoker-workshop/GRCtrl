package com.vanguard.grc.data.content

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// =====================================================================================
// Shared enums
// =====================================================================================

@Serializable
enum class Difficulty { BEGINNER, INTERMEDIATE, ADVANCED, EXPERT }

@Serializable
enum class QuestionType {
    MULTIPLE_CHOICE,
    MULTIPLE_SELECT,
    TRUE_FALSE,
    SCENARIO,
    RISK_CALCULATION,
    MATCH_CONTROL,
    EVIDENCE_IDENTIFICATION,
    COMPLIANCE_GAP,
    PRIORITISE_REMEDIATION,
    MANAGEMENT_DECISION,
    SHORT_ANSWER,
    PRACTICAL_ASSIGNMENT
}

@Serializable
enum class FrameworkKind {
    LAW,
    REGULATION,
    STANDARD,
    FRAMEWORK,
    CODE_OF_PRACTICE,
    CERTIFICATION,
    CONTRACTUAL_REQUIREMENT,
    GUIDANCE
}

@Serializable
enum class FrameworkStatus { ACTIVE, SUPERSEDED, WITHDRAWN, DRAFT }

// =====================================================================================
// Curriculum
// =====================================================================================

@Serializable
data class ContentSection(
    val heading: String = "",
    val body: String = "",
    val bullets: List<String> = emptyList(),
    val callout: String? = null,
    /** Simple two-column table rendered as a compact comparison grid. */
    val table: List<List<String>> = emptyList()
)

@Serializable
data class LessonExample(
    val title: String,
    val context: String,
    val outcome: String,
    val sector: String = ""
)

@Serializable
data class PracticalExercise(
    val title: String,
    val brief: String,
    val steps: List<String> = emptyList(),
    val deliverable: String = "",
    val evaluationCriteria: List<String> = emptyList(),
    val estimatedMinutes: Int = 30
)

@Serializable
data class Lesson(
    val id: String,
    val moduleId: String,
    val topic: String,
    val subtopic: String = "",
    val title: String,
    val summary: String = "",
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val objectives: List<String> = emptyList(),
    val estimatedMinutes: Int = 45,
    val sections: List<ContentSection> = emptyList(),
    val examples: List<LessonExample> = emptyList(),
    val practicalExercise: PracticalExercise? = null,
    val assessmentQuestionIds: List<String> = emptyList(),
    val references: List<String> = emptyList(),
    val competencyDomain: String = "",
    val version: String = "1.0.0",
    val lastReviewed: String = "2026-01-15",
    val tags: List<String> = emptyList()
)

@Serializable
data class CurriculumModule(
    val id: String,
    val order: Int,
    val title: String,
    val subtitle: String = "",
    val description: String = "",
    val iconKey: String = "shield",
    val estimatedMinutes: Int = 180,
    val competencyDomain: String = "",
    val lessonIds: List<String> = emptyList(),
    val advancedLessonIds: List<String> = emptyList()
)

@Serializable
data class LearningPath(
    val id: String,
    val title: String,
    val description: String,
    val kind: String,
    val moduleIds: List<String> = emptyList(),
    val estimatedHours: Int = 0,
    val outcomes: List<String> = emptyList(),
    val order: Int = 0
)

// =====================================================================================
// Assessment
// =====================================================================================

@Serializable
data class QuestionOption(
    val id: String,
    val text: String,
    val correct: Boolean = false,
    /** Why this option is correct or incorrect. */
    val rationale: String = ""
)

@Serializable
data class MatchPair(
    val leftId: String,
    val left: String,
    val right: String,
    val note: String = ""
)

@Serializable
data class NumericAnswer(
    val expected: Double,
    val tolerance: Double = 0.01,
    val unit: String = "",
    val formula: String = "",
    val workedExample: String = ""
)

@Serializable
data class Question(
    val id: String,
    val domain: String,
    val topic: String,
    val type: QuestionType,
    val difficulty: Difficulty = Difficulty.INTERMEDIATE,
    val stem: String,
    val options: List<QuestionOption> = emptyList(),
    val correctOptionIds: List<String> = emptyList(),
    val explanation: String = "",
    val whyOthersWrong: Map<String, String> = emptyMap(),
    val frameworkRef: String = "",
    val learningObjective: String = "",
    val revisionRef: String = "",
    val points: Int = 1,
    val matchPairs: List<MatchPair> = emptyList(),
    val numericAnswer: NumericAnswer? = null,
    val modelAnswer: String = "",
    @SerialName("sector") val sector: String = "",
    /** Free tags such as CISA, CISM, CISSP, ISO 27001 or a specific standard clause. */
    val tags: List<String> = emptyList()
)

// =====================================================================================
// Framework registry and mapping
// =====================================================================================

@Serializable
data class FrameworkDefinition(
    val id: String,
    val title: String,
    val publisher: String,
    val kind: FrameworkKind,
    val edition: String = "",
    val publicationDate: String = "",
    val applicability: String = "",
    val officialUrl: String = "",
    val lastVerified: String = "2026-01-15",
    val status: FrameworkStatus = FrameworkStatus.ACTIVE,
    val notes: String = "",
    val tags: List<String> = emptyList()
)

@Serializable
data class MappingEntry(
    val id: String,
    val aRef: String,
    val bRef: String,
    val commonObjective: String,
    val differences: String = "",
    val evidenceReuse: String = "",
    val additionalRequirements: String = "",
    val implementationNote: String = ""
)

@Serializable
data class MappingPair(
    val id: String,
    val title: String,
    val frameworkA: String,
    val frameworkB: String,
    val summary: String,
    val entries: List<MappingEntry> = emptyList(),
    val scopeDifferences: String = "",
    val warning: String = ""
)

// =====================================================================================
// ISO 27001 Annex A control detail
// =====================================================================================

@Serializable
enum class ControlCategory { ORGANIZATIONAL, PEOPLE, PHYSICAL, TECHNOLOGICAL }

@Serializable
data class ControlDetail(
    val id: String,
    val controlId: String,
    val title: String,
    val category: ControlCategory,
    val objective: String,
    val whyItExists: String,
    val riskAddressed: String,
    val implementationGuidance: String,
    val responsibleDepartment: String,
    val evidenceExamples: List<String> = emptyList(),
    val auditTesting: String = "",
    val commonGaps: List<String> = emptyList(),
    val interviewQuestions: List<String> = emptyList(),
    val scenario: String = "",
    val relatedControls: List<String> = emptyList(),
    val keywords: List<String> = emptyList(),
    val mappings: Map<String, String> = emptyMap()
)

// =====================================================================================
// Simulator
// =====================================================================================

@Serializable
data class ScenarioOption(
    val id: String,
    val text: String,
    val score: Int,
    val consequence: String,
    val whyWeaker: String = ""
)

@Serializable
data class Scenario(
    val id: String,
    val title: String,
    val difficulty: Difficulty,
    val category: String,
    val context: String,
    val situation: String,
    val informationAvailable: List<String> = emptyList(),
    val options: List<ScenarioOption> = emptyList(),
    val bestPractice: String = "",
    val managementGuidance: String = "",
    val competencies: List<String> = emptyList(),
    val stakeholderViews: Map<String, String> = emptyMap(),
    val tags: List<String> = emptyList()
)

// =====================================================================================
// Toolkit
// =====================================================================================

@Serializable
enum class ToolFieldType { TEXT, LONG_TEXT, NUMBER, DATE, SELECT, MULTI_SELECT, BOOLEAN, SCORE }

@Serializable
data class ToolField(
    val key: String,
    val label: String,
    val type: ToolFieldType = ToolFieldType.TEXT,
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val helpText: String = "",
    val placeholder: String = "",
    val defaultValue: String = ""
)

@Serializable
data class ToolDefinition(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val howToUse: List<String> = emptyList(),
    val fields: List<ToolField> = emptyList(),
    val sampleData: Map<String, String> = emptyMap(),
    val exportFormats: List<String> = listOf("CSV", "PDF"),
    val competencyDomain: String = "",
    val referenceFramework: String = ""
)

// =====================================================================================
// Templates
// =====================================================================================

@Serializable
data class TemplateDocument(
    val id: String,
    val title: String,
    val category: String,
    val purpose: String,
    val instructions: List<String> = emptyList(),
    val requiredInformation: List<String> = emptyList(),
    val exampleEntries: List<String> = emptyList(),
    val ownership: String = "",
    val reviewFrequency: String = "",
    val approvalRequired: String = "",
    val disclaimer: String = "",
    val body: String = "",
    val competencyDomain: String = ""
)

// =====================================================================================
// Interview tracks
// =====================================================================================

@Serializable
data class InterviewQuestion(
    val id: String,
    val type: String,
    val prompt: String,
    val modelAnswer: String = "",
    val evaluationPoints: List<String> = emptyList(),
    val commonMistakes: List<String> = emptyList()
)

@Serializable
data class InterviewTrack(
    val id: String,
    val role: String,
    val level: String,
    val summary: String,
    val competencies: List<String> = emptyList(),
    val questions: List<InterviewQuestion> = emptyList(),
    val negotiationGuidance: List<String> = emptyList(),
    val evaluationDimensions: List<String> = emptyList(),
    val typicalEmployerFocus: String = ""
)

// =====================================================================================
// Capstone projects
// =====================================================================================

@Serializable
data class CapstoneProject(
    val id: String,
    val title: String,
    val difficulty: Difficulty,
    val background: String,
    val scope: String,
    val objectives: List<String> = emptyList(),
    val startingInformation: List<String> = emptyList(),
    val activities: List<String> = emptyList(),
    val deliverables: List<String> = emptyList(),
    val evaluationCriteria: List<String> = emptyList(),
    val modelSolution: String = "",
    val managementFeedback: String = "",
    val estimatedHours: Int = 6,
    val competencyDomain: String = ""
)

// =====================================================================================
// Accelerator
// =====================================================================================

@Serializable
data class AcceleratorDay(
    val day: Int,
    val title: String,
    val focus: String,
    val moduleId: String = "",
    val lessonIds: List<String> = emptyList(),
    val exercise: String = "",
    val assessmentIds: List<String> = emptyList(),
    val deliverables: List<String> = emptyList(),
    val revision: List<String> = emptyList(),
    val minutesCore: Int = 35,
    val minutesFramework: Int = 25,
    val minutesExercise: Int = 30,
    val minutesAssessment: Int = 20,
    val minutesRevision: Int = 10,
    val objectives: List<String> = emptyList()
) {
    val totalMinutes: Int
        get() = minutesCore + minutesFramework + minutesExercise + minutesAssessment + minutesRevision
}

// =====================================================================================
// Glossary
// =====================================================================================

@Serializable
data class GlossaryTerm(
    val id: String,
    val term: String,
    val definition: String,
    val aliases: List<String> = emptyList(),
    val relatedIds: List<String> = emptyList(),
    val framework: String = "",
    val note: String = ""
)
