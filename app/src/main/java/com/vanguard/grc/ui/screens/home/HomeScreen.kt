package com.vanguard.grc.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.TrackChanges
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.data.content.Lesson
import com.vanguard.grc.domain.competency.CompetencyBand
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.ListRowCard
import com.vanguard.grc.ui.components.ProgressLine
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.StatTile
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import com.vanguard.grc.ui.theme.Gold400
import com.vanguard.grc.ui.theme.Blue400
import com.vanguard.grc.ui.theme.Navy600
import com.vanguard.grc.ui.theme.RiskLow

@Composable
fun HomeScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val content by vm.content.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val minutesToday by vm.minutesToday.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    val competency by vm.competency.collectAsStateWithLifecycle()
    val weak by vm.weakQuestions.collectAsStateWithLifecycle()
    val curriculumProgress by vm.curriculumProgress.collectAsStateWithLifecycle()
    val attempts by vm.attempts.collectAsStateWithLifecycle()
    val notes by vm.notes.collectAsStateWithLifecycle()

    val completedIds = progress.filter { it.completed }.map { it.contentId }.toSet()
    val continueLesson: Lesson? = content.lessons
        .filter { it.id !in completedIds }
        .minByOrNull { it.order0() }
        ?: content.lessons.firstOrNull { it.id !in completedIds }

    val acceleratorNext = content.accelerator.firstOrNull { day ->
        content.acceleratorByDay[day.day]?.let { false } ?: true
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            GreetingBlock(
                name = settings.learnerName,
                curriculumProgress = curriculumProgress,
                minutesToday = minutesToday,
                goalMinutes = settings.dailyGoalMinutes
            )
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile(
                    label = "STREAK",
                    value = "$streak d",
                    accent = Gold400,
                    caption = "Consecutive study days",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "TODAY",
                    value = "$minutesToday m",
                    accent = Blue400,
                    caption = "of ${settings.dailyGoalMinutes} min goal",
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "COMPETENCY",
                    value = "${competency.overall}",
                    accent = if (competency.overallBand == CompetencyBand.NOVICE) MaterialTheme.colorScheme.onSurfaceVariant else RiskLow,
                    caption = competency.overallBand.label,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            VanguardCard(accent = Gold400) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AutoAwesome,
                        contentDescription = null,
                        tint = Gold400,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Eyebrow("Daily GRC Challenge")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = dailyChallenge(content, content.questions.size),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ActionChip("Open practice lab", Icons.Filled.PlayArrow) {
                        navController.navigate(Routes.PRACTICE_LAB)
                    }
                    ActionChip("Today's plan", Icons.Outlined.MenuBook) {
                        navController.navigate(Routes.ACCELERATOR)
                    }
                }
            }
        }

        if (continueLesson != null) {
            item {
                SectionHeader(
                    title = "Continue learning",
                    subtitle = "Pick up where you left off"
                )
            }
            item {
                VanguardCard(
                    onClick = { navController.navigate(Routes.lesson(continueLesson.id)) },
                    accent = Blue400
                ) {
                    Eyebrow(continueLesson.moduleId.replace('.', ' ').uppercase())
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = continueLesson.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = continueLesson.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "${continueLesson.estimatedMinutes} min",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(14.dp))
                        Icon(
                            Icons.Outlined.TrackChanges,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = continueLesson.difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Today's study plan",
                subtitle = "The 60-day accelerator, staged at ${settings.dailyGoalMinutes} minutes a day"
            )
        }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(content.accelerator.take(7)) { day ->
                    VanguardCard(
                        modifier = Modifier.width(150.dp),
                        onClick = { navController.navigate(Routes.acceleratorDay(day.day)) }
                    ) {
                        Eyebrow("DAY ${day.day}")
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = day.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3
                        )
                        Spacer(Modifier.height(8.dp))
                        ProgressLine(
                            progress = 0f,
                            color = Gold400
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${day.totalMinutes} min planned",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "My competency",
                subtitle = "Computed from demonstrated assessment performance, not study time"
            )
        }
        item {
            VanguardCard {
                competency.domains.take(6).forEach { domain ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = domain.domain.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${domain.questionsAnswered} answered - confidence ${domain.confidence}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Box(Modifier.width(90.dp)) {
                            ProgressLine(
                                progress = domain.score / 100f,
                                color = when {
                                    domain.score >= 70 -> RiskLow
                                    domain.score >= 45 -> Gold400
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${domain.score}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                ListRowCard(
                    title = "Open full competency report",
                    subtitle = "Strengths, weaknesses, revision schedule and readiness statement",
                    onClick = { navController.navigate(Routes.PROGRESS) }
                )
            }
        }

        if (weak.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Weak areas",
                    subtitle = "Topics you should revise today"
                )
            }
            item {
                VanguardCard {
                    weak.take(5).forEach { stat ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = stat.topic.ifBlank { stat.domain },
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${stat.wrongCount} wrong of ${stat.seenCount} seen",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ChipPill(text = stat.domain.replace('_', ' '), color = Gold400)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    ListRowCard(
                        title = "Start targeted revision",
                        subtitle = "A daily revision list built from your incorrect answers",
                        onClick = { navController.navigate(Routes.PRACTICE_LAB) }
                    )
                }
            }
        }

        item {
            SectionHeader(title = "Recently studied")
        }
        val recent = progress.sortedByDescending { it.lastViewedAt }.take(5)
        if (recent.isEmpty()) {
            item {
                VanguardCard {
                    Text(
                        text = "Nothing studied yet. Open a lesson from the curriculum to begin, and your position, notes and revision schedule will be tracked from that point.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(recent) { entry ->
                ListRowCard(
                    title = entry.title,
                    subtitle = if (entry.completed) "Completed - ${entry.timeSpentSeconds / 60} min logged" else "In progress",
                    trailingText = if (entry.completed) "Done" else "Open",
                    onClick = { navController.navigate(Routes.lesson(entry.contentId)) }
                )
            }
        }

        item {
            InfoCallout(
                text = "GRC VANGUARD is an educational platform. It does not confer professional certification, and completing a checklist here does not make an organisation compliant with any standard, law or regulator.",
                kind = com.vanguard.grc.ui.components.CalloutKind.INFO
            )
        }
    }
}

@Composable
private fun GreetingBlock(
    name: String,
    curriculumProgress: Float,
    minutesToday: Int,
    goalMinutes: Int
) {
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.linearGradient(
                    listOf(Navy600, MaterialTheme.colorScheme.background)
                )
            )
            .padding(18.dp)
    ) {
        Eyebrow("GRC VANGUARD")
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (name.isBlank()) "From GRC fundamentals to enterprise leadership"
            else "Welcome back, $name",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "From GRC fundamentals to enterprise leadership",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(14.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProgressLine(
                progress = curriculumProgress,
                color = Gold400,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                text = "${(curriculumProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = Gold400
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Curriculum complete - $minutesToday of $goalMinutes minutes studied today",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ActionChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .clickableNoRipple(onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(15.dp), tint = Gold400)
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun Modifier.clickableNoRipple(onClick: () -> Unit): Modifier =
    this.then(Modifier.clickable { onClick() })

private fun dailyChallenge(
    content: com.vanguard.grc.data.content.ContentIndex,
    questionCount: Int
): String {
    val lessons = content.lessons
    if (lessons.isEmpty()) {
        return "The curriculum is loading. Once ready you will get a daily challenge drawn from across the GRC domains."
    }
    val day = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
    val lesson = lessons[(day % lessons.size).toInt()]
    return "From \"${lesson.title}\": explain in three sentences how you would evidence that this control actually operates."
}

private fun Lesson.order0(): Int = estimatedMinutes
