package com.vanguard.grc.ui.screens.practice

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Biotech
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
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.StatTile
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun PracticeLabScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val weak by vm.weakQuestions.collectAsStateWithLifecycle()
    val stats by vm.questionStats.collectAsStateWithLifecycle()
    val competency by vm.competency.collectAsStateWithLifecycle()

    val questions = content.questions
    val byType = questions.groupBy { it.type }
    val byDifficulty = questions.groupBy { it.difficulty }
    val byDomain = questions.groupBy { it.domain }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("PRACTICE LAB")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${questions.size} original assessment items",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Every item is original, carries an explanation, explains why each wrong option is weaker, and points to the framework reference and the lesson to revise. Practice is randomised and weighted by difficulty.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    StatTile("ATTEMPTED", "${stats.sumOf { it.seenCount }}", caption = "Questions seen", modifier = Modifier.weight(1f))
                    StatTile("ACCURACY", "${if (stats.sumOf { it.seenCount } == 0) 0 else stats.sumOf { it.correctCount } * 100 / stats.sumOf { it.seenCount }}%", caption = "Across all items", modifier = Modifier.weight(1f))
                    StatTile("WEAK", "${weak.size}", caption = "Items to revise", modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            SectionHeader("Start a practice set")
            VanguardCard {
                Column {
                    AssessmentCatalog.definitions.filter { it.kind == "PRACTICE" }.forEach { definition ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = definition.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = definition.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            ChipPill(
                                text = "Start",
                                color = MaterialTheme.colorScheme.secondary,
                                filled = true,
                                onClick = { navController.navigate(Routes.assessment(definition.id)) }
                            )
                        }
                    }
                }
            }
        }

        item {
            SectionHeader("Bank composition", subtitle = "Question types across the bank")
            VanguardCard {
                byType.forEach { (type, items) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = type.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        ChipPill("${items.size}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        item {
            SectionHeader("Difficulty distribution")
            VanguardCard {
                byDifficulty.forEach { (difficulty, items) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = difficulty.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        androidx.compose.foundation.layout.Box(
                            Modifier
                                .width(110.dp)
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        ) {
                            androidx.compose.foundation.layout.Box(
                                Modifier
                                    .fillMaxWidth(if (questions.isEmpty()) 0f else items.size.toFloat() / questions.size)
                                    .fillMaxSize()
                                    .clip(MaterialTheme.shapes.extraSmall)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "${items.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            SectionHeader("Practice by competency domain")
        }
        items(byDomain.entries.sortedByDescending { it.value.size }) { entry ->
            val domain = competency.domains.firstOrNull { it.domain.id == entry.key }
            VanguardCard {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = entry.key.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${entry.value.size} items" +
                                (domain?.let { " - your score ${it.score}" } ?: ""),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    ChipPill(
                        text = "Exam",
                        color = MaterialTheme.colorScheme.primary,
                        onClick = { navController.navigate(Routes.assessment("dom_${entry.key.lowercase()}")) }
                    )
                }
            }
        }

        item {
            InfoCallout(
                text = "Questions here are written for this platform. No leaked, recalled or copyrighted certification examination content is reproduced. Each item is attributed to the framework or domain it tests so you can revise the right material.",
                kind = com.vanguard.grc.ui.components.CalloutKind.INFO
            )
        }
    }
}
