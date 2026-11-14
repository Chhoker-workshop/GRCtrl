package com.vanguard.grc.ui.screens.curriculum

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.DifficultyChip
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.ProgressLine
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun ModuleScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    moduleId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val module = content.modulesById[moduleId]
    val lessons = content.lessons.filter { it.moduleId == moduleId }
    val completed = progress.filter { it.completed }.map { it.contentId }.toSet()
    val done = lessons.count { it.id in completed }
    val pct = if (lessons.isEmpty()) 0f else done.toFloat() / lessons.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (module == null) {
            item {
                InfoCallout("Module not found in the current curriculum corpus.", kind = com.vanguard.grc.ui.components.CalloutKind.WARNING)
            }
            return@LazyColumn
        }

        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Text(
                    text = "MODULE ${module.order}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = module.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (module.subtitle.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = module.subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = module.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(14.dp))
                ProgressLine(progress = pct, color = MaterialTheme.colorScheme.secondary)
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$done of ${lessons.size} lessons complete - ${module.estimatedMinutes} min estimated",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item { SectionHeader(title = "Lessons", subtitle = "Work through in order for a complete treatment of the domain") }

        items(lessons) { lesson ->
            val isDone = lesson.id in completed
            VanguardCard(onClick = { navController.navigate(Routes.lesson(lesson.id)) }) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = if (isDone) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (isDone) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = lesson.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = lesson.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            DifficultyChip(lesson.difficulty)
                            com.vanguard.grc.ui.components.ChipPill(
                                text = "${lesson.estimatedMinutes} min",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (lesson.practicalExercise != null) {
                                com.vanguard.grc.ui.components.ChipPill(
                                    text = "Exercise",
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
