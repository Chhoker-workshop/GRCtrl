package com.vanguard.grc.ui.screens.paths

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.ProgressLine
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.StatTile
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun PathsScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val progress by vm.progress.collectAsStateWithLifecycle()
    val completed = progress.filter { it.completed }.map { it.contentId }.toSet()

    val acceleratorLessons = content.accelerator.flatMap { it.lessonIds }.distinct()
    val acceleratorDone = acceleratorLessons.count { it in completed }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("PRIMARY PATH")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "60-Day GRC Accelerator",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "An intensive programme at roughly two hours a day, structured across eight weeks and a four-day capstone. Designed to take a learner from zero GRC knowledge to defensible enterprise-level judgement.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "DAYS",
                        value = "60",
                        caption = "120 min per day",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "TOTAL",
                        value = "${content.accelerator.sumOf { it.totalMinutes } / 60} h",
                        caption = "Scheduled contact time",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "DONE",
                        value = "$acceleratorDone",
                        caption = "of ${acceleratorLessons.size} lessons",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(14.dp))
                ProgressLine(
                    progress = if (acceleratorLessons.isEmpty()) 0f else acceleratorDone.toFloat() / acceleratorLessons.size,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipPill(
                        text = "Open the programme",
                        color = MaterialTheme.colorScheme.secondary,
                        filled = true,
                        onClick = { navController.navigate(Routes.ACCELERATOR) }
                    )
                    ChipPill(
                        text = "Start day 1",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate(Routes.acceleratorDay(1)) }
                    )
                }
            }
        }

        item {
            InfoCalloutBox(
                "This programme is a structured learning path, not a professional qualification. Completing it does not make you a certified GRC professional, and no regulator or employer treats it as equivalent to recognised certification."
            )
        }

        if (content.paths.isNotEmpty()) {
            item { SectionHeader(title = "Extended professional tracks") }
            items(content.paths) { path ->
                val pathLessons = content.modules
                    .filter { it.id in path.moduleIds }
                    .flatMap { content.lessons.filter { l -> l.moduleId == it.id } }
                val done = pathLessons.count { it.id in completed }
                VanguardCard(onClick = {
                    navController.navigate(Routes.module(path.moduleIds.firstOrNull().orEmpty()))
                }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Eyebrow(path.kind.uppercase())
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = path.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = path.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (pathLessons.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        ProgressLine(
                            progress = done.toFloat() / pathLessons.size,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$done of ${pathLessons.size} lessons - ${path.estimatedHours} estimated hours",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (path.outcomes.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Eyebrow("Outcomes")
                        Spacer(Modifier.height(6.dp))
                        BulletList(path.outcomes.take(4))
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoCalloutBox(text: String) {
    com.vanguard.grc.ui.components.InfoCallout(
        text = text,
        kind = com.vanguard.grc.ui.components.CalloutKind.WARNING
    )
}
