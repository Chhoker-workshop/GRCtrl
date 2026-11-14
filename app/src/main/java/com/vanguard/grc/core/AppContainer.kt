package com.vanguard.grc.core

import android.content.Context
import androidx.room.Room
import com.vanguard.grc.data.auth.AuthRepository
import com.vanguard.grc.data.local.AppDatabase
import com.vanguard.grc.data.content.ContentRepository
import com.vanguard.grc.data.content.ContentValidator
import com.vanguard.grc.data.settings.SettingsStore
import com.vanguard.grc.data.repo.AssessmentRepository
import com.vanguard.grc.data.repo.LearningRepository
import com.vanguard.grc.data.repo.ToolkitRepository
import com.vanguard.grc.domain.assessment.AssessmentEngine
import com.vanguard.grc.domain.competency.CompetencyEngine
import com.vanguard.grc.domain.export.ExportService
import com.vanguard.grc.domain.risk.RiskEngine
import com.vanguard.grc.domain.srs.SpacedRepetitionScheduler
import com.vanguard.grc.domain.toolkit.ToolRegistry

/**
 * Manual dependency container. Everything is created lazily and lives for the
 * lifetime of the process.
 */
class AppContainer(private val context: Context) {

    val settings: SettingsStore by lazy { SettingsStore(context) }

    val database: AppDatabase by lazy {
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .addMigrations(*AppDatabase.MIGRATIONS)
            .build()
    }

    val contentRepository: ContentRepository by lazy { ContentRepository(context.assets) }

    val contentValidator: ContentValidator by lazy { ContentValidator() }

    val learningRepository: LearningRepository by lazy {
        LearningRepository(database.learningDao(), contentRepository)
    }

    val assessmentRepository: AssessmentRepository by lazy {
        AssessmentRepository(database.assessmentDao(), contentRepository)
    }

    val toolkitRepository: ToolkitRepository by lazy {
        ToolkitRepository(database.toolDao(), ToolRegistry.all())
    }

    val authRepository: AuthRepository by lazy { AuthRepository() }

    val assessmentEngine: AssessmentEngine by lazy { AssessmentEngine() }

    val competencyEngine: CompetencyEngine by lazy { CompetencyEngine() }

    val riskEngine: RiskEngine by lazy { RiskEngine }

    val spacedRepetition: SpacedRepetitionScheduler by lazy { SpacedRepetitionScheduler() }

    val exportService: ExportService by lazy { ExportService(context) }
}
