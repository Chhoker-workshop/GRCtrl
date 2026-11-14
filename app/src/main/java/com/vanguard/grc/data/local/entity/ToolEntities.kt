package com.vanguard.grc.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A saved record produced by a GRC Toolkit tool.
 *
 * [isSample] distinguishes demonstration records shipped with the toolkit from
 * records the learner actually entered. The UI always labels the two
 * differently so sample data is never mistaken for a real register.
 */
@Entity(tableName = "tool_records", indices = [Index("toolId"), Index("isSample"), Index("updatedAt")])
data class ToolRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val toolId: String,
    val title: String,
    val dataJson: String,
    val isSample: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/** State of a practical capstone project. */
@Entity(tableName = "projects", indices = [Index("status")])
data class ProjectEntity(
    @PrimaryKey val projectId: String,
    val title: String,
    val status: String = "NOT_STARTED",
    val progressPercent: Int = 0,
    val answersJson: String = "",
    val lastSection: String = "",
    val updatedAt: Long = System.currentTimeMillis()
)

/** A decision taken in the enterprise simulator. */
@Entity(tableName = "simulator_decisions", indices = [Index("scenarioId"), Index("decidedAt")])
data class SimulatorDecisionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenarioId: String,
    val difficulty: String,
    val optionIndex: Int,
    val score: Int,
    val maxScore: Int,
    val rationale: String,
    val decidedAt: Long = System.currentTimeMillis()
)

/** A completed mock interview, stored for review and scoring history. */
@Entity(tableName = "interview_sessions", indices = [Index("roleId")])
data class InterviewSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val roleId: String,
    val roleTitle: String,
    val answersJson: String,
    val score: Int,
    val dimensionScoresJson: String,
    val completedAt: Long = System.currentTimeMillis()
)
