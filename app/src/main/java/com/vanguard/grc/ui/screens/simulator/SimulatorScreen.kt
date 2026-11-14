package com.vanguard.grc.ui.screens.simulator

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
import androidx.compose.material.icons.outlined.Business
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
import com.vanguard.grc.ui.components.DifficultyChip
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.StatTile
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun SimulatorScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val decisions by vm.simulatorDecisions.collectAsStateWithLifecycle()

    val scenarios = content.scenarios
    val byDifficulty = scenarios.groupBy { it.difficulty }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("ENTERPRISE SIMULATOR")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Vanguard Financial Services Ltd.",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "5,000 employees across multiple offices, cloud infrastructure, customer financial data, internet banking, third-party providers, an internal IT operation, a security operations centre and multiple regulatory obligations.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile(
                        label = "SCENARIOS",
                        value = "${scenarios.size}",
                        caption = "Management situations",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "DECIDED",
                        value = "${decisions.size}",
                        caption = "Your decisions logged",
                        modifier = Modifier.weight(1f)
                    )
                    StatTile(
                        label = "YOUR ROLE",
                        value = "GRC",
                        caption = "GRC Manager, reporting to the CISO",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            InfoCallout(
                text = "Vanguard Financial Services is a fictional organisation built for training. The situations are realistic but the company, its people and its numbers are invented.",
                kind = com.vanguard.grc.ui.components.CalloutKind.INFO
            )
        }

        byDifficulty.forEach { (difficulty, items) ->
            item {
                SectionHeader(
                    title = "${difficulty.name.lowercase().replaceFirstChar { it.uppercase() }} scenarios",
                    subtitle = when (difficulty) {
                        com.vanguard.grc.data.content.Difficulty.BEGINNER -> "Straightforward judgement calls"
                        com.vanguard.grc.data.content.Difficulty.INTERMEDIATE -> "Conflicting priorities and incomplete information"
                        else -> "Multi-stakeholder, high-consequence decisions"
                    }
                )
            }
            items(items) { scenario ->
                VanguardCard(onClick = { navController.navigate(Routes.scenario(scenario.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Business,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = scenario.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = scenario.situation,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        DifficultyChip(scenario.difficulty)
                        ChipPill(scenario.category, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        ChipPill("${scenario.options.size} options", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
