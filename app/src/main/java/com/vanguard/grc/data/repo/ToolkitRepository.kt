package com.vanguard.grc.data.repo

import com.vanguard.grc.data.content.ToolDefinition
import com.vanguard.grc.data.local.dao.ToolDao
import com.vanguard.grc.data.local.entity.InterviewSessionEntity
import com.vanguard.grc.data.local.entity.ProjectEntity
import com.vanguard.grc.data.local.entity.SimulatorDecisionEntity
import com.vanguard.grc.data.local.entity.ToolRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * Persistence for toolkit registers, capstone project state, simulator
 * decisions and interview sessions.
 */
class ToolkitRepository(
    private val dao: ToolDao,
    private val tools: List<ToolDefinition>
) {

    fun records(toolId: String): Flow<List<ToolRecordEntity>> = dao.observeRecords(toolId)

    fun userRecords(toolId: String): Flow<List<ToolRecordEntity>> = dao.observeUserRecords(toolId)

    fun allRecords(): Flow<List<ToolRecordEntity>> = dao.observeAllRecords()

    fun userRecordCount(): Flow<Int> = dao.observeUserRecordCount()

    suspend fun getRecord(id: Long): ToolRecordEntity? = dao.getRecord(id)

    suspend fun saveRecord(
        id: Long?,
        toolId: String,
        title: String,
        dataJson: String,
        isSample: Boolean = false
    ): Long {
        val now = System.currentTimeMillis()
        val record = if (id == null || id == 0L) {
            ToolRecordEntity(
                toolId = toolId,
                title = title,
                dataJson = dataJson,
                isSample = isSample,
                createdAt = now,
                updatedAt = now
            )
        } else {
            val existing = dao.getRecord(id)
            (existing ?: ToolRecordEntity(toolId = toolId, title = title, dataJson = dataJson, createdAt = now))
                .copy(
                    title = title,
                    dataJson = dataJson,
                    isSample = isSample,
                    updatedAt = now
                )
        }
        return dao.upsertRecord(record)
    }

    suspend fun deleteRecord(id: Long) = dao.deleteRecord(id)

    suspend fun clearRecords() = dao.clearRecords()

    // --- Projects ------------------------------------------------------------------

    fun projects(): Flow<List<ProjectEntity>> = dao.observeProjects()

    suspend fun project(projectId: String): ProjectEntity? = dao.getProject(projectId)

    suspend fun saveProject(
        projectId: String,
        title: String,
        status: String,
        progressPercent: Int,
        answersJson: String,
        lastSection: String
    ) {
        dao.upsertProject(
            ProjectEntity(
                projectId = projectId,
                title = title,
                status = status,
                progressPercent = progressPercent.coerceIn(0, 100),
                answersJson = answersJson,
                lastSection = lastSection,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    // --- Simulator -----------------------------------------------------------------

    fun decisions(): Flow<List<SimulatorDecisionEntity>> = dao.observeDecisions()

    fun decisionCount(): Flow<Int> = dao.observeDecisionCount()

    suspend fun recordDecision(
        scenarioId: String,
        difficulty: String,
        optionIndex: Int,
        score: Int,
        maxScore: Int,
        rationale: String
    ) {
        dao.insertDecision(
            SimulatorDecisionEntity(
                scenarioId = scenarioId,
                difficulty = difficulty,
                optionIndex = optionIndex,
                score = score,
                maxScore = maxScore,
                rationale = rationale,
                decidedAt = System.currentTimeMillis()
            )
        )
    }

    // --- Interviews -----------------------------------------------------------------

    fun interviews(): Flow<List<InterviewSessionEntity>> = dao.observeInterviews()

    suspend fun saveInterview(
        roleId: String,
        roleTitle: String,
        answersJson: String,
        score: Int,
        dimensionScoresJson: String
    ) {
        dao.insertInterview(
            InterviewSessionEntity(
                roleId = roleId,
                roleTitle = roleTitle,
                answersJson = answersJson,
                score = score,
                dimensionScoresJson = dimensionScoresJson,
                completedAt = System.currentTimeMillis()
            )
        )
    }

    /** Seed the toolkit with the built-in sample record for each tool. */
    suspend fun seedSamples() {
        tools.forEach { tool ->
            if (tool.sampleData.isEmpty()) return@forEach
            if (dao.countSamples(tool.id) > 0) return@forEach
            dao.upsertRecord(
                ToolRecordEntity(
                    toolId = tool.id,
                    title = "Sample - ${tool.name}",
                    dataJson = sampleJson(tool),
                    isSample = true,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }

    private fun sampleJson(tool: ToolDefinition): String =
        tool.sampleData.entries.joinToString(",") { (k, v) ->
            "\"${k.escapeJson()}\":\"${v.escapeJson()}\""
        }

    private fun String.escapeJson(): String = this
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "")
}
