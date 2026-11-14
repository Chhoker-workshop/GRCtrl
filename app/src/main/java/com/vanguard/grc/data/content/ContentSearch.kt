package com.vanguard.grc.data.content

/**
 * Unified search result across every content type in the corpus.
 */
data class SearchResult(
    val id: String,
    val title: String,
    val subtitle: String,
    val kind: SearchKind,
    val body: String,
    val route: String,
    val score: Int,
    val tags: List<String> = emptyList()
)

enum class SearchKind(val label: String) {
    LESSON("Lesson"),
    CONTROL("Control"),
    QUESTION("Question"),
    FRAMEWORK("Framework"),
    SCENARIO("Scenario"),
    TOOL("Tool"),
    TEMPLATE("Template"),
    GLOSSARY("Term"),
    INTERVIEW("Interview track"),
    PROJECT("Project"),
    MODULE("Module")
}

/**
 * Token-based local search. The index is rebuilt only when the corpus changes,
 * and queries run entirely on-device so global search works with no network.
 */
class ContentSearch(index: ContentIndex) {

    private data class Entry(
        val result: SearchResult,
        val titleLower: String,
        val bodyLower: String,
        val tagLower: String
    )

    private val entries: List<Entry> = buildEntries(index)

    fun search(query: String, kinds: Set<SearchKind> = emptySet(), limit: Int = 60): List<SearchResult> {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return emptyList()
        val tokens = q.split(Regex("\\s+")).filter { it.length >= 2 }
        if (tokens.isEmpty()) return emptyList()

        return entries.asSequence()
            .filter { kinds.isEmpty() || it.result.kind in kinds }
            .map { entry -> entry to score(entry, tokens) }
            .filter { it.second > 0 }
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first.result }
            .toList()
    }

    /** Suggested topics shown on the search screen before the user types. */
    fun suggestions(): List<String> = listOf(
        "risk appetite",
        "statement of applicability",
        "three lines model",
        "incident response",
        "vendor due diligence",
        "DPIA",
        "SOC 2",
        "PCI DSS",
        "RBI",
        "NIST CSF",
        "risk register",
        "internal audit",
        "zero trust",
        "board reporting",
        "AI governance"
    )

    private fun score(entry: Entry, tokens: List<String>): Int {
        var total = 0
        for (token in tokens) {
            var tokenScore = 0
            if (entry.titleLower.contains(token)) tokenScore += 60
            if (entry.titleLower.startsWith(token)) tokenScore += 30
            if (entry.tagLower.contains(token)) tokenScore += 25
            val bodyHits = countOccurrences(entry.bodyLower, token)
            tokenScore += minOf(bodyHits * 6, 30)
            if (tokenScore == 0) return 0
            total += tokenScore
        }
        return total
    }

    private fun countOccurrences(haystack: String, needle: String): Int {
        var count = 0
        var index = haystack.indexOf(needle)
        while (index >= 0) {
            count++
            index = haystack.indexOf(needle, index + needle.length)
        }
        return count
    }

    private fun buildEntries(index: ContentIndex): List<Entry> {
        val out = mutableListOf<Entry>()

        index.modules.forEach { m ->
            out += entry(
                id = m.id,
                title = m.title,
                subtitle = m.subtitle,
                kind = SearchKind.MODULE,
                body = m.description,
                route = "module/${m.id}",
                tags = m.competencyDomain
            )
        }

        index.lessons.forEach { l ->
            val body = buildString {
                append(l.summary).append(' ')
                l.sections.forEach { s ->
                    append(s.heading).append(' ').append(s.body).append(' ')
                    s.bullets.forEach { append(it).append(' ') }
                }
                l.examples.forEach { append(it.context).append(' ').append(it.outcome).append(' ') }
                l.practicalExercise?.let { append(it.brief).append(' ') }
                append(l.references.joinToString(" "))
            }
            out += entry(
                id = l.id,
                title = l.title,
                subtitle = "${l.topic}${if (l.subtopic.isNotBlank()) " - ${l.subtopic}" else ""}",
                kind = SearchKind.LESSON,
                body = body,
                route = "lesson/${l.id}",
                tags = (l.tags + l.competencyDomain + l.references).joinToString(" ")
            )
        }

        index.controls.forEach { c ->
            val body = buildString {
                append(c.objective).append(' ')
                append(c.whyItExists).append(' ')
                append(c.riskAddressed).append(' ')
                append(c.implementationGuidance).append(' ')
                append(c.auditTesting).append(' ')
                append(c.scenario).append(' ')
                c.evidenceExamples.forEach { append(it).append(' ') }
                c.commonGaps.forEach { append(it).append(' ') }
                c.keywords.forEach { append(it).append(' ') }
            }
            out += entry(
                id = c.id,
                title = "${c.controlId} ${c.title}",
                subtitle = c.category.name.lowercase().replaceFirstChar { it.uppercase() } + " control",
                kind = SearchKind.CONTROL,
                body = body,
                route = "control/${c.id}",
                tags = (c.keywords + c.relatedControls).joinToString(" ")
            )
        }

        index.questions.forEach { q ->
            out += entry(
                id = q.id,
                title = q.stem,
                subtitle = "${q.domain} - ${q.topic}",
                kind = SearchKind.QUESTION,
                body = q.explanation + " " + q.options.joinToString(" ") { it.text },
                route = "practice",
                tags = "${q.domain} ${q.frameworkRef} ${q.learningObjective}"
            )
        }

        index.frameworks.forEach { f ->
            out += entry(
                id = f.id,
                title = f.title,
                subtitle = "${f.publisher} - ${f.kind.name.lowercase().replace('_', ' ')}",
                kind = SearchKind.FRAMEWORK,
                body = f.applicability + " " + f.notes + " " + f.edition + " " + f.publicationDate,
                route = "framework/${f.id}",
                tags = f.tags.joinToString(" ")
            )
        }

        index.scenarios.forEach { s ->
            out += entry(
                id = s.id,
                title = s.title,
                subtitle = "${s.category} - ${s.difficulty.name.lowercase()}",
                kind = SearchKind.SCENARIO,
                body = s.context + " " + s.situation,
                route = "simulator/${s.id}",
                tags = s.tags.joinToString(" ")
            )
        }

        index.tools.forEach { t ->
            out += entry(
                id = t.id,
                title = t.name,
                subtitle = t.category,
                kind = SearchKind.TOOL,
                body = t.description + " " + t.howToUse.joinToString(" "),
                route = "tool/${t.id}",
                tags = t.competencyDomain + " " + t.referenceFramework
            )
        }

        index.templates.forEach { t ->
            out += entry(
                id = t.id,
                title = t.title,
                subtitle = t.category,
                kind = SearchKind.TEMPLATE,
                body = t.purpose + " " + t.body,
                route = "template/${t.id}",
                tags = t.competencyDomain
            )
        }

        index.glossary.forEach { g ->
            out += entry(
                id = g.id,
                title = g.term,
                subtitle = g.framework,
                kind = SearchKind.GLOSSARY,
                body = g.definition + " " + g.aliases.joinToString(" ") + " " + g.note,
                route = "glossary/${g.id}",
                tags = g.framework
            )
        }

        index.interviews.forEach { t ->
            out += entry(
                id = t.id,
                title = t.role,
                subtitle = t.level,
                kind = SearchKind.INTERVIEW,
                body = t.summary + " " + t.competencies.joinToString(" "),
                route = "interview/${t.id}",
                tags = t.role + " " + t.level
            )
        }

        index.projects.forEach { p ->
            out += entry(
                id = p.id,
                title = p.title,
                subtitle = "Capstone project",
                kind = SearchKind.PROJECT,
                body = p.background + " " + p.scope + " " + p.objectives.joinToString(" "),
                route = "project/${p.id}",
                tags = p.competencyDomain
            )
        }

        return out
    }

    private fun entry(
        id: String,
        title: String,
        subtitle: String,
        kind: SearchKind,
        body: String,
        route: String,
        tags: String
    ) = Entry(
        result = SearchResult(
            id = id,
            title = title,
            subtitle = subtitle,
            kind = kind,
            body = body,
            route = route,
            score = 0,
            tags = tags.split(" ").filter { it.isNotBlank() }
        ),
        titleLower = title.lowercase(),
        bodyLower = body.lowercase(),
        tagLower = tags.lowercase()
    )
}
