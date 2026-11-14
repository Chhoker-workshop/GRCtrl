package com.vanguard.grc.ui.screens.lesson

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.data.content.Lesson
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.DifficultyChip
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun LessonScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    lessonId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val lesson = content.lessonsById[lessonId]
    val progress by vm.progress.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()
    val isBookmarked by vm.learning.isBookmarked(lessonId).collectAsStateWithLifecycle(initialValue = false)
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    var noteText by remember(lessonId) { mutableStateOf("") }
    var showNoteField by remember(lessonId) { mutableStateOf(false) }
    var confidence by remember(lessonId) { mutableStateOf<Float?>(null) }

    LaunchedEffect(lessonId) {
        lesson?.let { vm.learning.markViewed(it) }
    }

    if (lesson == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                InfoCallout(
                    "This lesson is not present in the current curriculum corpus. It may have been retired in a later content version.",
                    kind = CalloutKind.WARNING
                )
            }
        }
        return
    }

    val currentProgress = progress.firstOrNull { it.contentId == lessonId }
    val lessonNotes = notes.filter { it.contentId == lessonId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Eyebrow(lesson.moduleId.replace('.', ' ').uppercase())
                Spacer(Modifier.weight(1f))
                IconButton(onClick = {
                    scope.launch {
                        vm.learning.toggleBookmark(
                            lessonId, lesson.title, lesson.topic, "LESSON", isBookmarked
                        )
                    }
                }) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = if (isBookmarked) "Remove bookmark" else "Bookmark",
                        tint = if (isBookmarked) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                text = lesson.title,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                DifficultyChip(lesson.difficulty)
                ChipPill("${lesson.estimatedMinutes} min", color = MaterialTheme.colorScheme.onSurfaceVariant)
                ChipPill("v${lesson.version}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (lesson.summary.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = lesson.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (lesson.objectives.isNotEmpty()) {
            item {
                VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                    Eyebrow("Learning objectives")
                    Spacer(Modifier.height(8.dp))
                    BulletList(lesson.objectives)
                }
            }
        }

        items(lesson.sections) { section ->
            VanguardCard {
                if (section.heading.isNotBlank()) {
                    Text(
                        text = section.heading,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                }
                if (section.body.isNotBlank()) {
                    Text(
                        text = section.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (section.bullets.isNotEmpty()) {
                    Spacer(Modifier.height(10.dp))
                    BulletList(section.bullets)
                }
                if (section.table.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    ComparisonTable(rows = section.table)
                }
                if (!section.callout.isNullOrBlank()) {
                    Spacer(Modifier.height(12.dp))
                    InfoCallout(section.callout, kind = CalloutKind.INFO)
                }
            }
        }

        if (lesson.examples.isNotEmpty()) {
            item { SectionHeader(title = "Worked examples", subtitle = "Applied across sectors") }
            items(lesson.examples) { example ->
                VanguardCard {
                    Text(
                        text = example.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (example.sector.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        ChipPill(example.sector, color = MaterialTheme.colorScheme.secondary)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = example.context,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Outcome: ${example.outcome}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        lesson.practicalExercise?.let { exercise ->
            item { SectionHeader(title = "Practical exercise") }
            item {
                VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                    Text(
                        text = exercise.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = exercise.brief,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (exercise.steps.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Eyebrow("Activities")
                        Spacer(Modifier.height(8.dp))
                        BulletList(exercise.steps)
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Deliverable: ${exercise.deliverable}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (exercise.evaluationCriteria.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Eyebrow("You will be judged on")
                        Spacer(Modifier.height(6.dp))
                        BulletList(exercise.evaluationCriteria)
                    }
                }
            }
        }

        if (lesson.references.isNotEmpty()) {
            item {
                VanguardCard {
                    Eyebrow("References and further reading")
                    Spacer(Modifier.height(8.dp))
                    lesson.references.forEach { ref ->
                        Text(
                            text = ref,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Content version ${lesson.version}, last reviewed ${lesson.lastReviewed}.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionHeader(title = "Personal notes")
        }
        if (lessonNotes.isNotEmpty()) {
            items(lessonNotes) { note ->
                VanguardCard {
                    Text(note.title, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(Modifier.height(4.dp))
                    Text(note.body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Updated ${java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.US).format(java.util.Date(note.updatedAt))}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (showNoteField) {
            item {
                VanguardCard {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Your note") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                scope.launch {
                                    vm.learning.saveNote(null, lessonId, "Note on ${lesson.title}", noteText, lesson.topic)
                                }
                                noteText = ""
                                showNoteField = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) { Text("Save note") }
                        OutlinedButton(onClick = { showNoteField = false }) { Text("Cancel") }
                    }
                }
            }
        }
            item {
                OutlinedButton(
                    onClick = { showNoteField = !showNoteField },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.EditNote, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(if (showNoteField) "Close note editor" else "Add a note")
                }
            }

        item {
            SectionHeader(title = "Self-assessment", subtitle = "Confidence feeds your revision schedule")
            VanguardCard {
                Text(
                    text = "How confident are you on this material now?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(25, 50, 75, 100).forEach { level ->
                        val chosen = confidence?.toInt() == level
                        ChipPill(
                            text = if (level == 100) "Confident" else "$level%",
                            color = if (chosen) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                            filled = chosen,
                            onClick = {
                                confidence = level.toFloat()
                                scope.launch { vm.learning.setConfidence(lessonId, level) }
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            if (currentProgress?.completed == true) {
                VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Lesson complete. Revisit it whenever you want - spacing your revision improves retention.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            } else {
                Button(
                    onClick = {
                        scope.launch {
                            vm.learning.markCompleted(lessonId, lesson.moduleId, lesson.title)
                            vm.learning.logStudySession(
                                (lesson.estimatedMinutes / 3).coerceAtLeast(5),
                                lessonId,
                                "LESSON"
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Mark lesson complete")
                }
            }
        }

        if (lesson.assessmentQuestionIds.isNotEmpty()) {
            item {
                OutlinedButton(
                    onClick = { navController.navigate(Routes.PRACTICE_LAB) },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Attempt the assessment items for this lesson") }
            }
        }
    }
}

@Composable
private fun ComparisonTable(rows: List<List<String>>) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        rows.forEachIndexed { index, row ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                row.forEachIndexed { colIndex, cell ->
                    Text(
                        text = cell,
                        style = if (index == 0) MaterialTheme.typography.labelMedium else MaterialTheme.typography.bodySmall,
                        color = if (index == 0) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (index == 0) FontWeight.SemiBold else FontWeight.Normal,
                        modifier = Modifier.weight(if (row.size > 1) 1f else 1f)
                    )
                }
            }
            if (index < rows.lastIndex) {
                androidx.compose.material3.HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }
        }
    }
}
