package com.vanguard.grc.ui.screens.toolkit

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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Build
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
import com.vanguard.grc.domain.toolkit.ToolRegistry
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.EmptyState
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun ToolkitScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val records by vm.toolRecords.collectAsStateWithLifecycle()
    val tools = ToolRegistry.all()
    val grouped = tools.groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("PRACTICAL TOOLKIT")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Thirty working instruments",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Each tool is a real working instrument with meaningful fields, guidance on how to use it, a realistic sample record and export to CSV or PDF. Everything works offline and everything is stored locally on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipPill("${tools.size} tools", color = MaterialTheme.colorScheme.secondary)
                    ChipPill(
                        "${records.count { !it.isSample }} saved records",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item {
            InfoCallout(
                text = "Sample records shipped with each tool are illustrative examples written for training. They are labelled as samples everywhere they appear so they are never mistaken for your organisation's real registers.",
                kind = com.vanguard.grc.ui.components.CalloutKind.INFO
            )
        }

        grouped.forEach { (category, categoryTools) ->
            item { SectionHeader(title = category) }
            items(categoryTools) { tool ->
                val saved = records.count { it.toolId == tool.id && !it.isSample }
                VanguardCard(onClick = { navController.navigate(Routes.tool(tool.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Build,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = tool.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = tool.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChipPill("${tool.fields.size} fields", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (saved > 0) {
                            ChipPill("$saved saved", color = MaterialTheme.colorScheme.primary)
                        }
                        tool.exportFormats.forEach { fmt ->
                            ChipPill(fmt, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }
    }
}
