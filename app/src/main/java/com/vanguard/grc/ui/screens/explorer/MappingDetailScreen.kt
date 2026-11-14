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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard

@Composable
fun MappingDetailScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    mappingId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val pair = content.mappings.firstOrNull { it.id == mappingId }

    if (pair == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { InfoCallout("Mapping not found.", kind = CalloutKind.WARNING) }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Text(
                    text = pair.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = pair.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipPill(pair.frameworkA, color = MaterialTheme.colorScheme.primary)
                    ChipPill(pair.frameworkB, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (pair.scopeDifferences.isNotBlank()) {
            item {
                SectionHeader("Scope differences")
                VanguardCard {
                    Text(
                        text = pair.scopeDifferences,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        item {
            SectionHeader(
                title = "Control-level mapping",
                subtitle = "${pair.entries.size} mapped objectives"
            )
        }

        items(pair.entries) { entry ->
            VanguardCard {
                Row {
                    Column(Modifier.weight(1f)) {
                        Eyebrow(pair.frameworkA)
                        Text(
                            text = entry.aRef,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = "maps to",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Eyebrow(pair.frameworkB)
                        Text(
                            text = entry.bRef,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = entry.commonObjective,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (entry.differences.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Eyebrow("Where they differ")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.differences,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (entry.evidenceReuse.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Eyebrow("Evidence reuse")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.evidenceReuse,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (entry.additionalRequirements.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Eyebrow("Additional requirements this framework adds")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = entry.additionalRequirements,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (entry.implementationNote.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    InfoCallout(entry.implementationNote, kind = CalloutKind.INFO)
                }
            }
        }

        if (pair.warning.isNotBlank()) {
            item {
                InfoCallout(pair.warning, kind = CalloutKind.WARNING)
            }
        }
    }
}
