package com.vanguard.grc.ui.screens.projects

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Workspaces
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
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.DifficultyChip
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.ProgressLine
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes

@Composable
fun ProjectsScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val projects by vm.projects.collectAsStateWithLifecycle()
    val saved = projects.associateBy { it.projectId }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("PRACTICAL CAPSTONES")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${content.projects.size} enterprise-scale projects",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Each project gives you a business background, a scope, starting information, required activities, expected deliverables and evaluation criteria - then a model solution and management-level feedback once you have worked through it.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        items(content.projects) { project ->
            val state = saved[project.id]
            VanguardCard(onClick = { navController.navigate(Routes.project(project.id)) }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Workspaces,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = project.title,
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = project.background,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 3
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip(project.difficulty)
                    ChipPill("${project.estimatedHours} h", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChipPill(
                        text = state?.status?.replace('_', ' ') ?: "Not started",
                        color = if ((state?.progressPercent ?: 0) > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if ((state?.progressPercent ?: 0) > 0) {
                    Spacer(Modifier.height(8.dp))
                    ProgressLine(progress = state!!.progressPercent / 100f)
                }
            }
        }
    }
}

@Composable
fun ProjectDetailScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    projectId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val projects by vm.projects.collectAsStateWithLifecycle()
    val project = content.projectsById[projectId]
    val state = projects.firstOrNull { it.projectId == projectId }

    if (project == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { InfoCallout("Project not found.", kind = CalloutKind.WARNING) }
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
                Eyebrow("CAPSTONE PROJECT")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = project.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    DifficultyChip(project.difficulty)
                    ChipPill("${project.estimatedHours} hours", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ChipPill(project.competencyDomain.replace('_', ' '), color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        item {
            SectionHeader("Business background")
            VanguardCard { Text(project.background, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) }
        }

        item {
            SectionHeader("Scope")
            VanguardCard { Text(project.scope, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) }
        }

        if (project.objectives.isNotEmpty()) {
            item {
                SectionHeader("Objectives")
                VanguardCard { BulletList(project.objectives) }
            }
        }

        if (project.startingInformation.isNotEmpty()) {
            item {
                SectionHeader("Starting information")
                VanguardCard { BulletList(project.startingInformation) }
            }
        }

        if (project.activities.isNotEmpty()) {
            item {
                SectionHeader("Required activities")
                VanguardCard { BulletList(project.activities) }
            }
        }

        if (project.deliverables.isNotEmpty()) {
            item {
                SectionHeader("Expected deliverables")
                VanguardCard { BulletList(project.deliverables) }
            }
        }

        if (project.evaluationCriteria.isNotEmpty()) {
            item {
                SectionHeader("Evaluation criteria")
                VanguardCard { BulletList(project.evaluationCriteria) }
            }
        }

        item {
            SectionHeader(
                title = "Model solution",
                subtitle = "Read this after you have attempted the work - it is the worked answer, not the answer"
            )
            VanguardCard { Text(project.modelSolution, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface) }
        }

        item {
            SectionHeader("Management-level feedback")
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Text(project.managementFeedback, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        }

        item {
            InfoCallout(
                text = "Projects are self-assessed. Work the activity, produce the deliverable, then compare it against the evaluation criteria and the model solution. The quality bar is the one a manager or auditor would apply, not the one a textbook would.",
                kind = CalloutKind.INFO
            )
        }
    }
}
