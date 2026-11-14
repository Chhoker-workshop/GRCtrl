package com.vanguard.grc.ui.screens.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.data.content.SearchKind
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.EmptyState
import com.vanguard.grc.ui.components.SearchField
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard

@Composable
fun SearchScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val query by vm.searchQuery.collectAsStateWithLifecycle()
    val kind by vm.searchKind.collectAsStateWithLifecycle()
    val results by vm.searchResults.collectAsStateWithLifecycle()
    val content by vm.content.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.prepareSearch() }

    val suggestions = listOf(
        "risk appetite", "statement of applicability", "three lines model",
        "incident response", "vendor due diligence", "DPIA", "SOC 2",
        "PCI DSS", "RBI", "NIST CSF", "risk register", "internal audit",
        "zero trust", "board reporting", "AI governance"
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SearchField(
                value = query,
                onValueChange = vm::setSearchQuery,
                placeholder = "Search lessons, controls, questions, regulations, templates..."
            )
        }

        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    ChipPill(
                        text = "All",
                        color = if (kind == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        filled = kind == null,
                        onClick = { vm.setSearchKind(null) }
                    )
                }
                items(SearchKind.entries.toList()) { k ->
                    ChipPill(
                        text = k.label,
                        color = if (kind == k) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        filled = kind == k,
                        onClick = { vm.setSearchKind(if (kind == k) null else k) }
                    )
                }
            }
        }

        if (query.isBlank()) {
            item { SectionHeader(title = "Suggested searches") }
            item {
                VanguardCard {
                    suggestions.chunked(2).forEach { pair ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { term ->
                                ChipPill(
                                    text = term,
                                    color = MaterialTheme.colorScheme.secondary,
                                    onClick = { vm.setSearchQuery(term) }
                                )
                            }
                            if (pair.size == 1) Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
            item {
                SectionHeader(title = "Search coverage")
                VanguardCard {
                    Text(
                        text = "${content.lessons.size} lessons, ${content.controls.size} Annex A controls, ${content.questions.size} assessment items, ${content.frameworks.size} frameworks and standards, ${content.glossary.size} glossary terms, ${content.templates.size} templates and ${content.scenarios.size} simulator scenarios are indexed for on-device search. Search runs entirely offline.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (results.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Outlined.Search,
                    title = "No matches",
                    message = "Try a broader term such as 'risk', 'audit', 'privacy' or 'ISO 27001'. Search covers lessons, controls, questions, frameworks, templates and scenarios."
                )
            }
        } else {
            item {
                SectionHeader(title = "${results.size} results for \"$query\"")
            }
            items(results) { result ->
                VanguardCard(onClick = { navController.navigate(result.route) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = result.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                text = result.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.size(8.dp))
                        ChipPill(text = result.kind.label, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
    }
}
