package com.vanguard.grc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.vanguard.grc.data.local.entity.InterviewSessionEntity
import com.vanguard.grc.data.local.entity.ProjectEntity
import com.vanguard.grc.data.local.entity.SimulatorDecisionEntity
import com.vanguard.grc.data.local.entity.ToolRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ToolDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRecord(record: ToolRecordEntity): Long

    @Query("SELECT * FROM tool_records WHERE toolId = :toolId ORDER BY isSample ASC, updatedAt DESC")
    fun observeRecords(toolId: String): Flow<List<ToolRecordEntity>>

    @Query("SELECT * FROM tool_records WHERE toolId = :toolId AND isSample = 0 ORDER BY updatedAt DESC")
    fun observeUserRecords(toolId: String): Flow<List<ToolRecordEntity>>

    @Query("SELECT * FROM tool_records WHERE id = :id")
    suspend fun getRecord(id: Long): ToolRecordEntity?

    @Query("SELECT * FROM tool_records ORDER BY updatedAt DESC")
    fun observeAllRecords(): Flow<List<ToolRecordEntity>>

    @Query("SELECT COUNT(*) FROM tool_records WHERE isSample = 0")
    fun observeUserRecordCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM tool_records WHERE toolId = :toolId AND isSample = 1")
    suspend fun countSamples(toolId: String): Int

    @Query("DELETE FROM tool_records WHERE id = :id")
    suspend fun deleteRecord(id: Long)

    @Query("DELETE FROM tool_records")
    suspend fun clearRecords()

    // --- Projects ---------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProject(project: ProjectEntity)

    @Query("SELECT * FROM projects")
    fun observeProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM projects WHERE projectId = :projectId")
    suspend fun getProject(projectId: String): ProjectEntity?

    @Query("DELETE FROM projects")
    suspend fun clearProjects()

    // --- Simulator --------------------------------------------------------------

    @Insert
    suspend fun insertDecision(decision: SimulatorDecisionEntity)

    @Query("SELECT * FROM simulator_decisions ORDER BY decidedAt DESC")
    fun observeDecisions(): Flow<List<SimulatorDecisionEntity>>

    @Query("SELECT COUNT(*) FROM simulator_decisions")
    fun observeDecisionCount(): Flow<Int>

    @Query("DELETE FROM simulator_decisions")
    suspend fun clearDecisions()

    // --- Interviews -------------------------------------------------------------

    @Insert
    suspend fun insertInterview(session: InterviewSessionEntity)

    @Query("SELECT * FROM interview_sessions ORDER BY completedAt DESC")
    fun observeInterviews(): Flow<List<InterviewSessionEntity>>

    @Query("DELETE FROM interview_sessions")
    suspend fun clearInterviews()
}
