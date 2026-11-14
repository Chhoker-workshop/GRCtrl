package com.vanguard.grc.ui.screens.assessment

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
import androidx.compose.material.icons.outlined.AssignmentTurnedIn
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
import com.vanguard.grc.domain.assessment.AssessmentCatalog
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.ProgressLine
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AssessmentsScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val attempts by vm.attempts.collectAsStateWithLifecycle()
    val competency by vm.competency.collectAsStateWithLifecycle()

    val definitions = AssessmentCatalog.definitions
    val daily = content.accelerator.size

    val kindGroups = definitions.groupBy { it.kind }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("COMPETENCY-BASED ASSESSMENT")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Competency is earned, not purchased",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${content.questions.size} original assessment items across ${content.questions.map { it.domain }.distinct().size} competency domains. Every item carries an explanation, the reason each wrong option is weaker, and a revision pointer.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            InfoCallout(
                text = "All questions in this platform are original, written for learning. No proprietary certification examination content from ISACA, ISC2, PCI SSC, AICPA or any other body is reproduced here. Practice examinations are aligned to the published domain structure of those certifications without using their material.",
                kind = com.vanguard.grc.ui.components.CalloutKind.INFO
            )
        }

        kindGroups.forEach { (kind, items) ->
            item {
                SectionHeader(
                    title = when (kind) {
                        "DOMAIN_EXAM" -> "Domain examinations"
                        "CERTIFICATION_PRACTICE" -> "Certification-aligned practice"
                        "MOCK_EXAM" -> "Mock examinations"
                        else -> "Practice"
                    },
                    subtitle = when (kind) {
                        "DOMAIN_EXAM" -> "One full examination per competency domain"
                        "CERTIFICATION_PRACTICE" -> "Original practice content aligned to published domain structures"
                        "MOCK_EXAM" -> "Full-length, mixed-difficulty mock examinations"
                        else -> "Untimed practice sets"
                    }
                )
            }
            items(items) { definition ->
                val poolSize = definition.poolSelector(content.questions).size
                val best = attempts.filter { it.assessmentId == definition.id }
                    .maxByOrNull { it.scorePercent ?: 0 }
                VanguardCard(
                    onClick = {
                        if (poolSize > 0) navController.navigate(Routes.assessment(definition.id))
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = definition.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = definition.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ChipPill("${definition.questionCount} items", color = MaterialTheme.colorScheme.primary)
                        if (definition.timeLimitMinutes > 0) {
                            ChipPill("${definition.timeLimitMinutes} min", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        ChipPill("pass ${definition.passMark}%", color = MaterialTheme.colorScheme.secondary)
                        if (best?.scorePercent != null) {
                            ChipPill("best ${best.scorePercent}%", color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                    if (poolSize < definition.questionCount) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "$poolSize items currently available for this pool.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = "Daily assessments",
                subtitle = "$daily daily assessments, one closing each accelerator day"
            )
            VanguardCard {
                Text(
                    text = "Each accelerator day ends with a short assessment drawn from that day's material. Open the day from the 60-day programme to take it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                ChipPill(
                    text = "Open the accelerator",
                    color = MaterialTheme.colorScheme.secondary,
                    filled = true,
                    onClick = { navController.navigate(Routes.ACCELERATOR) }
                )
            }
        }

        if (attempts.isNotEmpty()) {
            item { SectionHeader(title = "Attempt history") }
            items(attempts.take(20)) { attempt ->
                VanguardCard {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.AssignmentTurnedIn,
                            contentDescription = null,
                            tint = if (attempt.passed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = attempt.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = SimpleDateFormat("dd MMM yyyy HH:mm", Locale.US).format(Date(attempt.completedAt ?: attempt.startedAt)),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${attempt.scorePercent ?: 0}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = if (attempt.passed) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }

        item {
            SectionHeader("Domain readiness")
            VanguardCard {
                competency.domains.forEach { domain ->
                    Row(
                        Modifier
                            .fillMaxSize()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = domain.domain.title,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${domain.questionsAnswered} answered - ${domain.band.label}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        androidx.compose.foundation.layout.Box(Modifier.width(80.dp)) {
                            ProgressLine(progress = domain.score / 100f)
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${domain.score}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
