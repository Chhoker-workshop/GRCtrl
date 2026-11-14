package com.vanguard.grc.ui.screens.assessment

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
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.data.content.Question
import com.vanguard.grc.data.content.QuestionType
import com.vanguard.grc.domain.assessment.Answer
import com.vanguard.grc.domain.assessment.AssessmentCatalog
import com.vanguard.grc.domain.assessment.GradedAnswer
import com.vanguard.grc.domain.assessment.ScoreResult
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.DifficultyChip
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun AssessmentRunnerScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    assessmentId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val weak by vm.weakQuestions.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val definition = AssessmentCatalog.byId(assessmentId)
    val pool = remember(assessmentId, content.questions.size) {
        val base = definition?.poolSelector?.invoke(content.questions).orEmpty()
        if (assessmentId == "practice_weak" && weak.isNotEmpty()) {
            val weakIds = weak.map { it.questionId }.toSet()
            val weakQuestions = base.filter { it.id in weakIds }
            if (weakQuestions.size >= 5) weakQuestions else base
        } else {
            base
        }
    }

    var questions by remember { mutableStateOf<List<Question>>(emptyList()) }
    var currentIndex by remember { mutableIntStateOf(0) }
    var answers by remember { mutableStateOf<Map<String, Answer>>(emptyMap()) }
    var result by remember { mutableStateOf<ScoreResult?>(null) }
    var graded by remember { mutableStateOf<List<GradedAnswer>?>(null) }
    var attemptId by remember { mutableStateOf<Long?>(null) }
    var startedAt by remember { mutableStateOf(System.currentTimeMillis()) }
    var numericInput by remember { mutableStateOf("") }
    var shortInput by remember { mutableStateOf("") }
    var matchSelection by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(assessmentId, pool.size) {
        if (questions.isEmpty() && pool.isNotEmpty()) {
            questions = vm.assessmentEngine.buildExam(
                pool = pool,
                count = minOf(definition?.questionCount ?: 20, pool.size),
                seed = System.nanoTime()
            )
            startedAt = System.currentTimeMillis()
            scope.launch {
                attemptId = vm.assessments.startAttempt(
                    assessmentId = assessmentId,
                    title = definition?.title ?: assessmentId,
                    kind = definition?.kind ?: "PRACTICE",
                    domain = definition?.domain ?: "GENERAL",
                    passMark = definition?.passMark ?: 0
                )
            }
        }
    }

    if (definition == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { InfoCallout("Assessment not found.", kind = CalloutKind.WARNING) }
        }
        return
    }

    if (questions.isEmpty()) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                InfoCallout(
                    "No questions are currently available for this assessment pool. Add items to the bank or choose another assessment.",
                    kind = CalloutKind.WARNING
                )
            }
        }
        return
    }

    // ---------------------------------------------------------------- results view
    val scoreResult = result
    if (scoreResult != null && graded != null) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                VanguardCard(accent = if (scoreResult.passed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error) {
                    Eyebrow(if (scoreResult.passed) "PASSED" else "NOT YET AT STANDARD")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "${scoreResult.scorePercent}%",
                        style = MaterialTheme.typography.displaySmall,
                        color = if (scoreResult.passed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${scoreResult.correctCount} of ${scoreResult.totalCount} correct - pass mark ${definition.passMark}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (scoreResult.domainBreakdown.isNotEmpty()) {
                item {
                    SectionHeader("Performance by domain")
                    VanguardCard {
                        scoreResult.domainBreakdown.values.forEach { breakdown ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        text = breakdown.domain.replace('_', ' '),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "${breakdown.correct}/${breakdown.total} correct",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "${breakdown.percent}%",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            item { SectionHeader("Item review", subtitle = "The explanation and why each wrong option is weaker") }

            items(graded!!) { gradedAnswer ->
                val selected = answers[gradedAnswer.question.id]?.selectedOptionIds.orEmpty()
                VanguardCard(accent = if (gradedAnswer.correct) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (gradedAnswer.correct) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (gradedAnswer.correct) "Correct" else "Review required",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (gradedAnswer.correct) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                        Spacer(Modifier.weight(1f))
                        DifficultyChip(gradedAnswer.question.difficulty)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = gradedAnswer.question.stem,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (gradedAnswer.question.options.isNotEmpty()) {
                        Spacer(Modifier.height(10.dp))
                        gradedAnswer.question.options.forEach { option ->
                            val isCorrect = option.id in gradedAnswer.question.correctOptionIds
                            val wasSelected = option.id in selected
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = if (isCorrect) "[CORRECT] " else if (wasSelected) "[YOUR ANSWER] " else "[   ] ",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = when {
                                        isCorrect -> MaterialTheme.colorScheme.secondary
                                        wasSelected -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                                    }
                                )
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = gradedAnswer.feedback,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (gradedAnswer.question.frameworkRef.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        ChipPill(gradedAnswer.question.frameworkRef, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            item {
                OutlinedButton(
                    onClick = {
                        navController.popBackStack()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Back to assessments") }
            }
        }
        return
    }

    // ----------------------------------------------------------------- runner view
    val question = questions[currentIndex]
    val answer = answers[question.id]

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Eyebrow("${definition.kind.replace('_', ' ')}")
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = definition.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    ChipPill(
                        text = "${currentIndex + 1} / ${questions.size}",
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(10.dp))
                androidx.compose.material3.LinearProgressIndicator(
                    progress = { (currentIndex + 1f) / questions.size },
                    modifier = Modifier.fillMaxWidth().height(5.dp)
                )
            }
        }

        item {
            VanguardCard(accent = MaterialTheme.colorScheme.primary) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip(question.difficulty)
                    ChipPill(question.domain.replace('_', ' '), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChipPill(
                        text = question.type.name.replace('_', ' '),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = question.stem,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        item {
            when (question.type) {
                QuestionType.RISK_CALCULATION -> {
                    VanguardCard {
                        Eyebrow("Your calculation")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = numericInput,
                            onValueChange = { numericInput = it.filter { c -> c.isDigit() || c == '.' || c == '-' } },
                            label = { Text("Enter your numeric answer") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (question.numericAnswer?.formula?.isNotBlank() == true) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Method: ${question.numericAnswer?.formula}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(10.dp))
                        ChipPill(
                            text = "Use this answer",
                            color = MaterialTheme.colorScheme.secondary,
                            filled = true,
                            onClick = {
                                val numeric = numericInput.toDoubleOrNull()
                                answers = answers.toMutableMap().apply {
                                    put(
                                        question.id,
                                        Answer(question.id, numericResponse = numeric, timeSpentSeconds = 0)
                                    )
                                }
                            }
                        )
                    }
                }

                QuestionType.MATCH_CONTROL -> {
                    VanguardCard {
                        Eyebrow("Match each item to its correct answer")
                        Spacer(Modifier.height(10.dp))
                        question.matchPairs.forEach { pair ->
                            Column(Modifier.padding(vertical = 6.dp)) {
                                Text(
                                    text = pair.left,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    question.matchPairs.map { it.right }.distinct().take(6).forEach { right ->
                                        ChipPill(
                                            text = right.take(28),
                                            color = if (matchSelection[pair.leftId] == right) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            filled = matchSelection[pair.leftId] == right,
                                            onClick = {
                                                matchSelection = matchSelection.toMutableMap().apply { put(pair.leftId, right) }
                                                answers = answers.toMutableMap().apply {
                                                    put(question.id, Answer(question.id, matchMapping = matchSelection))
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                QuestionType.SHORT_ANSWER, QuestionType.PRACTICAL_ASSIGNMENT -> {
                    VanguardCard {
                        Eyebrow("Your response")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = shortInput,
                            onValueChange = { shortInput = it },
                            label = { Text("Write your answer") },
                            minLines = 5,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(10.dp))
                        ChipPill(
                            text = "Use this answer",
                            color = MaterialTheme.colorScheme.secondary,
                            filled = true,
                            onClick = {
                                answers = answers.toMutableMap().apply {
                                    put(question.id, Answer(question.id, freeText = shortInput))
                                }
                            }
                        )
                    }
                }

                else -> {
                    val multiSelect = question.type == QuestionType.MULTIPLE_SELECT
                    VanguardCard {
                        question.options.forEach { option ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .selectable(
                                        selected = option.id in (answer?.selectedOptionIds.orEmpty()),
                                        onClick = {
                                            val current = answer?.selectedOptionIds.orEmpty().toMutableList()
                                            if (multiSelect) {
                                                if (current.contains(option.id)) current.remove(option.id)
                                                else current.add(option.id)
                                            } else {
                                                current.clear()
                                                current.add(option.id)
                                            }
                                            answers = answers.toMutableMap().apply {
                                                put(question.id, Answer(question.id, selectedOptionIds = current))
                                            }
                                        }
                                    )
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                if (multiSelect) {
                                    Checkbox(
                                        checked = option.id in (answer?.selectedOptionIds.orEmpty()),
                                        onCheckedChange = {
                                            val current = answer?.selectedOptionIds.orEmpty().toMutableList()
                                            if (it) current.add(option.id) else current.remove(option.id)
                                            answers = answers.toMutableMap().apply {
                                                put(question.id, Answer(question.id, selectedOptionIds = current))
                                            }
                                        }
                                    )
                                } else {
                                    RadioButton(
                                        selected = option.id in (answer?.selectedOptionIds.orEmpty()),
                                        onClick = {
                                            answers = answers.toMutableMap().apply {
                                                put(question.id, Answer(question.id, selectedOptionIds = listOf(option.id)))
                                            }
                                        }
                                    )
                                }
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = option.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                        if (multiSelect) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Select every option that applies. More than one answer may be correct.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (currentIndex > 0) {
                    OutlinedButton(
                        onClick = { currentIndex-- },
                        modifier = Modifier.weight(1f)
                    ) { Text("Previous") }
                }
                if (currentIndex < questions.lastIndex) {
                    Button(
                        onClick = { currentIndex++ },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) { Text("Next") }
                } else {
                    Button(
                        onClick = {
                            scope.launch {
                                val id = attemptId
                                if (id != null) {
                                    val scored = vm.assessments.finaliseAttempt(
                                        attemptId = id,
                                        questions = questions,
                                        answers = answers.values.toList(),
                                        engine = vm.assessmentEngine,
                                        timeSpentSeconds = ((System.currentTimeMillis() - startedAt) / 1000).toInt(),
                                        passMark = definition.passMark
                                    )
                                    result = scored
                                    graded = scored.graded
                                    vm.assessments.refreshCompetencyCache(vm.competencyEngine)
                                    vm.learning.logStudySession(20, null, "ASSESSMENT")
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) { Text("Submit") }
                }
            }
        }

        item {
            if (question.frameworkRef.isNotBlank() || question.revisionRef.isNotBlank()) {
                VanguardCard {
                    Eyebrow("Reference")
                    Spacer(Modifier.height(6.dp))
                    if (question.frameworkRef.isNotBlank()) {
                        Text(
                            text = question.frameworkRef,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (question.revisionRef.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Revise: ${question.revisionRef}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
