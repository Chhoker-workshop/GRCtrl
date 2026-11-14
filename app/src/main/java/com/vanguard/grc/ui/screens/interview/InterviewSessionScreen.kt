package com.vanguard.grc.ui.screens.interview

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.data.content.InterviewQuestion
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun InterviewSessionScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    trackId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val track = content.interviewsById[trackId]
    val answers = remember { mutableStateListOf<String>() }
    val revealed = remember { mutableStateListOf<Boolean>() }
    var selfScore by remember { mutableStateOf(0) }

    if (track == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { InfoCallout("Interview track not found.", kind = CalloutKind.WARNING) }
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
                Eyebrow(track.level.uppercase())
                Spacer(Modifier.height(6.dp))
                Text(
                    text = track.role,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = track.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (track.typicalEmployerFocus.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = "What the employer is really testing: ${track.typicalEmployerFocus}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        if (track.evaluationDimensions.isNotEmpty()) {
            item {
                SectionHeader("How your answers are evaluated")
                VanguardCard {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        track.evaluationDimensions.forEach {
                            ChipPill(it, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }

        if (track.competencies.isNotEmpty()) {
            item {
                SectionHeader("Competencies being assessed")
                VanguardCard { BulletList(track.competencies) }
            }
        }

        item { SectionHeader("Interview questions", subtitle = "Answer in writing, then reveal the model answer") }

        items(track.questions) { question ->
            val index = track.questions.indexOf(question)
            InterviewQuestionCard(
                question = question,
                answer = answers.getOrNull(index).orEmpty(),
                onAnswerChange = { value ->
                    while (answers.size <= index) answers.add("")
                    answers[index] = value
                },
                revealed = revealed.getOrNull(index) ?: false,
                onReveal = {
                    while (revealed.size <= index) revealed.add(false)
                    revealed[index] = true
                }
            )
        }

        item {
            SectionHeader("Career and salary negotiation")
            VanguardCard {
                track.negotiationGuidance.forEach {
                    Text(
                        text = "- $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                }
            }
        }

        item {
            InfoCallout(
                text = "Interview answers are assessed on technical accuracy, business understanding, risk-based thinking, communication, leadership judgement and practical implementation knowledge. The model answers show the reasoning and the structure - they are not scripts to memorise.",
                kind = CalloutKind.INFO
            )
        }
    }
}

@Composable
private fun InterviewQuestionCard(
    question: InterviewQuestion,
    answer: String,
    onAnswerChange: (String) -> Unit,
    revealed: Boolean,
    onReveal: () -> Unit
) {
    VanguardCard {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            ChipPill(question.type, color = MaterialTheme.colorScheme.secondary)
        }
        Spacer(Modifier.height(10.dp))
        Text(
            text = question.prompt,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = answer,
            onValueChange = onAnswerChange,
            label = { Text("Your answer") },
            minLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(10.dp))
        if (!revealed) {
            Button(
                onClick = onReveal,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) { Text("Reveal model answer") }
        } else {
            Eyebrow("Model answer")
            Spacer(Modifier.height(6.dp))
            Text(
                text = question.modelAnswer,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (question.evaluationPoints.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Eyebrow("What a strong answer demonstrates")
                Spacer(Modifier.height(6.dp))
                BulletList(question.evaluationPoints)
            }
            if (question.commonMistakes.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Eyebrow("Common mistakes interviewers hear")
                Spacer(Modifier.height(6.dp))
                question.commonMistakes.forEach {
                    Text(
                        text = "- $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}
