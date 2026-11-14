package com.vanguard.grc.data.content

/**
 * Static validation of the curriculum corpus.
 *
 * The validator is intentionally strict about the things that silently break
 * the learner experience - dangling identifiers, questions with no correct
 * answer, empty lessons - and informational about the things that are stylistic
 * (a missing icon key, an unreviewed lesson).
 */
class ContentValidator {

    data class Issue(
        val severity: Severity,
        val contentId: String,
        val message: String
    )

    enum class Severity { ERROR, WARNING, INFO }

    data class Report(
        val issues: List<Issue>,
        val stats: Stats
    ) {
        val errorCount: Int get() = issues.count { it.severity == Severity.ERROR }
        val warningCount: Int get() = issues.count { it.severity == Severity.WARNING }
        val isClean: Boolean get() = errorCount == 0
    }

    data class Stats(
        val modules: Int,
        val lessons: Int,
        val questions: Int,
        val controls: Int,
        val scenarios: Int,
        val tools: Int,
        val templates: Int,
        val mappings: Int,
        val frameworks: Int,
        val interviews: Int,
        val projects: Int,
        val acceleratorDays: Int,
        val glossaryTerms: Int,
        val totalMinutes: Int
    )

    fun validate(index: ContentIndex): Report {
        val issues = mutableListOf<Issue>()

        // --- Duplicate identifiers ------------------------------------------------
        issues += duplicates(index.modules.map { it.id to it.title }, "module")
        issues += duplicates(index.lessons.map { it.id to it.title }, "lesson")
        issues += duplicates(index.questions.map { it.id to it.stem.take(40) }, "question")
        issues += duplicates(index.controls.map { it.id to it.controlId }, "control")
        issues += duplicates(index.scenarios.map { it.id to it.title }, "scenario")
        issues += duplicates(index.tools.map { it.id to it.name }, "tool")
        issues += duplicates(index.templates.map { it.id to it.title }, "template")
        issues += duplicates(index.projects.map { it.id to it.title }, "project")
        issues += duplicates(index.interviews.map { it.id to it.role }, "interview track")
        issues += duplicates(index.frameworks.map { it.id to it.title }, "framework")
        issues += duplicates(index.glossary.map { it.id to it.term }, "glossary term")
        issues += duplicates(index.mappings.map { it.id to it.title }, "mapping pair")

        // --- Lessons ---------------------------------------------------------------
        index.lessons.forEach { lesson ->
            if (lesson.id.isBlank()) {
                issues += Issue(Severity.ERROR, "<blank>", "Lesson has a blank identifier")
            }
            if (lesson.title.isBlank()) {
                issues += Issue(Severity.ERROR, lesson.id, "Lesson title is empty")
            }
            if (lesson.sections.isEmpty() && lesson.practicalExercise == null) {
                issues += Issue(Severity.ERROR, lesson.id, "Lesson has no content sections and no exercise")
            }
            if (lesson.objectives.isEmpty()) {
                issues += Issue(Severity.WARNING, lesson.id, "Lesson has no learning objectives")
            }
            if (lesson.estimatedMinutes <= 0) {
                issues += Issue(Severity.WARNING, lesson.id, "Lesson estimated duration must be positive")
            }
            if (lesson.version.isBlank()) {
                issues += Issue(Severity.WARNING, lesson.id, "Lesson has no content version")
            }
            lesson.sections.forEachIndexed { i, section ->
                if (section.body.isBlank() && section.bullets.isEmpty() && section.table.isEmpty()) {
                    issues += Issue(Severity.WARNING, "${lesson.id}#section$i", "Empty content section")
                }
            }
            lesson.assessmentQuestionIds.forEach { qid ->
                if (index.questionsById[qid] == null) {
                    issues += Issue(Severity.ERROR, lesson.id, "References unknown assessment question '$qid'")
                }
            }
        }

        // --- Modules ---------------------------------------------------------------
        index.modules.forEach { module ->
            if (module.lessonIds.isEmpty() && module.advancedLessonIds.isEmpty()) {
                issues += Issue(Severity.ERROR, module.id, "Module has no lessons")
            }
            (module.lessonIds + module.advancedLessonIds).forEach { lid ->
                if (index.lessonsById[lid] == null) {
                    issues += Issue(Severity.ERROR, module.id, "References unknown lesson '$lid'")
                }
            }
        }
        index.lessons.forEach { lesson ->
            if (index.modulesById[lesson.moduleId] == null) {
                issues += Issue(Severity.ERROR, lesson.id, "Belongs to unknown module '${lesson.moduleId}'")
            }
        }

        // --- Questions -------------------------------------------------------------
        index.questions.forEach { q ->
            if (q.stem.isBlank()) issues += Issue(Severity.ERROR, q.id, "Question stem is empty")
            if (q.explanation.isBlank()) issues += Issue(Severity.WARNING, q.id, "Question has no explanation")
            when (q.type) {
                QuestionType.MULTIPLE_CHOICE,
                QuestionType.MULTIPLE_SELECT,
                QuestionType.TRUE_FALSE,
                QuestionType.SCENARIO,
                QuestionType.EVIDENCE_IDENTIFICATION,
                QuestionType.COMPLIANCE_GAP,
                QuestionType.PRIORITISE_REMEDIATION,
                QuestionType.MANAGEMENT_DECISION -> {
                    if (q.options.isEmpty()) {
                        issues += Issue(Severity.ERROR, q.id, "Question type ${q.type} requires options")
                    }
                    if (q.correctOptionIds.isEmpty()) {
                        issues += Issue(Severity.ERROR, q.id, "Question has no correct answer")
                    }
                    val optionIds = q.options.map { it.id }.toSet()
                    q.correctOptionIds.forEach { cid ->
                        if (cid !in optionIds) {
                            issues += Issue(Severity.ERROR, q.id, "Correct answer '$cid' has no matching option")
                        }
                    }
                    val flagged = q.options.filter { it.correct }.map { it.id }.toSet()
                    if (flagged.isNotEmpty() && flagged != q.correctOptionIds.toSet()) {
                        issues += Issue(Severity.ERROR, q.id, "Option 'correct' flags disagree with correctOptionIds")
                    }
                    if (q.type == QuestionType.MULTIPLE_SELECT && q.correctOptionIds.size < 2) {
                        issues += Issue(Severity.WARNING, q.id, "Multiple-select question has fewer than two correct options")
                    }
                    if (q.type == QuestionType.TRUE_FALSE && q.options.size != 2) {
                        issues += Issue(Severity.ERROR, q.id, "True/false question must have exactly two options")
                    }
                    if (q.options.any { it.rationale.isBlank() }) {
                        issues += Issue(Severity.WARNING, q.id, "One or more options have no rationale")
                    }
                }
                QuestionType.RISK_CALCULATION -> {
                    if (q.numericAnswer == null) {
                        issues += Issue(Severity.ERROR, q.id, "Risk calculation question has no numeric answer")
                    }
                    if (q.options.isEmpty()) {
                        issues += Issue(Severity.ERROR, q.id, "Risk calculation question requires candidate answers")
                    }
                }
                QuestionType.MATCH_CONTROL -> {
                    if (q.matchPairs.isEmpty()) {
                        issues += Issue(Severity.ERROR, q.id, "Matching question has no pairs")
                    }
                    if (q.correctOptionIds.isEmpty()) {
                        issues += Issue(Severity.ERROR, q.id, "Matching question has no correct mapping")
                    }
                }
                QuestionType.SHORT_ANSWER -> {
                    if (q.modelAnswer.isBlank()) {
                        issues += Issue(Severity.ERROR, q.id, "Short answer question has no model answer")
                    }
                }
                QuestionType.PRACTICAL_ASSIGNMENT -> {
                    if (q.modelAnswer.isBlank() && q.explanation.isBlank()) {
                        issues += Issue(Severity.ERROR, q.id, "Practical assignment has neither model answer nor explanation")
                    }
                }
            }
        }

        // --- Controls --------------------------------------------------------------
        index.controls.forEach { control ->
            if (control.controlId.isBlank()) issues += Issue(Severity.ERROR, control.id, "Control has no controlId")
            if (control.objective.isBlank()) issues += Issue(Severity.ERROR, control.id, "Control has no objective")
            if (control.implementationGuidance.isBlank()) issues += Issue(Severity.ERROR, control.id, "Control has no implementation guidance")
            if (control.evidenceExamples.isEmpty()) issues += Issue(Severity.WARNING, control.id, "Control lists no evidence examples")
            if (control.auditTesting.isBlank()) issues += Issue(Severity.WARNING, control.id, "Control has no audit testing guidance")
            if (control.interviewQuestions.isEmpty()) issues += Issue(Severity.WARNING, control.id, "Control lists no interview questions")
        }

        // --- Mappings --------------------------------------------------------------
        index.mappings.forEach { pair ->
            if (pair.entries.isEmpty()) issues += Issue(Severity.WARNING, pair.id, "Mapping pair has no entries")
            pair.entries.forEach { entry ->
                if (entry.commonObjective.isBlank()) {
                    issues += Issue(Severity.ERROR, pair.id, "Mapping entry '${entry.aRef}' has no common objective")
                }
            }
        }

        // --- Scenarios -------------------------------------------------------------
        index.scenarios.forEach { s ->
            if (s.options.size < 3) issues += Issue(Severity.WARNING, s.id, "Scenario offers fewer than three options")
            if (s.options.none { it.score >= 80 }) {
                issues += Issue(Severity.ERROR, s.id, "Scenario has no high-scoring (best-practice) option")
            }
            if (s.bestPractice.isBlank()) issues += Issue(Severity.ERROR, s.id, "Scenario has no best-practice explanation")
            if (s.options.any { it.consequence.isBlank() }) {
                issues += Issue(Severity.WARNING, s.id, "Scenario options must describe consequences")
            }
        }

        // --- Tools and templates ---------------------------------------------------
        index.tools.forEach { tool ->
            if (tool.fields.isEmpty()) issues += Issue(Severity.ERROR, tool.id, "Tool has no fields")
            tool.fields.forEach { field ->
                if (field.key.isBlank()) issues += Issue(Severity.ERROR, tool.id, "Tool field has a blank key")
                if (field.type in listOf(ToolFieldType.SELECT, ToolFieldType.MULTI_SELECT) && field.options.isEmpty()) {
                    issues += Issue(Severity.ERROR, tool.id, "Field '${field.key}' is a selection with no options")
                }
            }
            val fieldKeys = tool.fields.map { it.key }.toSet()
            tool.sampleData.keys.forEach { key ->
                if (key !in fieldKeys) {
                    issues += Issue(Severity.WARNING, tool.id, "Sample data references unknown field '$key'")
                }
            }
            if (tool.howToUse.isEmpty()) issues += Issue(Severity.WARNING, tool.id, "Tool has no usage instructions")
        }

        index.templates.forEach { tpl ->
            if (tpl.body.isBlank()) issues += Issue(Severity.ERROR, tpl.id, "Template has no body content")
            if (tpl.purpose.isBlank()) issues += Issue(Severity.ERROR, tpl.id, "Template has no stated purpose")
            if (tpl.disclaimer.isBlank()) issues += Issue(Severity.WARNING, tpl.id, "Template is missing the educational-use disclaimer")
        }

        // --- Accelerator -----------------------------------------------------------
        val days = index.accelerator.map { it.day }.toSet()
        (1..60).forEach { day ->
            if (day !in days) issues += Issue(Severity.ERROR, "accelerator.day.$day", "Accelerator day $day is missing")
        }
        index.accelerator.forEach { day ->
            day.lessonIds.forEach { lid ->
                if (index.lessonsById[lid] == null) {
                    issues += Issue(Severity.ERROR, "accelerator.day.${day.day}", "References unknown lesson '$lid'")
                }
            }
            if (day.objectives.isEmpty()) issues += Issue(Severity.WARNING, "accelerator.day.${day.day}", "Day has no objectives")
            if (day.exercise.isBlank()) issues += Issue(Severity.WARNING, "accelerator.day.${day.day}", "Day has no practical exercise")
        }

        // --- Paths, projects, interviews -------------------------------------------
        index.paths.forEach { path ->
            path.moduleIds.forEach { mid ->
                if (index.modulesById[mid] == null) {
                    issues += Issue(Severity.ERROR, path.id, "References unknown module '$mid'")
                }
            }
            if (path.moduleIds.isEmpty()) issues += Issue(Severity.ERROR, path.id, "Learning path has no modules")
        }
        index.projects.forEach { p ->
            if (p.deliverables.isEmpty()) issues += Issue(Severity.ERROR, p.id, "Project has no deliverables")
            if (p.evaluationCriteria.isEmpty()) issues += Issue(Severity.ERROR, p.id, "Project has no evaluation criteria")
            if (p.modelSolution.isBlank()) issues += Issue(Severity.ERROR, p.id, "Project has no model solution")
        }
        index.interviews.forEach { track ->
            if (track.questions.isEmpty()) issues += Issue(Severity.ERROR, track.id, "Interview track has no questions")
            track.questions.forEach { q ->
                if (q.modelAnswer.isBlank()) issues += Issue(Severity.WARNING, "${track.id}/${q.id}", "Question has no model answer")
            }
        }

        // --- Framework registry ----------------------------------------------------
        index.frameworks.forEach { fw ->
            if (fw.officialUrl.isBlank()) issues += Issue(Severity.WARNING, fw.id, "Framework has no official source URL")
            if (fw.lastVerified.isBlank()) issues += Issue(Severity.WARNING, fw.id, "Framework has no last-verified date")
        }

        val stats = Stats(
            modules = index.modules.size,
            lessons = index.lessons.size,
            questions = index.questions.size,
            controls = index.controls.size,
            scenarios = index.scenarios.size,
            tools = index.tools.size,
            templates = index.templates.size,
            mappings = index.mappings.size,
            frameworks = index.frameworks.size,
            interviews = index.interviews.size,
            projects = index.projects.size,
            acceleratorDays = index.accelerator.size,
            glossaryTerms = index.glossary.size,
            totalMinutes = index.lessons.sumOf { it.estimatedMinutes } +
                index.accelerator.sumOf { it.totalMinutes }
        )

        return Report(issues.sortedWith(compareBy({ it.severity.ordinal }, { it.contentId })), stats)
    }

    private fun duplicates(items: List<Pair<String, String>>, label: String): List<Issue> {
        val grouped = items.groupBy { it.first }
        return grouped.filter { it.value.size > 1 }.map { (id, group) ->
            Issue(Severity.ERROR, id, "Duplicate $label identifier used ${group.size} times")
        }
    }
}
