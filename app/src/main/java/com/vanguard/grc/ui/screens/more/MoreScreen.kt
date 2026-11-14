package com.vanguard.grc.ui.screens.more

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import com.vanguard.grc.ui.navigation.SECONDARY_DESTINATIONS
import com.vanguard.grc.ui.navigation.NavGroup

@Composable
fun MoreScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val competency by vm.competency.collectAsStateWithLifecycle()

    val grouped = SECONDARY_DESTINATIONS.groupBy { it.group }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.primary) {
                Eyebrow("GRC VANGUARD")
                androidx.compose.foundation.layout.Spacer(Modifier.size(8.dp))
                Text(
                    text = "Every part of the platform",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                Text(
                    text = "${content.modules.size} modules - ${content.lessons.size} lessons - ${content.questions.size} assessment items - ${content.controls.size} Annex A controls - ${content.templates.size} templates - ${content.scenarios.size} simulator scenarios - ${content.projects.size} capstones",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        NavGroup.entries.forEach { group ->
            val destinations = grouped[group].orEmpty()
            if (destinations.isEmpty()) return@forEach
            item { SectionHeader(title = group.label) }
            items(destinations) { destination ->
                VanguardCard(
                    onClick = { navController.navigate(destination.route) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.foundation.layout.Row(
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Icon(
                            destination.icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(Modifier.size(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = destination.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = destination.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(title = "Current standing")
            VanguardCard {
                Text(
                    text = "Competency ${competency.overall} / 100 (${competency.overallBand.label}) - ${competency.totalQuestionsAnswered} assessment items answered across ${competency.domains.count { it.questionsAnswered > 0 }} of 10 competency domains.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
