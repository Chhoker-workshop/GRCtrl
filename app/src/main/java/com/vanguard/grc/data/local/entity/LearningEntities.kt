package com.vanguard.grc.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Per-learning-unit progress.
 *
 * [contentId] is the stable content identifier used across the JSON
 * curriculum (for example `fnd.m01.gv.01`). Progress rows are keyed by that
 * identifier so content can be re-versioned without losing learner history.
 */
@Entity(
    tableName = "lesson_progress",
    indices = [Index("moduleId"), Index("completed"), Index("lastViewedAt")]
)
data class LessonProgressEntity(
    @PrimaryKey val contentId: String,
    val moduleId: String,
    val title: String,
    val completed: Boolean = false,
    val completedAt: Long? = null,
    val firstViewedAt: Long = System.currentTimeMillis(),
    val lastViewedAt: Long = System.currentTimeMillis(),
    val timeSpentSeconds: Int = 0,
    /** Best assessed score, 0..100. Null when the unit has no assessment. */
    val bestScore: Int? = null,
    /** Number of times the unit has been revisited for revision. */
    val revisionCount: Int = 0,
    /** Self-reported confidence, 0..100. */
    val confidence: Int? = null,
    /** Next spaced-repetition review timestamp (epoch millis). */
    val nextReviewAt: Long? = null,
    /** Learning-objective mastery flags, comma separated. */
    val masteredObjectives: String = ""
)

/**
 * Daily study-time ledger. One row per calendar day per content unit is
 * avoided - instead a single row per day records total minutes plus the unit
 * studied, which is enough to drive streaks and goal tracking.
 */
@Entity(tableName = "study_sessions", indices = [Index("epochDay")])
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val epochDay: Long,
    val minutes: Int,
    val contentId: String? = null,
    val activityType: String,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "notes",
    indices = [Index("contentId"), Index("updatedAt")]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contentId: String,
    val title: String,
    val body: String,
    val tags: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "bookmarks", indices = [Index("createdAt")])
data class BookmarkEntity(
    @PrimaryKey val contentId: String,
    val title: String,
    val subtitle: String = "",
    val kind: String = "LESSON",
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Tracks the 60-day accelerator. One row per calendar day that the learner
 * engages with the programme.
 */
@Entity(tableName = "accelerator_days", indices = [Index("epochDay")])
data class AcceleratorDayEntity(
    @PrimaryKey val day: Int,
    val epochDay: Long,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val minutesLogged: Int = 0,
    val assessmentScore: Int? = null,
    val notes: String = ""
)
