package com.vanguard.grc.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vanguard.grc.GrcApplication
import com.vanguard.grc.data.content.ContentIndex
import com.vanguard.grc.data.content.ContentSearch
import com.vanguard.grc.data.content.SearchKind
import com.vanguard.grc.data.content.SearchResult
import com.vanguard.grc.data.content.ToolDefinition
import com.vanguard.grc.data.local.entity.AssessmentAttemptEntity
import com.vanguard.grc.data.local.entity.BookmarkEntity
import com.vanguard.grc.data.local.entity.NoteEntity
import com.vanguard.grc.data.local.entity.ProjectEntity
import com.vanguard.grc.data.local.entity.QuestionStatEntity
import com.vanguard.grc.data.local.entity.ToolRecordEntity
import com.vanguard.grc.data.settings.UserSettings
import com.vanguard.grc.domain.assessment.AssessmentEngine
import com.vanguard.grc.domain.competency.CompetencyEngine
import com.vanguard.grc.domain.competency.CompetencySnapshot
import com.vanguard.grc.domain.export.ExportService
import com.vanguard.grc.domain.export.ExportResult
import com.vanguard.grc.domain.srs.SpacedRepetitionScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Single view model for the platform. The application is small enough that a
 * single, well-scoped view model keeps state ownership obvious and avoids
 * fragment-style state loss across configuration changes.
 */
class GrcViewModel(application: Application) : AndroidViewModel(application) {

    private val container = (application as GrcApplication).container

    val settings: StateFlow<UserSettings> = container.settings.state
    val content: StateFlow<ContentIndex> = container.contentRepository.index

    val learning = container.learningRepository
    val assessments = container.assessmentRepository
    val toolkit = container.toolkitRepository

    val assessmentEngine: AssessmentEngine = container.assessmentEngine
    val competencyEngine: CompetencyEngine = container.competencyEngine
    val exportService: ExportService = container.exportService
    val spacedRepetition: SpacedRepetitionScheduler = container.spacedRepetition
    val authRepository = container.authRepository

    // --- Derived state -------------------------------------------------------------

    val progress: StateFlow<List<com.vanguard.grc.data.local.entity.LessonProgressEntity>> =
        learning.allProgress().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val minutesToday: StateFlow<Int> =
        learning.minutesToday().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val minutesWeek: StateFlow<Int> =
        learning.minutesThisWeek().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val streak: StateFlow<Int> =
        learning.streak().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val completedCount: StateFlow<Int> =
        learning.completedCount().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val attempts: StateFlow<List<AssessmentAttemptEntity>> =
        assessments.attempts().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val questionStats: StateFlow<List<QuestionStatEntity>> =
        assessments.questionStats().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val competency: StateFlow<CompetencySnapshot> =
        assessments.competency(competencyEngine)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), competencyEngine.compute(emptyList(), emptyList(), emptyMap()))

    val notes: StateFlow<List<NoteEntity>> =
        learning.allNotes().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarks: StateFlow<List<BookmarkEntity>> =
        learning.allBookmarks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val toolRecords: StateFlow<List<ToolRecordEntity>> =
        toolkit.allRecords().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val projects: StateFlow<List<ProjectEntity>> =
        toolkit.projects().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val simulatorDecisions: StateFlow<List<com.vanguard.grc.data.local.entity.SimulatorDecisionEntity>> =
        toolkit.decisions().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val interviewSessions: StateFlow<List<com.vanguard.grc.data.local.entity.InterviewSessionEntity>> =
        toolkit.interviews().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val acceleratorDays: StateFlow<List<com.vanguard.grc.data.local.entity.AcceleratorDayEntity>> =
        learning.acceleratorDays().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weakQuestions: StateFlow<List<QuestionStatEntity>> =
        assessments.weakQuestions(20).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Percentage of the curriculum completed, weighted by lesson count. */
    val curriculumProgress: StateFlow<Float> = combine(progress, content) { list, index ->
        val total = index.lessons.size
        if (total == 0) 0f else {
            val done = list.count { it.completed }
            done.toFloat() / total
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0f)

    // --- Search ---------------------------------------------------------------------

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchKind = MutableStateFlow<SearchKind?>(null)
    val searchKind: StateFlow<SearchKind?> = _searchKind.asStateFlow()

    private val searchIndex = MutableStateFlow<ContentSearch?>(null)

    val searchResults: StateFlow<List<SearchResult>> = combine(
        searchQuery,
        _searchKind,
        searchIndex
    ) { query, kind, index ->
        if (query.isBlank() || index == null) emptyList()
        else index.search(query, setOfNotNull(kind))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setSearchQuery(value: String) {
        _searchQuery.value = value
    }

    fun setSearchKind(kind: SearchKind?) {
        _searchKind.value = kind
    }

    /**
     * Builds the local search index once the corpus is available.
     */
    fun prepareSearch() {
        viewModelScope.launch {
            container.contentRepository.ensureLoaded()
            val idx = container.contentRepository.index.value
            searchIndex.value = ContentSearch(idx)
        }
    }

    // --- Lifecycle -------------------------------------------------------------------

    init {
        viewModelScope.launch {
            container.contentRepository.ensureLoaded()
            searchIndex.value = ContentSearch(container.contentRepository.index.value)
            toolkit.seedSamples()
        }
    }

    // --- Actions ----------------------------------------------------------------------

    fun updateSettings(transform: (UserSettings) -> UserSettings) = container.settings.update(transform)

    fun toolsByCategory(): Map<String, List<ToolDefinition>> =
        container.toolkitRepository.let { repo ->
            ToolRegistryBridge.all().groupBy { it.category }
        }

    suspend fun exportToolCsv(
        toolId: String,
        fileName: String,
        headers: List<String>,
        rows: List<List<String>>,
        title: String
    ): ExportResult = exportService.exportCsv(fileName, headers, rows, title)

    suspend fun exportPdf(
        fileName: String,
        title: String,
        subtitle: String,
        sections: List<ExportService.PdfSection>
    ): ExportResult = exportService.exportPdf(fileName, title, subtitle, sections)

    fun exportedFiles() = exportService.exportedFiles()

    fun deleteExport(file: java.io.File) = exportService.deleteExport(file)

    fun shareIntent(uri: android.net.Uri, mimeType: String) = exportService.shareIntent(uri, mimeType)

    suspend fun wipeLearnerData() {
        learning.clearAll()
        assessments.clearHistory()
        toolkit.clearRecords()
    }
}

/** Small indirection so the view model does not need to import the toolkit registry directly. */
private object ToolRegistryBridge {
    fun all(): List<ToolDefinition> = com.vanguard.grc.domain.toolkit.ToolRegistry.all()
}

/** Convenience helper for flows that need a one-shot snapshot. */
suspend fun <T> StateFlow<T>.snapshot(): T = value
