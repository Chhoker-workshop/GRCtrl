package com.vanguard.grc.ui.screens.explorer

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
import androidx.compose.material.icons.outlined.AccountTree
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
import com.vanguard.grc.data.content.FrameworkKind
import com.vanguard.grc.data.content.FrameworkStatus
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun ExplorerScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val frameworks = content.frameworks
    val byKind = frameworks.groupBy { it.kind }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("FRAMEWORK EXPLORER")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Reference registry and cross-framework mapping",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${frameworks.size} frameworks, standards, laws and regulatory instruments with publisher, edition, publication date, official source and last verification date. ${content.mappings.size} mapping pairs show where objectives align and where they do not.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            InfoCallout(
                text = "A mapping shows that two frameworks address a similar objective. It never establishes compliance with either, and it is not a certification route. Use it to plan evidence reuse and to find the gaps a second framework would add.",
                kind = com.vanguard.grc.ui.components.CalloutKind.WARNING
            )
        }

        item {
            SectionHeader(
                title = "Cross-framework mappings",
                subtitle = "Common objectives, scope differences and evidence reuse"
            )
        }
        items(content.mappings) { pair ->
            VanguardCard(onClick = { navController.navigate(Routes.mapping(pair.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.AccountTree,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = pair.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = pair.summary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipPill(pair.frameworkA, color = MaterialTheme.colorScheme.primary)
                    ChipPill(pair.frameworkB, color = MaterialTheme.colorScheme.primary)
                    ChipPill("${pair.entries.size} mappings", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        FrameworkKind.entries.forEach { kind ->
            val items = byKind[kind].orEmpty()
            if (items.isEmpty()) return@forEach
            item {
                SectionHeader(
                    title = when (kind) {
                        FrameworkKind.LAW -> "Laws"
                        FrameworkKind.REGULATION -> "Regulations and directions"
                        FrameworkKind.STANDARD -> "Standards"
                        FrameworkKind.FRAMEWORK -> "Frameworks"
                        FrameworkKind.CODE_OF_PRACTICE -> "Codes of practice"
                        FrameworkKind.CERTIFICATION -> "Certifications and schemes"
                        FrameworkKind.CONTRACTUAL_REQUIREMENT -> "Contractual requirements"
                        FrameworkKind.GUIDANCE -> "Official guidance"
                    }
                )
            }
            items(items) { framework ->
                VanguardCard {
                    Row(verticalAlignment = Alignment.Top) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = framework.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = framework.publisher +
                                    (if (framework.edition.isNotBlank()) " - ${framework.edition}" else ""),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = framework.applicability,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChipPill(
                            text = framework.status.name,
                            color = when (framework.status) {
                                FrameworkStatus.ACTIVE -> MaterialTheme.colorScheme.secondary
                                FrameworkStatus.SUPERSEDED -> MaterialTheme.colorScheme.onSurfaceVariant
                                else -> MaterialTheme.colorScheme.error
                            }
                        )
                        if (framework.publicationDate.isNotBlank()) {
                            ChipPill(framework.publicationDate, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ChipPill("verified ${framework.lastVerified}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
