package com.vanguard.grc.data.content

import android.content.res.AssetManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * A single parsed content document.
 */
data class ContentDocument(
    val path: String,
    val kind: String,
    val schemaVersion: String,
    val itemCount: Int,
    val raw: JsonArray
)

/**
 * In-memory index of the entire curriculum, built once from the JSON assets.
 */
data class ContentIndex(
    val modules: List<CurriculumModule> = emptyList(),
    val lessons: List<Lesson> = emptyList(),
    val paths: List<LearningPath> = emptyList(),
    val questions: List<Question> = emptyList(),
    val frameworks: List<FrameworkDefinition> = emptyList(),
    val mappings: List<MappingPair> = emptyList(),
    val controls: List<ControlDetail> = emptyList(),
    val scenarios: List<Scenario> = emptyList(),
    val tools: List<ToolDefinition> = emptyList(),
    val templates: List<TemplateDocument> = emptyList(),
    val interviews: List<InterviewTrack> = emptyList(),
    val projects: List<CapstoneProject> = emptyList(),
    val accelerator: List<AcceleratorDay> = emptyList(),
    val glossary: List<GlossaryTerm> = emptyList(),
    val documents: List<ContentDocument> = emptyList(),
    val loadErrors: List<String> = emptyList()
) {
    val lessonsById: Map<String, Lesson> = lessons.associateBy { it.id }
    val modulesById: Map<String, CurriculumModule> = modules.associateBy { it.id }
    val questionsById: Map<String, Question> = questions.associateBy { it.id }
    val controlsById: Map<String, ControlDetail> = controls.associateBy { it.id }
    val controlsByControlId: Map<String, ControlDetail> = controls.associateBy { it.controlId }
    val frameworksById: Map<String, FrameworkDefinition> = frameworks.associateBy { it.id }
    val scenariosById: Map<String, Scenario> = scenarios.associateBy { it.id }
    val toolsById: Map<String, ToolDefinition> = tools.associateBy { it.id }
    val templatesById: Map<String, TemplateDocument> = templates.associateBy { it.id }
    val interviewsById: Map<String, InterviewTrack> = interviews.associateBy { it.id }
    val projectsById: Map<String, CapstoneProject> = projects.associateBy { it.id }
    val glossaryById: Map<String, GlossaryTerm> = glossary.associateBy { it.id }
    val acceleratorByDay: Map<Int, AcceleratorDay> = accelerator.associateBy { it.day }
}

/**
 * Loads, validates and indexes the versioned JSON curriculum shipped in
 * `assets/curriculum`. The whole corpus is parsed once on first use and held
 * in memory - the corpus is measured in the low tens of megabytes at most, and
 * keeping it resident makes global search and offline operation instant.
 */
