package com.vanguard.grc.ui.screens.glossary

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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SearchField
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun GlossaryScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    val terms = content.glossary.filter {
        query.isBlank() || it.term.contains(query, ignoreCase = true) ||
            it.definition.contains(query, ignoreCase = true) ||
            it.aliases.any { alias -> alias.contains(query, ignoreCase = true) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            SearchField(
                value = query,
                onValueChange = { query = it },
                placeholder = "Search ${content.glossary.size} terms"
            )
        }
        item { SectionHeader(title = "${terms.size} terms") }
        items(terms) { term ->
            VanguardCard(onClick = { navController.navigate(Routes.glossaryTerm(term.id)) }) {
                Text(
                    text = term.term,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = term.definition,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3
                )
                if (term.framework.isNotBlank()) {
                    Spacer(Modifier.height(6.dp))
                    ChipPill(term.framework, color = MaterialTheme.colorScheme.secondary)
                }
            }
        }
    }
}

@Composable
fun GlossaryTermScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    termId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val term = content.glossaryById[termId]

    if (term == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { InfoCallout("Term not found.", kind = CalloutKind.WARNING) }
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
                    text = term.term,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (term.aliases.isNotEmpty()) {
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text = "Also written as: ${term.aliases.joinToString(", ")}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (term.framework.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    ChipPill(term.framework, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            SectionHeader("Definition")
            VanguardCard {
                Text(
                    text = term.definition,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (term.note.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    InfoCallout(term.note, kind = CalloutKind.INFO)
                }
            }
        }

        if (term.relatedIds.isNotEmpty()) {
            item {
                SectionHeader("Related terms")
                VanguardCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        term.relatedIds.take(8).forEach { id ->
                            val related = content.glossaryById[id]
                            ChipPill(
                                text = related?.term ?: id,
                                color = MaterialTheme.colorScheme.primary,
                                onClick = { related?.let { navController.navigate(Routes.glossaryTerm(it.id)) } }
                            )
                        }
                    }
                }
            }
        }
    }
}
