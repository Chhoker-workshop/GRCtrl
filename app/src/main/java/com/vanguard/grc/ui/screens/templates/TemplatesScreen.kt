package com.vanguard.grc.ui.screens.templates

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.domain.export.ExportResult
import com.vanguard.grc.domain.export.ExportService
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.LabeledValue
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import kotlinx.coroutines.launch

@Composable
fun TemplatesScreen(
    vm: GrcViewModel,
    navController: NavHostController
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val templates = content.templates
    val byCategory = templates.groupBy { it.category }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            VanguardCard(accent = MaterialTheme.colorScheme.secondary) {
                Eyebrow("DOCUMENTATION LIBRARY")
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "${templates.size} working templates",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "Policies, charters, registers and reports - each with purpose, instructions, required information, worked examples, ownership, review frequency and approval requirements.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            InfoCallout(
                text = "Every template here is an educational example. It is not a legally compliant document, it is not tailored to your organisation, and completing it does not make you compliant with any standard, law or certification scheme. Have your own legal, compliance and audit reviewers adapt it.",
                kind = CalloutKind.WARNING
            )
        }

        byCategory.forEach { (category, items) ->
            item { SectionHeader(title = category) }
            items(items) { template ->
                VanguardCard(onClick = { navController.navigate(Routes.template(template.id)) }) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Outlined.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = template.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = template.purpose,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 3
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (template.ownership.isNotBlank()) {
                            ChipPill(template.ownership, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (template.reviewFrequency.isNotBlank()) {
                            ChipPill(template.reviewFrequency, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TemplateDetailScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    templateId: String
) {
    val content by vm.content.collectAsStateWithLifecycle()
    val template = content.templatesById[templateId]
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }

    if (template == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item { InfoCallout("Template not found.", kind = CalloutKind.WARNING) }
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
                Eyebrow(template.category.uppercase())
                Spacer(Modifier.height(6.dp))
                Text(
                    text = template.title,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = template.purpose,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (template.instructions.isNotEmpty()) {
            item {
                SectionHeader("Instructions")
                VanguardCard {
                    template.instructions.forEach { instruction ->
                        Text(
                            text = "- $instruction",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }

        item {
            SectionHeader("Template")
            VanguardCard {
                Text(
                    text = template.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        if (template.requiredInformation.isNotEmpty()) {
            item {
                SectionHeader("Required information")
                VanguardCard {
                    template.requiredInformation.forEach {
                        Text(
                            text = "- $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }

        if (template.exampleEntries.isNotEmpty()) {
            item {
                SectionHeader("Example entries")
                VanguardCard {
                    template.exampleEntries.forEach {
                        Text(
                            text = "- $it",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(6.dp))
                    }
                }
            }
        }

        item {
            SectionHeader("Governance")
            VanguardCard {
                LabeledValue("Ownership", template.ownership.ifBlank { "Not specified" })
                Spacer(Modifier.height(10.dp))
                LabeledValue("Review frequency", template.reviewFrequency.ifBlank { "Not specified" })
                Spacer(Modifier.height(10.dp))
                LabeledValue("Approval required", template.approvalRequired.ifBlank { "Not specified" })
            }
        }

        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(
                            ClipData.newPlainText(template.title, template.body)
                        )
                        message = "Template copied to the clipboard"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Outlined.ContentCopy, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Copy")
                }
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            val sections = listOf(
                                ExportService.PdfSection(
                                    heading = template.title,
                                    paragraphs = listOf(template.purpose)
                                ),
                                ExportService.PdfSection(
                                    heading = "Instructions",
                                    bullets = template.instructions
                                ),
                                ExportService.PdfSection(
                                    heading = "Template",
                                    paragraphs = listOf(template.body)
                                ),
                                ExportService.PdfSection(
                                    heading = "Governance",
                                    paragraphs = listOf(
                                        "Ownership: ${template.ownership}",
                                        "Review frequency: ${template.reviewFrequency}",
                                        "Approval required: ${template.approvalRequired}"
                                    )
                                )
                            )
                            when (val result = vm.exportPdf(template.id, template.title, "GRC VANGUARD documentation library", sections)) {
                                is ExportResult.Success -> message = "Exported as ${result.fileName}"
                                is ExportResult.Failure -> message = "Export failed: ${result.reason}"
                            }
                        }
                    }
                ) { Text("Export PDF") }
            }
            if (message != null) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = message!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        item {
            InfoCallout(
                text = template.disclaimer.ifBlank {
                    "Educational example only. Adapt this template to your organisation's context, obligations and internal governance before use. It is not a compliant document and does not constitute professional advice."
                },
                kind = CalloutKind.WARNING
            )
        }
    }
}
