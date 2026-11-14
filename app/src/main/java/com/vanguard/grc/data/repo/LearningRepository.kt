package com.vanguard.grc.data.repo

import com.vanguard.grc.data.content.ContentRepository
import com.vanguard.grc.data.content.Lesson
import com.vanguard.grc.data.local.dao.LearningDao
import com.vanguard.grc.data.local.entity.AcceleratorDayEntity
import com.vanguard.grc.data.local.entity.BookmarkEntity
import com.vanguard.grc.data.local.entity.LessonProgressEntity
import com.vanguard.grc.data.local.entity.NoteEntity
import com.vanguard.grc.data.local.entity.StudySessionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.concurrent.TimeUnit

/**
 * Learning progress, study time, notes, bookmarks and the 60-day accelerator.
 */
class LearningRepository(
    private val dao: LearningDao,
    private val content: ContentRepository
) {

    fun allProgress(): Flow<List<LessonProgressEntity>> = dao.observeAllProgress()

    fun moduleProgress(moduleId: String): Flow<List<LessonProgressEntity>> =
        dao.observeModuleProgress(moduleId)

    fun completedCount(): Flow<Int> = dao.observeCompletedCount()

    suspend fun progressFor(contentId: String): LessonProgressEntity? = dao.getProgress(contentId)

    suspend fun progressForIds(ids: List<String>): List<LessonProgressEntity> =
        if (ids.isEmpty()) emptyList() else dao.getProgressFor(ids)

    /**
     * Records that a lesson was opened, creating the progress row on first view.
     */
    suspend fun markViewed(lesson: Lesson) {
        val existing = dao.getProgress(lesson.id)
        if (existing == null) {
            dao.upsertProgress(
                LessonProgressEntity(
                    contentId = lesson.id,
                    moduleId = lesson.moduleId,
                    title = lesson.title,
                    completed = false,
                    firstViewedAt = System.currentTimeMillis(),
                    lastViewedAt = System.currentTimeMillis()
                )
            )
        } else {
            dao.touch(lesson.id, System.currentTimeMillis())
        }
    }

    suspend fun markCompleted(contentId: String, moduleId: String, title: String) {
        val now = System.currentTimeMillis()
        val existing = dao.getProgress(contentId)
        if (existing == null) {
            dao.upsertProgress(
                LessonProgressEntity(
                    contentId = contentId,
                    moduleId = moduleId,
                    title = title,
                    completed = true,
                    completedAt = now,
                    firstViewedAt = now,
                    lastViewedAt = now
                )
            )
        } else {
            dao.markCompleted(contentId, now)
        }
    }

    suspend fun logTime(contentId: String, seconds: Int) {
        if (seconds <= 0) return
        dao.addTime(contentId, seconds, System.currentTimeMillis())
    }

    suspend fun recordScore(contentId: String, score: Int) = dao.recordScore(contentId, score)

    suspend fun setConfidence(contentId: String, confidence: Int) =
        dao.setConfidence(contentId, confidence.coerceIn(0, 100))

    suspend fun scheduleRevision(contentId: String, nextReview: Long) =
        dao.scheduleRevision(contentId, nextReview)

    // --- Study time ---------------------------------------------------------------

    suspend fun logStudySession(minutes: Int, contentId: String?, activityType: String) {
        if (minutes <= 0) return
        dao.insertSession(
            StudySessionEntity(
                epochDay = today(),
                minutes = minutes,
                contentId = contentId,
                activityType = activityType
            )
        )
    }

    fun minutesToday(): Flow<Int> = dao.observeMinutesForDay(today())

    fun minutesThisWeek(): Flow<Int> {
        val cal = Calendar.getInstance()
        val start = cal.clone() as Calendar
        start.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.set(Calendar.SECOND, 0)
        val end = cal.clone() as Calendar
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        return dao.observeMinutesInRange(start.timeInMillis / DAY, end.timeInMillis / DAY)
    }

    fun minutesTotal(): Flow<Int> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        val end = Calendar.getInstance()
        end.set(Calendar.HOUR_OF_DAY, 23)
        end.set(Calendar.MINUTE, 59)
        end.set(Calendar.SECOND, 59)
        return dao.observeMinutesInRange(cal.timeInMillis / DAY, end.timeInMillis / DAY)
    }

    fun activeDays(): Flow<List<Long>> = dao.observeRecentActiveDays(120)

    /**
     * Current streak in consecutive days ending today (or yesterday, so the
     * streak is not broken before the learner has had a chance to study today).
     */
    fun streak(): Flow<Int> = dao.observeRecentActiveDays(400).map { days ->
        if (days.isEmpty()) return@map 0
        val dayMs = DAY
        val today = today()
        val set = days.toSet()
        var cursor = if (set.contains(today)) today else today - 1
        var count = 0
        while (set.contains(cursor)) {
            count++
            cursor -= 1
        }
        count
    }

    // --- Notes and bookmarks ------------------------------------------------------

    fun notesFor(contentId: String): Flow<List<NoteEntity>> = dao.observeNotesFor(contentId)

    fun allNotes(): Flow<List<NoteEntity>> = dao.observeAllNotes()

    suspend fun saveNote(id: Long?, contentId: String, title: String, body: String, tags: String): Long {
        val now = System.currentTimeMillis()
        return if (id == null || id == 0L) {
            dao.upsertNote(
                NoteEntity(
                    contentId = contentId,
                    title = title.ifBlank { "Untitled note" },
                    body = body,
                    tags = tags,
                    createdAt = now,
                    updatedAt = now
                )
            )
        } else {
            val existing = dao.getNote(id)
            dao.upsertNote(
                (existing ?: NoteEntity(contentId = contentId, title = title, body = body))
                    .copy(
                        title = title.ifBlank { existing?.title ?: "Untitled note" },
                        body = body,
                        tags = tags,
                        updatedAt = now
                    )
            )
            id
        }
    }

    suspend fun deleteNote(id: Long) = dao.deleteNote(id)

    fun isBookmarked(contentId: String): Flow<Boolean> = dao.observeIsBookmarked(contentId)

    fun allBookmarks(): Flow<List<BookmarkEntity>> = dao.observeAllBookmarks()

    suspend fun toggleBookmark(contentId: String, title: String, subtitle: String, kind: String, bookmarked: Boolean) {
        if (bookmarked) {
            dao.removeBookmark(contentId)
        } else {
            dao.upsertBookmark(
                BookmarkEntity(contentId = contentId, title = title, subtitle = subtitle, kind = kind)
            )
        }
    }

    // --- Accelerator ---------------------------------------------------------------

    fun acceleratorDays(): Flow<List<AcceleratorDayEntity>> = dao.observeAcceleratorDays()

    fun acceleratorCompletedDays(): Flow<Int> = dao.observeAcceleratorCompletedDays()

    suspend fun acceleratorDay(day: Int): AcceleratorDayEntity? = dao.getAcceleratorDay(day)

    suspend fun logAcceleratorProgress(day: Int, minutes: Int) {
        dao.logAcceleratorMinutes(day, today(), minutes)
    }

    suspend fun completeAcceleratorDay(day: Int, score: Int?, notes: String) {
        val existing = dao.getAcceleratorDay(day)
        dao.upsertAcceleratorDay(
            AcceleratorDayEntity(
                day = day,
                epochDay = today(),
                startedAt = existing?.startedAt ?: System.currentTimeMillis(),
                completedAt = System.currentTimeMillis(),
                minutesLogged = existing?.minutesLogged ?: 0,
                assessmentScore = score ?: existing?.assessmentScore,
                notes = notes.ifBlank { existing?.notes ?: "" }
            )
        )
    }

    // --- Maintenance ----------------------------------------------------------------

    suspend fun clearAll() {
        dao.clearProgress()
        dao.clearSessions()
        dao.clearNotes()
        dao.clearBookmarks()
    }

    companion object {
        val DAY: Long = TimeUnit.DAYS.toMillis(1)

        fun today(): Long = System.currentTimeMillis() / DAY
    }
}
