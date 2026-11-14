package com.vanguard.grc.ui.screens.mentor

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Send
import androidx.compose.material.icons.outlined.SmartToy
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.domain.mentor.MentorAnswer
import com.vanguard.grc.domain.mentor.MentorEngine
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.theme.Gold400

private data class ChatTurn(
    val prompt: String,
    val answer: MentorAnswer
)

@Composable
fun MentorScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val settings by vm.settings.collectAsStateWithLifecycle()
    val turns = remember { mutableStateListOf<ChatTurn>() }
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val engine = remember(content.lessons.size, content.controls.size) {
        MentorEngine(content)
    }
    val starters = remember { engine.starterQuestions() }

    LaunchedEffect(turns.size) {
        if (turns.isNotEmpty()) {
            listState.animateScrollToItem(turns.size + 2)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        state = listState,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = Gold400) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.SmartToy,
                        contentDescription = null,
                        tint = Gold400,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Eyebrow("AI GRC MENTOR")
                        Text(
                            text = "An enterprise GRC lead in your pocket",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "The mentor answers from this application's own verified curriculum, and cites what it uses. It never invents a standard clause, never treats a generated answer as audit approval, and tells you plainly when the corpus does not contain a confident answer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    ChipPill(
                        text = when (settings.mentorProvider.name) {
                            "OFFLINE_ONLY" -> "Offline corpus"
                            "LOCAL_MODEL" -> "Local model"
                            else -> "External provider"
                        },
                        color = MaterialTheme.colorScheme.secondary
                    )
                    ChipPill("No credentials stored", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            InfoCallout(
                text = "The mentor is a learning aid. It does not provide professional advice, it cannot certify an organisation, and it must not be presented to an auditor as evidence of conformity. Verify every standard and regulatory point against the current published text.",
                kind = CalloutKind.WARNING
            )
        }

        if (turns.isEmpty()) {
            item { SectionHeader(title = "What would you like to understand?") }
            item {
                VanguardCard {
                    starters.forEach { starter ->
                        Box(Modifier.padding(vertical = 3.dp)) {
                            Text(
                                text = starter,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .clickable { input = starter }
                                    .padding(10.dp)
                            )
                        }
                    }
                }
            }
        }

        items(turns) { turn ->
            Column {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.14f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = turn.prompt,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(Modifier.height(8.dp))
                VanguardCard(accent = Gold400) {
                    Text(
                        text = turn.answer.headline,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = turn.answer.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (turn.answer.suggestedActions.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Eyebrow("What to do next")
                        Spacer(Modifier.height(6.dp))
                        turn.answer.suggestedActions.forEach { action ->
                            Text(
                                text = "- $action",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                        }
                    }
                    if (turn.answer.citations.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Eyebrow("Sources used")
                        Spacer(Modifier.height(6.dp))
                        turn.answer.citations.forEach { citation ->
                            Text(
                                text = "- ${citation.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { navController.navigate(citation.route) }
                                    .padding(vertical = 3.dp)
                            )
                        }
                    }
                    if (turn.answer.caveats.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        InfoCallout(turn.answer.caveats.joinToString("\n"), kind = CalloutKind.INFO)
                    }
                }
            }
        }

        item {
            VanguardCard {
                OutlinedTextField(
                    value = input,
                    onValueChange = { input = it },
                    label = { Text("Ask about any GRC concept, control or decision") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        val question = input.trim()
                        if (question.isNotEmpty()) {
                            turns.add(ChatTurn(question, engine.ask(question)))
                            input = ""
                        }
                    },
                    enabled = input.isNotBlank(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Gold400)
                ) {
                    Icon(Icons.Outlined.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Ask the mentor")
                }
            }
        }
    }
}
