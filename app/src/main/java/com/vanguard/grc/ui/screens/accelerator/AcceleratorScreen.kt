package com.vanguard.grc.ui.screens.accelerator

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
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.ProgressLine
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun AcceleratorScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val days by vm.acceleratorDays.collectAsStateWithLifecycle()
    val completedDays = days.filter { it.completedAt != null }.map { it.day }.toSet()

    val weeks = (1..8).map { week ->
        val start = (week - 1) * 7 + 1
        val end = (week * 7).coerceAtMost(56)
        week to content.accelerator.filter { it.day in start..end }
    }
    val capstone = content.accelerator.filter { it.day > 56 }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("60-DAY ACCELERATOR")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "120 minutes a day, every day",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "Each day is built to fill a full two-hour session: 35 minutes of core concept, 25 minutes of framework and standard study, 30 minutes of practical enterprise exercise, 20 minutes of assessment and scenario work, and 10 minutes of revision and notes.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                ProgressLine(
                    progress = completedDays.size / 60f,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${completedDays.size} of 60 days complete",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        weeks.forEach { (week, weekDays) ->
            if (weekDays.isEmpty()) return@forEach
            item {
                SectionHeader(
                    title = "Week $week",
                    subtitle = "Days ${weekDays.first().day} to ${weekDays.last().day}"
                )
            }
            items(weekDays) { day ->
                DayRow(
                    day = day.day,
                    title = day.title,
                    focus = day.focus,
                    minutes = day.totalMinutes,
                    completed = day.day in completedDays,
                    onClick = { navController.navigate(Routes.acceleratorDay(day.day)) }
                )
            }
        }

        if (capstone.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Final four days - capstone",
                    subtitle = "Days 57 to 60: you act as the Enterprise GRC Lead"
                )
            }
            items(capstone) { day ->
                DayRow(
                    day = day.day,
                    title = day.title,
                    focus = day.focus,
                    minutes = day.totalMinutes,
                    completed = day.day in completedDays,
                    onClick = { navController.navigate(Routes.acceleratorDay(day.day)) }
                )
            }
        }
    }
}

@Composable
private fun DayRow(
    day: Int,
    title: String,
    focus: String,
    minutes: Int,
    completed: Boolean,
    onClick: () -> Unit
) {
    VanguardCard(onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (completed) Icons.Outlined.CheckCircle else Icons.Outlined.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (completed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Eyebrow("DAY $day")
                    Spacer(Modifier.width(8.dp))
                    ChipPill(
                        text = "$minutes min",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (focus.isNotBlank()) {
                    Text(
                        text = focus,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
            }
        }
    }
}