class ContentRepository(private val assets: AssetManager) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = false
        coerceInputValues = true
        explicitNulls = false
    }

    private val _index = MutableStateFlow(ContentIndex())
    val index: StateFlow<ContentIndex> = _index.asStateFlow()

    private val loadMutex = Mutex()
    @Volatile
    private var loaded = false

    /** Ensures the corpus is loaded exactly once. */
    suspend fun ensureLoaded() {
        if (loaded) return
        loadMutex.withLock {
            if (loaded) return
            _index.value = loadIndex()
            loaded = true
        }
    }

    private suspend fun loadIndex(): ContentIndex = withContext(Dispatchers.IO) {
        val documents = mutableListOf<ContentDocument>()
        val errors = mutableListOf<String>()

        val files = try {
            listAssetFiles("curriculum")
        } catch (e: Exception) {
            errors += "Unable to enumerate curriculum assets: ${e.message}"
            emptyList()
        }

        val modules = mutableListOf<CurriculumModule>()
        val lessons = mutableListOf<Lesson>()
        val paths = mutableListOf<LearningPath>()
        val questions = mutableListOf<Question>()
        val frameworks = mutableListOf<FrameworkDefinition>()
        val mappings = mutableListOf<MappingPair>()
        val controls = mutableListOf<ControlDetail>()
        val scenarios = mutableListOf<Scenario>()
        val tools = mutableListOf<ToolDefinition>()
        val templates = mutableListOf<TemplateDocument>()
        val interviews = mutableListOf<InterviewTrack>()
        val projects = mutableListOf<CapstoneProject>()
        val accelerator = mutableListOf<AcceleratorDay>()
        val glossary = mutableListOf<GlossaryTerm>()

        for (file in files) {
            if (!file.endsWith(".json")) continue
            val text = try {
                assets.open(file).bufferedReader().use { it.readText() }
            } catch (e: Exception) {
                errors += "$file: could not be read (${e.message})"
                continue
            }
            val root = try {
                json.parseToJsonElement(text).jsonObject
            } catch (e: Exception) {
                errors += "$file: invalid JSON (${e.message})"
                continue
            }
            val kind = root["kind"]?.jsonPrimitive?.content ?: "UNKNOWN"
            val schemaVersion = root["schemaVersion"]?.jsonPrimitive?.content ?: "0"
            val items = root["items"]?.takeIf { it is JsonArray }?.jsonArray ?: JsonArray(emptyList())

            documents += ContentDocument(file, kind, schemaVersion, items.size, items)

            try {
                when (kind) {
                    "MODULES" -> modules += items.map { json.decodeFromJsonElement(CurriculumModule.serializer(), it) }
                    "LESSONS" -> lessons += items.map { json.decodeFromJsonElement(Lesson.serializer(), it) }
                    "PATHS" -> paths += items.map { json.decodeFromJsonElement(LearningPath.serializer(), it) }
                    "QUESTIONS" -> questions += items.map { json.decodeFromJsonElement(Question.serializer(), it) }
                    "FRAMEWORKS" -> frameworks += items.map { json.decodeFromJsonElement(FrameworkDefinition.serializer(), it) }
                    "MAPPINGS" -> mappings += items.map { json.decodeFromJsonElement(MappingPair.serializer(), it) }
                    "CONTROLS" -> controls += items.map { json.decodeFromJsonElement(ControlDetail.serializer(), it) }
                    "SCENARIOS" -> scenarios += items.map { json.decodeFromJsonElement(Scenario.serializer(), it) }
                    "TOOLS" -> tools += items.map { json.decodeFromJsonElement(ToolDefinition.serializer(), it) }
                    "TEMPLATES" -> templates += items.map { json.decodeFromJsonElement(TemplateDocument.serializer(), it) }
                    "INTERVIEWS" -> interviews += items.map { json.decodeFromJsonElement(InterviewTrack.serializer(), it) }
                    "PROJECTS" -> projects += items.map { json.decodeFromJsonElement(CapstoneProject.serializer(), it) }
                    "ACCELERATOR" -> accelerator += items.map { json.decodeFromJsonElement(AcceleratorDay.serializer(), it) }
                    "GLOSSARY" -> glossary += items.map { json.decodeFromJsonElement(GlossaryTerm.serializer(), it) }
                    else -> errors += "$file: unknown content kind '$kind'"
                }
            } catch (e: Exception) {
                errors += "$file: failed to decode '$kind' items (${e.message})"
            }
        }

        ContentIndex(
            modules = modules.sortedBy { it.order },
            lessons = lessons,
            paths = paths.sortedBy { it.order },
            questions = questions,
            frameworks = frameworks,
            mappings = mappings,
            controls = controls,
            scenarios = scenarios,
            tools = tools,
            templates = templates,
            interviews = interviews,
            projects = projects,
            accelerator = accelerator.sortedBy { it.day },
            glossary = glossary,
            documents = documents,
            loadErrors = errors
        )
    }

    private fun listAssetFiles(root: String): List<String> {
        val out = mutableListOf<String>()
        fun walk(path: String) {
            val children = try {
                assets.list(path)
            } catch (e: Exception) {
                emptyArray()
            }
            if (children.isNullOrEmpty()) return
            for (child in children) {
                val childPath = if (path.endsWith("/")) "$path$child" else "$path/$child"
                try {
                    assets.open(childPath).close()
                    out += childPath
                } catch (e: Exception) {
                    walk(childPath)
                }
            }
        }
        walk(root)
        return out.sorted()
    }

    // --- Typed accessors ---------------------------------------------------------

    suspend fun lessons(): List<Lesson> = ensureLoaded().let { index.value.lessons }
    suspend fun modules(): List<CurriculumModule> = ensureLoaded().let { index.value.modules }
    suspend fun questions(): List<Question> = ensureLoaded().let { index.value.questions }
    suspend fun frameworks(): List<FrameworkDefinition> = ensureLoaded().let { index.value.frameworks }
    suspend fun mappings(): List<MappingPair> = ensureLoaded().let { index.value.mappings }
    suspend fun controls(): List<ControlDetail> = ensureLoaded().let { index.value.controls }
    suspend fun scenarios(): List<Scenario> = ensureLoaded().let { index.value.scenarios }
    suspend fun tools(): List<ToolDefinition> = ensureLoaded().let { index.value.tools }
    suspend fun templates(): List<TemplateDocument> = ensureLoaded().let { index.value.templates }
    suspend fun interviews(): List<InterviewTrack> = ensureLoaded().let { index.value.interviews }
    suspend fun projects(): List<CapstoneProject> = ensureLoaded().let { index.value.projects }
    suspend fun accelerator(): List<AcceleratorDay> = ensureLoaded().let { index.value.accelerator }
    suspend fun glossary(): List<GlossaryTerm> = ensureLoaded().let { index.value.glossary }
    suspend fun paths(): List<LearningPath> = ensureLoaded().let { index.value.paths }

    fun lesson(id: String): Lesson? = index.value.lessonsById[id]
    fun question(id: String): Question? = index.value.questionsById[id]
    fun module(id: String): CurriculumModule? = index.value.modulesById[id]
    fun control(id: String): ControlDetail? = index.value.controlsById[id]

    fun lessonsForModule(moduleId: String): List<Lesson> {
        val module = index.value.modulesById[moduleId] ?: return emptyList()
        val ids = (module.lessonIds + module.advancedLessonIds).toSet()
        return index.value.lessons.filter { it.moduleId == moduleId || it.id in ids }
    }
}
