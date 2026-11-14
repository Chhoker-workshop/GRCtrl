package com.vanguard.grc.ui.screens.accelerator

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.LabeledValue
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun AcceleratorDayScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    day: Int
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val days by vm.acceleratorDays.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val plan = content.acceleratorByDay[day]
    val record = days.firstOrNull { it.day == day }
    val lessons = plan?.lessonIds?.mapNotNull { content.lessonsById[it] }.orEmpty()
    val completedIds = progress.filter { it.completed }.map { it.contentId }.toSet()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (plan == null) {
            item {
                InfoCallout(
                    "Day $day is not present in the current accelerator content.",
                    kind = CalloutKind.WARNING
                )
            }
            return@LazyColumn
        }

        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("DAY $day OF 60")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = plan.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (plan.focus.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = plan.focus,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipPill("${plan.minutesCore}m core", color = MaterialTheme.colorScheme.primary)
                    ChipPill("${plan.minutesFramework}m framework", color = MaterialTheme.colorScheme.primary)
                    ChipPill("${plan.minutesExercise}m exercise", color = MaterialTheme.colorScheme.secondary)
                    ChipPill("${plan.minutesAssessment}m assessment", color = MaterialTheme.colorScheme.primary)
                    ChipPill("${plan.minutesRevision}m revision", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (plan.objectives.isNotEmpty()) {
            item {
                SectionHeader("Today's objectives")
                VanguardCard { BulletList(plan.objectives) }
            }
        }

        item {
            SectionHeader("Core study", subtitle = "Work through these lessons in order")
        }
        items(lessons) { lesson ->
            VanguardCard(onClick = { navController.navigate(Routes.lesson(lesson.id)) }) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = lesson.summary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                ChipPill(
                    text = if (lesson.id in completedIds) "Completed" else "${lesson.estimatedMinutes} min",
                    color = if (lesson.id in completedIds) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (plan.exercise.isNotBlank()) {
            item {
                SectionHeader("Practical enterprise exercise")
                VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                    Text(
                        text = plan.exercise,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (plan.deliverables.isNotEmpty()) {
            item {
                SectionHeader("Deliverables")
                VanguardCard { BulletList(plan.deliverables) }
            }
        }

        if (plan.assessmentIds.isNotEmpty()) {
            item {
                SectionHeader("Assessment and scenario questions")
                VanguardCard {
                    plan.assessmentIds.forEach { qid ->
                        val q = content.questionsById[qid]
                        VanguardCard(
                            onClick = { navController.navigate(Routes.PRACTICE_LAB) },
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = q?.stem ?: "Assessment item $qid",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (q != null) {
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    ChipPill(q.difficulty.name.lowercase().replaceFirstChar { it.uppercase() }, color = MaterialTheme.colorScheme.primary)
                                    ChipPill(q.domain.replace('_', ' '), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (plan.revision.isNotEmpty()) {
            item {
                SectionHeader("Revision and consolidation")
                VanguardCard { BulletList(plan.revision) }
            }
        }

        item {
            SectionHeader("Day record")
            VanguardCard {
                LabeledValue(
                    "Status",
                    if (record?.completedAt != null) "Completed" else "In progress"
                )
                Spacer(Modifier.height(8.dp))
                LabeledValue(
                    "Minutes logged",
                    "${record?.minutesLogged ?: 0} of ${plan.totalMinutes}"
                )
                if (record?.assessmentScore != null) {
                    Spacer(Modifier.height(8.dp))
                    LabeledValue("Assessment score", "${record.assessmentScore}%")
                }
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                vm.learning.logAcceleratorProgress(day, plan.totalMinutes)
                                vm.learning.logStudySession(plan.totalMinutes, null, "ACCELERATOR")
                                vm.learning.completeAcceleratorDay(day, record?.assessmentScore, record?.notes ?: "")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) { Text("Complete day") }
                    OutlinedButton(
                        onClick = {
                            scope.launch { vm.learning.logAcceleratorProgress(day, 30) }
                        }
                    ) { Text("+30 min") }
                }
            }
        }
    }
}
