package com.vanguard.grc.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import com.vanguard.grc.data.local.entity.AcceleratorDayEntity
import com.vanguard.grc.data.local.entity.AssessmentAttemptEntity
import com.vanguard.grc.data.local.entity.AssessmentResponseEntity
import com.vanguard.grc.data.local.entity.BookmarkEntity
import com.vanguard.grc.data.local.entity.CompetencyEntity
import com.vanguard.grc.data.local.entity.LessonProgressEntity
import com.vanguard.grc.data.local.entity.NoteEntity
import com.vanguard.grc.data.local.entity.ProjectEntity
import com.vanguard.grc.data.local.entity.QuestionStatEntity
import com.vanguard.grc.data.local.entity.SimulatorDecisionEntity
import com.vanguard.grc.data.local.entity.StudySessionEntity
import com.vanguard.grc.data.local.entity.ToolRecordEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppDatabaseTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun lessonProgress_roundTripsAndPersists() = runBlocking {
        val dao = db.learningDao()
        dao.upsertProgress(
            LessonProgressEntity(
                contentId = "fnd.l01",
                moduleId = "fnd",
                title = "What Governance Actually Means",
                completed = true,
                completedAt = 1_000L,
                timeSpentSeconds = 600,
                bestScore = 85
            )
        )
        val stored = dao.getProgress("fnd.l01")
        assertNotNull(stored)
        assertTrue(stored!!.completed)
        assertEquals(85, stored.bestScore)
        assertEquals(600, stored.timeSpentSeconds)
    }

    @Test
    fun lessonProgress_completionIsSetIdempotently() = runBlocking {
        val dao = db.learningDao()
        dao.upsertProgress(LessonProgressEntity("fnd.l02", "fnd", "Title"))
        dao.markCompleted("fnd.l02", 2_000L)
        dao.markCompleted("fnd.l02", 3_000L)
        val stored = dao.getProgress("fnd.l02")!!
        assertTrue(stored.completed)
        assertEquals(2_000L, stored.completedAt)
    }

    @Test
    fun studySessions_accumulatePerDay() = runBlocking {
        val dao = db.learningDao()
        dao.insertSession(StudySessionEntity(epochDay = 100, minutes = 45, activityType = "LESSON"))
        dao.insertSession(StudySessionEntity(epochDay = 100, minutes = 30, activityType = "ASSESSMENT"))
        dao.insertSession(StudySessionEntity(epochDay = 101, minutes = 90, activityType = "LESSON"))
        assertEquals(75, dao.observeMinutesForDay(100).first())
        assertEquals(90, dao.observeMinutesForDay(101).first())
    }

    @Test
    fun notes_areUpsertedAndListed() = runBlocking {
        val dao = db.learningDao()
        val id = dao.upsertNote(NoteEntity(contentId = "fnd.l01", title = "Note", body = "Body"))
        assertTrue(id > 0)
        dao.upsertNote(NoteEntity(id = id, contentId = "fnd.l01", title = "Note v2", body = "Updated"))
        val notes = dao.observeNotesFor("fnd.l01").first()
        assertEquals(1, notes.size)
        assertEquals("Note v2", notes.first().title)
        assertEquals("Updated", notes.first().body)
    }

    @Test
    fun bookmarks_areIdempotentOnPrimaryKey() = runBlocking {
        val dao = db.learningDao()
        dao.upsertBookmark(BookmarkEntity(contentId = "ctl_a_5_1", title = "Control"))
        dao.upsertBookmark(BookmarkEntity(contentId = "ctl_a_5_1", title = "Control renamed"))
        val bookmarks = dao.observeAllBookmarks().first()
        assertEquals(1, bookmarks.size)
        assertEquals("Control renamed", bookmarks.first().title)
    }

    @Test
    fun acceleratorDays_areLoggedAndCompleted() = runBlocking {
        val dao = db.learningDao()
        dao.logAcceleratorMinutes(day = 7, epochDay = 42, minutes = 60)
        dao.logAcceleratorMinutes(day = 7, epochDay = 42, minutes = 60)
        val day = dao.getAcceleratorDay(7)!!
        assertEquals(120, day.minutesLogged)
        assertNull(day.completedAt)

        dao.upsertAcceleratorDay(
            AcceleratorDayEntity(
                day = 7,
                epochDay = 42,
                startedAt = day.startedAt,
                completedAt = 99_999L,
                minutesLogged = day.minutesLogged,
                assessmentScore = 78
            )
        )
        val completed = dao.getAcceleratorDay(7)!!
        assertNotNull(completed.completedAt)
        assertEquals(78, completed.assessmentScore)
        assertEquals(1, dao.observeAcceleratorCompletedDays().first())
    }

    @Test
    fun assessmentAttempt_isFinalisedWithItsResponses() = runBlocking {
        val dao = db.assessmentDao()
        val attemptId = dao.insertAttempt(
            AssessmentAttemptEntity(
                assessmentId = "dom_governance",
                title = "Governance Domain Exam",
                kind = "DOMAIN_EXAM",
                domain = "GOVERNANCE",
                startedAt = 1_000L,
                passMark = 70
            )
        )
        dao.insertResponses(
            listOf(
                AssessmentResponseEntity(attemptId = attemptId, questionId = "q1", domain = "GOVERNANCE", difficulty = "INTERMEDIATE", selectedOptionIds = "a", isCorrect = true),
                AssessmentResponseEntity(attemptId = attemptId, questionId = "q2", domain = "GOVERNANCE", difficulty = "ADVANCED", selectedOptionIds = "b", isCorrect = false)
            )
        )
        dao.finalizeAttempt(attemptId, at = 2_000L, score = 50, correct = 1, total = 2, seconds = 300, passed = false)
        val attempt = dao.getAttempt(attemptId)!!
        assertEquals(50, attempt.scorePercent)
        assertFalse(attempt.passed)
        assertEquals(2, dao.getResponses(attemptId).size)
    }

    @Test
    fun questionStats_areQueriedForWeaknessAndReview() = runBlocking {
        val dao = db.assessmentDao()
        dao.upsertQuestionStat(QuestionStatEntity("q1", "AUDIT", "Topic", seenCount = 4, correctCount = 1, wrongCount = 3, nextReviewAt = 1_000L))
        dao.upsertQuestionStat(QuestionStatEntity("q2", "PRIVACY", "Topic", seenCount = 2, correctCount = 2, wrongCount = 0, nextReviewAt = 9_999_999_999L))

        val weak = dao.observeWeakQuestions(10).first()
        assertEquals("q1", weak.first().questionId)

        val due = dao.observeDueForReview(now = 2_000L, limit = 10).first()
        assertEquals("q1", due.first().questionId)
    }

    @Test
    fun competencyScores_areCachedByDomain() = runBlocking {
        val dao = db.assessmentDao()
        dao.upsertCompetency(CompetencyEntity(domain = "GOVERNANCE", score = 72, confidenceWeight = 40, questionsAnswered = 15, accuracy = 75))
        val cached = dao.observeCompetencies().first()
        assertEquals(1, cached.size)
        assertEquals(72, cached.first().score)
    }

    @Test
    fun toolRecords_separateSamplesFromUserRecords() = runBlocking {
        val dao = db.toolDao()
        dao.upsertRecord(ToolRecordEntity(toolId = "risk_register", title = "Sample", dataJson = "{}", isSample = true))
        dao.upsertRecord(ToolRecordEntity(toolId = "risk_register", title = "Mine", dataJson = "{}", isSample = false))
        assertEquals(2, dao.observeRecords("risk_register").first().size)
        assertEquals(1, dao.observeUserRecords("risk_register").first().size)
        assertEquals(1, dao.observeUserRecordCount().first())
        assertEquals(1, dao.countSamples("risk_register"))
    }

    @Test
    fun projects_and_decisions_areStored() = runBlocking {
        val dao = db.toolDao()
        dao.upsertProject(ProjectEntity(projectId = "prj_01", title = "ISO 27001", status = "IN_PROGRESS", progressPercent = 40))
        dao.insertDecision(SimulatorDecisionEntity(scenarioId = "scn_01", difficulty = "ADVANCED", optionIndex = 1, score = 90, maxScore = 100, rationale = "Escalated"))
        assertEquals(40, dao.getProject("prj_01")!!.progressPercent)
        assertEquals(1, dao.observeDecisionCount().first())
    }

    @Test
    fun clearingProgressDoesNotDeleteOtherData() = runBlocking {
        db.learningDao().upsertProgress(LessonProgressEntity("fnd.l01", "fnd", "Title"))
        db.toolDao().upsertRecord(ToolRecordEntity(toolId = "t", title = "r", dataJson = "{}"))
        db.learningDao().clearProgress()
        assertNull(db.learningDao().getProgress("fnd.l01"))
        assertEquals(1, db.toolDao().observeAllRecords().first().size)
    }
}
