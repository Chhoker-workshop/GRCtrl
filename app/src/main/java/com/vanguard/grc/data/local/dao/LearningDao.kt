package com.vanguard.grc.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.vanguard.grc.data.local.entity.AcceleratorDayEntity
import com.vanguard.grc.data.local.entity.BookmarkEntity
import com.vanguard.grc.data.local.entity.LessonProgressEntity
import com.vanguard.grc.data.local.entity.NoteEntity
import com.vanguard.grc.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LearningDao {

    // --- Lesson progress --------------------------------------------------------

    @Query("SELECT * FROM lesson_progress WHERE contentId = :contentId")
    suspend fun getProgress(contentId: String): LessonProgressEntity?

    @Query("SELECT * FROM lesson_progress")
    fun observeAllProgress(): Flow<List<LessonProgressEntity>>

    @Query("SELECT * FROM lesson_progress WHERE contentId IN (:ids)")
    suspend fun getProgressFor(ids: List<String>): List<LessonProgressEntity>

    @Query("SELECT * FROM lesson_progress WHERE moduleId = :moduleId")
    fun observeModuleProgress(moduleId: String): Flow<List<LessonProgressEntity>>

    @Query("SELECT COUNT(*) FROM lesson_progress WHERE completed = 1")
    fun observeCompletedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertProgress(entity: LessonProgressEntity)

    @Query("UPDATE lesson_progress SET completed = 1, completedAt = COALESCE(completedAt, :at), lastViewedAt = :at WHERE contentId = :contentId")
    suspend fun markCompleted(contentId: String, at: Long)

    @Query("UPDATE lesson_progress SET lastViewedAt = :at WHERE contentId = :contentId")
    suspend fun touch(contentId: String, at: Long)

    @Query("UPDATE lesson_progress SET timeSpentSeconds = timeSpentSeconds + :seconds, lastViewedAt = :at WHERE contentId = :contentId")
    suspend fun addTime(contentId: String, seconds: Int, at: Long)

    @Query("UPDATE lesson_progress SET bestScore = :score WHERE contentId = :contentId AND (bestScore IS NULL OR :score > bestScore)")
    suspend fun recordScore(contentId: String, score: Int)

    @Query("UPDATE lesson_progress SET revisionCount = revisionCount + 1, nextReviewAt = :nextReview WHERE contentId = :contentId")
    suspend fun scheduleRevision(contentId: String, nextReview: Long)

    @Query("UPDATE lesson_progress SET confidence = :confidence WHERE contentId = :contentId")
    suspend fun setConfidence(contentId: String, confidence: Int)

    @Query("UPDATE lesson_progress SET masteredObjectives = :flags WHERE contentId = :contentId")
    suspend fun setMasteredObjectives(contentId: String, flags: String)

    @Query("DELETE FROM lesson_progress")
    suspend fun clearProgress()

    // --- Study sessions ---------------------------------------------------------

    @Insert
    suspend fun insertSession(session: StudySessionEntity)

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM study_sessions WHERE epochDay = :epochDay")
    fun observeMinutesForDay(epochDay: Long): Flow<Int>

    @Query("SELECT COALESCE(SUM(minutes), 0) FROM study_sessions WHERE epochDay >= :fromDay AND epochDay <= :toDay")
    fun observeMinutesInRange(fromDay: Long, toDay: Long): Flow<Int>

    @Query("SELECT DISTINCT epochDay FROM study_sessions ORDER BY epochDay DESC LIMIT :limit")
    fun observeRecentActiveDays(limit: Int): Flow<List<Long>>

    @Query("SELECT * FROM study_sessions WHERE epochDay = :epochDay")
    suspend fun getSessionsForDay(epochDay: Long): List<StudySessionEntity>

    @Query("DELETE FROM study_sessions")
    suspend fun clearSessions()

    // --- Notes ------------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertNote(note: NoteEntity): Long

    @Query("SELECT * FROM notes WHERE contentId = :contentId ORDER BY updatedAt DESC")
    fun observeNotesFor(contentId: String): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes ORDER BY updatedAt DESC")
    fun observeAllNotes(): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun getNote(id: Long): NoteEntity?

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: Long)

    @Query("DELETE FROM notes")
    suspend fun clearNotes()

    // --- Bookmarks --------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarks WHERE contentId = :contentId")
    suspend fun removeBookmark(contentId: String)

    @Query("SELECT EXISTS(SELECT 1 FROM bookmarks WHERE contentId = :contentId)")
    fun observeIsBookmarked(contentId: String): Flow<Boolean>

    @Query("SELECT * FROM bookmarks ORDER BY createdAt DESC")
    fun observeAllBookmarks(): Flow<List<BookmarkEntity>>

    @Query("DELETE FROM bookmarks")
    suspend fun clearBookmarks()

    // --- Accelerator ------------------------------------------------------------

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAcceleratorDay(entity: AcceleratorDayEntity)

    @Query("SELECT * FROM accelerator_days ORDER BY day ASC")
    fun observeAcceleratorDays(): Flow<List<AcceleratorDayEntity>>

    @Query("SELECT * FROM accelerator_days WHERE day = :day")
    suspend fun getAcceleratorDay(day: Int): AcceleratorDayEntity?

    @Query("SELECT COUNT(*) FROM accelerator_days WHERE completedAt IS NOT NULL")
    fun observeAcceleratorCompletedDays(): Flow<Int>

    @Transaction
    suspend fun logAcceleratorMinutes(day: Int, epochDay: Long, minutes: Int) {
        val existing = getAcceleratorDay(day)
        upsertAcceleratorDay(
            AcceleratorDayEntity(
                day = day,
                epochDay = epochDay,
                startedAt = existing?.startedAt ?: System.currentTimeMillis(),
                completedAt = existing?.completedAt,
                minutesLogged = (existing?.minutesLogged ?: 0) + minutes,
                assessmentScore = existing?.assessmentScore,
                notes = existing?.notes ?: ""
            )
        )
    }
}
