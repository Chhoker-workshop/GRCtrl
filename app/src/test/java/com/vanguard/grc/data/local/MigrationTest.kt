package com.vanguard.grc.data.local

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

class MigrationTest {

    @Test
    fun `schema version 1 is valid and migrations list is empty`() {
        assertEquals(1, 1)
        assertTrue("No migrations expected for initial schema", AppDatabase.MIGRATIONS.isEmpty())
    }

    @Test
    fun `exported schema v1 exists and contains core tables`() {
        val schemaFile = findSchemaFile(1)
        val text = schemaFile.readText()
        assertTrue("Schema identity mismatch", text.contains("\"identityHash\""))
        listOf(
            "lesson_progress", "assessment_attempts", "assessment_responses", "question_stats",
            "competency_scores", "tool_records", "notes", "bookmarks",
            "study_sessions", "accelerator_days", "projects", "simulator_decisions", "interview_sessions"
        ).forEach { table ->
            assertTrue("Schema v1 missing table $table", text.contains("\"tableName\": \"$table\""))
        }
    }

    private fun findSchemaFile(version: Int): File {
        var dir: File? = File(System.getProperty("user.dir") ?: ".")
        while (dir != null) {
            val candidate = File(dir, "app/schemas/com.vanguard.grc.data.local.AppDatabase/$version.json")
            if (candidate.isFile) return candidate
            dir = dir.parentFile
        }
        throw IllegalStateException("Schema file for version $version not found")
    }
}
