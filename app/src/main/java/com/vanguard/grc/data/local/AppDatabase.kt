package com.vanguard.grc.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.vanguard.grc.data.local.dao.AssessmentDao
import com.vanguard.grc.data.local.dao.LearningDao
import com.vanguard.grc.data.local.dao.ToolDao
import com.vanguard.grc.data.local.entity.AcceleratorDayEntity
import com.vanguard.grc.data.local.entity.AssessmentAttemptEntity
import com.vanguard.grc.data.local.entity.AssessmentResponseEntity
import com.vanguard.grc.data.local.entity.BookmarkEntity
import com.vanguard.grc.data.local.entity.CompetencyEntity
import com.vanguard.grc.data.local.entity.InterviewSessionEntity
import com.vanguard.grc.data.local.entity.LessonProgressEntity
import com.vanguard.grc.data.local.entity.NoteEntity
import com.vanguard.grc.data.local.entity.ProjectEntity
import com.vanguard.grc.data.local.entity.QuestionStatEntity
import com.vanguard.grc.data.local.entity.SimulatorDecisionEntity
import com.vanguard.grc.data.local.entity.StudySessionEntity
import com.vanguard.grc.data.local.entity.ToolRecordEntity

class Converters {
    @TypeConverter
    fun fromLongList(value: List<Long>?): String =
        value?.joinToString(",") ?: ""

    @TypeConverter
    fun toLongList(value: String): List<Long> =
        if (value.isBlank()) emptyList() else value.split(",").mapNotNull { it.toLongOrNull() }
}

@Database(
    entities = [
        LessonProgressEntity::class,
        StudySessionEntity::class,
        NoteEntity::class,
        BookmarkEntity::class,
        AcceleratorDayEntity::class,
        AssessmentAttemptEntity::class,
        AssessmentResponseEntity::class,
        QuestionStatEntity::class,
        CompetencyEntity::class,
        ToolRecordEntity::class,
        ProjectEntity::class,
        SimulatorDecisionEntity::class,
        InterviewSessionEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun learningDao(): LearningDao
    abstract fun assessmentDao(): AssessmentDao
    abstract fun toolDao(): ToolDao

    companion object {
        const val DATABASE_NAME = "grc_vanguard.db"

        /**
         * Version 1 is the initial schema, so no migrations exist yet. The
         * array is kept (and referenced from the builder) so that future
         * versions add entries without changing construction sites.
         */
        val MIGRATIONS: Array<Migration> = arrayOf()
    }
}

/**
 * Helper for building a database in tests with an in-memory or file-backed
 * SQLite store.
 */
fun buildDatabase(context: Context, name: String? = null): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, name ?: AppDatabase.DATABASE_NAME)
        .addMigrations(*AppDatabase.MIGRATIONS)
        .build()
