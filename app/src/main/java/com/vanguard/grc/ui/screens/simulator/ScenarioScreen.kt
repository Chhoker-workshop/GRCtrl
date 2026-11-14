package com.vanguard.grc.ui.screens.simulator

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
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lightbulb
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.DifficultyChip
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import kotlinx.coroutines.launch

@Composable
fun ScenarioScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    scenarioId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val scenario = content.scenariosById[scenarioId]
    val scope = rememberCoroutineScope()

    var selected by remember(scenarioId) { mutableIntStateOf(-1) }
    var submitted by remember(scenarioId) { mutableStateOf(false) }

    if (scenario == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { InfoCallout("Scenario not found.", kind = CalloutKind.WARNING) }
        }
        return
    }

    val chosen = if (selected >= 0) scenario.options.getOrNull(selected) else null

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip(scenario.difficulty)
                    ChipPill(scenario.category, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = scenario.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SectionHeader("Business context")
            VanguardCard {
                Text(
                    text = scenario.context,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SectionHeader("The situation")
            VanguardCard {
                Text(
                    text = scenario.situation,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (scenario.informationAvailable.isNotEmpty()) {
            item {
                SectionHeader("Information available to you")
                VanguardCard { BulletList(scenario.informationAvailable) }
            }
        }

        item {
            SectionHeader("Your decision")
            VanguardCard {
                scenario.options.forEachIndexed { index, option ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selected == index,
                                onClick = {
                                    selected = index
                                    submitted = false
                                }
                            )
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        RadioButton(selected = selected == index, onClick = { selected = index; submitted = false })
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = option.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        submitted = true
                        scope.launch {
                            vm.toolkit.recordDecision(
                                scenarioId = scenario.id,
                                difficulty = scenario.difficulty.name,
                                optionIndex = selected,
                                score = chosen?.score ?: 0,
                                maxScore = 100,
                                rationale = chosen?.text.orEmpty()
                            )
                        }
                    },
                    enabled = selected >= 0,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) { Text("Commit decision") }
            }
        }

        if (submitted && chosen != null) {
            item {
                SectionHeader("Management feedback")
                VanguardCard(
                    accent = if (chosen.score >= 80) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                ) {
                    Eyebrow(
                        if (chosen.score >= 80) "STRONG MANAGEMENT APPROACH" else "WEAKER MANAGEMENT APPROACH"
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Score: ${chosen.score} / 100",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = chosen.consequence,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (chosen.whyWeaker.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Outlined.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "What the stronger approach would have done",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = chosen.whyWeaker,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionHeader("What good looks like")
                VanguardCard {
                    Text(
                        text = scenario.bestPractice,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (scenario.managementGuidance.isNotBlank()) {
                        Spacer(Modifier.height(12.dp))
                        Eyebrow("Management guidance")
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = scenario.managementGuidance,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (scenario.stakeholderViews.isNotEmpty()) {
                item {
                    SectionHeader("Stakeholder positions")
                    VanguardCard {
                        scenario.stakeholderViews.forEach { (party, view) ->
                            Text(
                                text = party,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = view,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                }
            }

            item {
                SectionHeader("Competencies exercised")
                VanguardCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        scenario.competencies.forEach { c ->
                            ChipPill(c.replace('_', ' '), color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}
