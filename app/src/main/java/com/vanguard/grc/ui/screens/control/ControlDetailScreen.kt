package com.vanguard.grc.ui.screens.control

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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

/**
 * Full treatment of a single ISO/IEC 27001:2022 Annex A control.
 */
@Composable
fun ControlDetailScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    controlId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val control = content.controlsById[controlId] ?: content.controlsByControlId[controlId]

    if (control == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                InfoCallout(
                    "Control not found. The Annex A library may not be loaded yet, or this identifier is from an older content version.",
                    kind = CalloutKind.WARNING
                )
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Eyebrow("ISO/IEC 27001:2022 ANNEX A")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "${control.controlId} ${control.title}",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                ChipPill(
                    text = control.category.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = MaterialTheme.colorScheme.secondary
                )
                control.mappings.forEach { (framework, ref) ->
                    ChipPill(text = "$framework $ref", color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            SectionHeader("Control objective")
            VanguardCard {
                Text(
                    text = control.objective,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SectionHeader("Why it exists")
            VanguardCard {
                Text(
                    text = control.whyItExists,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SectionHeader("Risk addressed")
            VanguardCard {
                Text(
                    text = control.riskAddressed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SectionHeader("Implementation guidance")
            VanguardCard {
                Text(
                    text = control.implementationGuidance,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(12.dp))
                LabeledValue("Accountable department", control.responsibleDepartment)
            }
        }

        item {
            SectionHeader("Evidence examples", subtitle = "What a reviewer should be able to inspect")
            VanguardCard {
                BulletList(control.evidenceExamples)
            }
        }

        item {
            SectionHeader("Audit testing approach")
            VanguardCard {
                Text(
                    text = control.auditTesting,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            SectionHeader("Common implementation gaps", subtitle = "Where organisations most often fall short")
            VanguardCard {
                BulletList(control.commonGaps)
            }
        }

        item {
            SectionHeader("Sample interview questions")
            VanguardCard {
                control.interviewQuestions.forEach { q ->
                    Text(
                        text = q,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (control.scenario.isNotBlank()) {
            item {
                SectionHeader("Practical scenario")
                VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                    Text(
                        text = control.scenario,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (control.relatedControls.isNotEmpty()) {
            item {
                SectionHeader("Related controls")
                VanguardCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        control.relatedControls.take(6).forEach { ref ->
                            val related = content.controlsByControlId[ref]
                            ChipPill(
                                text = ref,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = {
                                    related?.let { navController.navigate(Routes.control(it.id)) }
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            InfoCallout(
                text = "This is an original explanatory summary written for education. It is not a reproduction of the ISO standard text. Confirm the exact control wording against the published standard before relying on it in an audit.",
                kind = CalloutKind.INFO
            )
        }
    }
}
