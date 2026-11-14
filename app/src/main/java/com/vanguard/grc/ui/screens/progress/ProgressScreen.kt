package com.vanguard.grc.ui.screens.progress

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.Insights
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
import com.vanguard.grc.domain.competency.CompetencyBand
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.ProgressLine
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.StatTile
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import com.vanguard.grc.ui.theme.Gold400
import com.vanguard.grc.ui.theme.RiskLow

@Composable
fun ProgressScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val competency by vm.competency.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val minutesToday by vm.minutesToday.collectAsStateWithLifecycle()
    val minutesWeek by vm.minutesWeek.collectAsStateWithLifecycle()
    val streak by vm.streak.collectAsStateWithLifecycle()
    val completedCount by vm.completedCount.collectAsStateWithLifecycle()
    val attempts by vm.attempts.collectAsStateWithLifecycle()
    val weak by vm.weakQuestions.collectAsStateWithLifecycle()
    val content by vm.content.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()

    val completedLessons = progress.filter { it.completed }
    val totalMinutes = progress.sumOf { it.timeSpentSeconds } / 60
    val revisionList = vm.spacedRepetition.dailyRevisionList(
        vm.questionStats.value,
        limit = 12
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = Gold400) {
                Eyebrow("READINESS")
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${competency.overall} / 100 - ${competency.overallBand.label}",
                    style = MaterialTheme.typography.displaySmall,
                    color = Gold400
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = vm.competencyEngine.readinessStatement(competency),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                ProgressLine(progress = competency.overall / 100f, color = Gold400)
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Confidence band ${competency.coverage}% coverage - competency is derived from demonstrated assessment performance and practical work, not from time spent.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("STREAK", "$streak d", accent = Gold400, caption = "Consecutive days", modifier = Modifier.weight(1f))
                StatTile("TODAY", "$minutesToday m", accent = MaterialTheme.colorScheme.primary, caption = "of ${settings.dailyGoalMinutes} min", modifier = Modifier.weight(1f))
                StatTile("WEEK", "$minutesWeek m", accent = MaterialTheme.colorScheme.primary, caption = "This week", modifier = Modifier.weight(1f))
            }
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatTile("LESSONS", "$completedCount", caption = "of ${content.lessons.size} complete", modifier = Modifier.weight(1f))
                StatTile("ATTEMPTS", "${attempts.size}", caption = "Assessments taken", modifier = Modifier.weight(1f))
                StatTile("TIME", "$totalMinutes m", caption = "Total study time", modifier = Modifier.weight(1f))
            }
        }

        item {
            SectionHeader(
                title = "Competency by domain",
                subtitle = "Weighted by difficulty, recency and breadth of coverage"
            )
        }
        item {
            VanguardCard {
                competency.domains.forEach { domain ->
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
                                text = "${domain.band.label} - ${domain.questionsAnswered} answered - ${domain.accuracy}% accuracy - confidence ${domain.confidence}%",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Box(Modifier.fillMaxWidth()) {
                                ProgressLine(
                                    progress = domain.score / 100f,
                                    color = when {
                                        domain.score >= 70 -> RiskLow
                                        domain.score >= 45 -> Gold400
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "${domain.score}",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        item {
            SectionHeader("Strongest and weakest areas")
            VanguardCard {
                val strongest = competency.strongest.take(3)
                val weakest = competency.weakest.take(3)
                if (strongest.isNotEmpty()) {
                    Eyebrow("Strongest")
                    Spacer(Modifier.height(6.dp))
                    strongest.forEach {
                        Text(
                            text = "- ${it.domain.title} (${it.score})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RiskLow
                        )
                    }
                }
                if (weakest.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Eyebrow("Weakest", color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(6.dp))
                    weakest.forEach {
                        Text(
                            text = "- ${it.domain.title} (${it.score})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                if (strongest.isEmpty() && weakest.isEmpty()) {
                    Text(
                        text = "Answer at least five items in a domain for it to be classified as strong or weak.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Today's revision list",
                subtitle = "Spaced repetition, driven by items you answered incorrectly"
            )
            VanguardCard {
                if (revisionList.isEmpty()) {
                    Text(
                        text = "Nothing due for revision. Items enter the list once you have answered them and they are scheduled for reinforcement.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    revisionList.forEach { stat ->
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
                                    text = "${stat.wrongCount} wrong of ${stat.seenCount} - next interval ${stat.intervalDays} days",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            ChipPill(stat.domain.replace('_', ' '), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    ChipPill(
                        text = "Start revision",
                        color = MaterialTheme.colorScheme.secondary,
                        filled = true,
                        onClick = { navController.navigate(Routes.PRACTICE_LAB) }
                    )
                }
            }
        }

        if (completedLessons.isNotEmpty()) {
            item { SectionHeader("Recently completed") }
            items(completedLessons.sortedByDescending { it.completedAt ?: 0L }.take(10)) { entry ->
                VanguardCard(onClick = { navController.navigate(Routes.lesson(entry.contentId)) }) {
                    Text(
                        text = entry.title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${entry.timeSpentSeconds / 60} minutes logged" +
                            (entry.bestScore?.let { " - best assessment $it%" } ?: ""),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            InfoCallout(
                text = "A readiness estimate is a self-assessment aid. It is not a professional qualification, and it is not a statement that an organisation is compliant. Confidence falls as evidence thins, which is why the platform shows the confidence band alongside every score.",
                kind = CalloutKind.INFO
            )
        }
    }
}
