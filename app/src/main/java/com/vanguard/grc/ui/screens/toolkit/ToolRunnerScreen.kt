package com.vanguard.grc.ui.screens.toolkit

import android.content.Intent
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
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import com.vanguard.grc.data.content.ToolField
import com.vanguard.grc.data.content.ToolFieldType
import com.vanguard.grc.domain.export.ExportResult
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.BulletList
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.ChipPill
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import com.vanguard.grc.ui.navigation.Routes
import kotlinx.coroutines.launch

/**
 * A data-driven runner for every toolkit instrument. The form is generated from
 * the tool definition, so adding a new tool never requires new UI code.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolRunnerScreen(
    vm: GrcViewModel,
    navController: NavHostController,
    toolId: String
) {
    val tool = com.vanguard.grc.domain.toolkit.ToolRegistry.byId(toolId)
    val records by vm.toolRecords.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var values by remember(toolId) { mutableStateOf(tool?.sampleData ?: emptyMap()) }
    var title by remember(toolId) { mutableStateOf(tool?.let { "My ${it.name}" } ?: "") }
    var savedMessage by remember { mutableStateOf<String?>(null) }
    var selectedRecordId by remember { mutableStateOf<Long?>(null) }

    val toolRecords = records.filter { it.toolId == toolId }

    if (tool == null) {
        LazyColumn(Modifier.fillMaxSize()) {
            item {
                InfoCallout("Tool not found.", kind = CalloutKind.WARNING)
            }
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
                Eyebrow(tool.category.uppercase())
                Spacer(Modifier.height(6.dp))
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (tool.referenceFramework.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    ChipPill(tool.referenceFramework, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        if (tool.howToUse.isNotEmpty()) {
            item {
                SectionHeader("How to use this tool")
                VanguardCard {
                    BulletList(tool.howToUse)
                }
            }
        }

        if (toolRecords.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Saved records",
                    subtitle = "Sample records are clearly labelled and can be copied or edited"
                )
            }
            items(toolRecords) { record ->
                VanguardCard(
                    onClick = {
                        selectedRecordId = record.id
                        title = record.title
                        values = parseRecord(record.dataJson)
                    }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = record.title,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (record.isSample) "Sample record - illustrative only" else "Your record",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (record.isSample) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                            )
                        }
                        if (!record.isSample) {
                            TextButton(onClick = {
                                scope.launch { vm.toolkit.deleteRecord(record.id) }
                            }) {
                                Icon(Icons.Outlined.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        item {
            SectionHeader(
                title = if (selectedRecordId != null) "Edit record" else "New record",
                subtitle = "Pre-filled with the worked sample so you can see a complete entry"
            )
        }

        item {
            VanguardCard {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Record title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                tool.fields.forEach { field ->
                    ToolFieldEditor(
                        field = field,
                        value = values[field.key].orEmpty(),
                        onValueChange = { newValue ->
                            values = values.toMutableMap().apply { put(field.key, newValue) }
                        }
                    )
                    Spacer(Modifier.height(12.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            scope.launch {
                                vm.toolkit.saveRecord(
                                    id = selectedRecordId,
                                    toolId = tool.id,
                                    title = title.ifBlank { tool.name },
                                    dataJson = buildJson(values),
                                    isSample = false
                                )
                                savedMessage = "Record saved"
                                selectedRecordId = null
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Outlined.Save, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Save")
                    }
                    OutlinedButton(
                        onClick = {
                            values = tool.sampleData
                            title = "My ${tool.name}"
                            selectedRecordId = null
                        }
                    ) { Text("Reset to sample") }
                }
                if (savedMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = savedMessage!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        item {
            SectionHeader("Export", subtitle = "Produce a register extract for review or external use")
            VanguardCard {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if ("CSV" in tool.exportFormats) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                val headers = tool.fields.map { it.label }
                                val rows = listOf(tool.fields.map { values[it.key].orEmpty() })
                                when (val result = vm.exportToolCsv(tool.id, tool.id, headers, rows, tool.name)) {
                                    is ExportResult.Success -> {
                                        context.startActivity(
                                            Intent.createChooser(
                                                vm.shareIntent(result.uri, result.mimeType),
                                                "Share register"
                                            )
                                        )
                                    }
                                    is ExportResult.Failure -> savedMessage = result.reason
                                }
                            }
                        }) {
                            Icon(Icons.Outlined.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("CSV")
                        }
                    }
                    if ("PDF" in tool.exportFormats) {
                        OutlinedButton(onClick = {
                            scope.launch {
                                val sections = buildList {
                                    add(
                                        com.vanguard.grc.domain.export.ExportService.PdfSection(
                                            heading = tool.name,
                                            paragraphs = listOf(tool.description)
                                        )
                                    )
                                    add(
                                        com.vanguard.grc.domain.export.ExportService.PdfSection(
                                            heading = "Record extract",
                                            table = listOf(
                                                tool.fields.map { it.label }
                                            ) + listOf(tool.fields.map { values[it.key].orEmpty() })
                                        )
                                    )
                                }
                                when (val result = vm.exportPdf(tool.id, tool.name, "GRC VANGUARD working register", sections)) {
                                    is ExportResult.Success -> {
                                        context.startActivity(
                                            Intent.createChooser(
                                                vm.shareIntent(result.uri, result.mimeType),
                                                "Share report"
                                            )
                                        )
                                    }
                                    is ExportResult.Failure -> savedMessage = result.reason
                                }
                            }
                        }) {
                            Icon(Icons.Outlined.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("PDF")
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Exports carry a header identifying them as GRC VANGUARD educational output. They are written to the app's private storage and shared through the system share sheet.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolFieldEditor(
    field: ToolField,
    value: String,
    onValueChange: (String) -> Unit
) {
    when (field.type) {
        ToolFieldType.LONG_TEXT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(field.label + if (field.required) " *" else "") },
                minLines = 3,
                supportingText = if (field.helpText.isNotBlank()) {
                    { Text(field.helpText, style = MaterialTheme.typography.labelSmall) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ToolFieldType.NUMBER -> {
            OutlinedTextField(
                value = value,
                onValueChange = { input -> onValueChange(input.filter { it.isDigit() || it == '.' || it == '-' }) },
                label = { Text(field.label + if (field.required) " *" else "") },
                singleLine = true,
                supportingText = if (field.helpText.isNotBlank()) {
                    { Text(field.helpText, style = MaterialTheme.typography.labelSmall) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ToolFieldType.DATE -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(field.label + if (field.required) " *" else "") },
                singleLine = true,
                placeholder = { Text("YYYY-MM-DD", style = MaterialTheme.typography.bodySmall) },
                supportingText = if (field.helpText.isNotBlank()) {
                    { Text(field.helpText, style = MaterialTheme.typography.labelSmall) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ToolFieldType.BOOLEAN -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = value.equals("true", ignoreCase = true),
                    onCheckedChange = { onValueChange(if (it) "true" else "false") }
                )
                Spacer(Modifier.width(6.dp))
                Column(Modifier.weight(1f)) {
                    Text(field.label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    if (field.helpText.isNotBlank()) {
                        Text(field.helpText, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
        ToolFieldType.SELECT -> {
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = value,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(field.label + if (field.required) " *" else "") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    supportingText = if (field.helpText.isNotBlank()) {
                        { Text(field.helpText, style = MaterialTheme.typography.labelSmall) }
                    } else null,
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    field.options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onValueChange(option)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        ToolFieldType.MULTI_SELECT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(field.label + if (field.required) " *" else "") },
                supportingText = {
                    Text(
                        if (field.helpText.isNotBlank()) field.helpText
                        else "Separate selections with a comma. Options: ${field.options.joinToString(", ")}",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                minLines = 2,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ToolFieldType.SCORE -> {
            OutlinedTextField(
                value = value,
                onValueChange = { input -> onValueChange(input.filter { it.isDigit() || it == '.' }) },
                label = { Text(field.label + if (field.required) " *" else "") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        ToolFieldType.TEXT -> {
            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                label = { Text(field.label + if (field.required) " *" else "") },
                singleLine = true,
                placeholder = if (field.placeholder.isNotBlank()) {
                    { Text(field.placeholder, style = MaterialTheme.typography.bodySmall) }
                } else null,
                supportingText = if (field.helpText.isNotBlank()) {
                    { Text(field.helpText, style = MaterialTheme.typography.labelSmall) }
                } else null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

internal fun buildJson(values: Map<String, String>): String =
    values.entries.joinToString(",") { (k, v) ->
        "\"${k.escapeJson()}\":\"${v.escapeJson()}\""
    }

internal fun parseRecord(json: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    var index = 0
    while (index < json.length) {
        val keyStart = json.indexOf('"', index)
        if (keyStart < 0) break
        val keyEnd = json.indexOf('"', keyStart + 1)
        if (keyEnd < 0) break
        val key = json.substring(keyStart + 1, keyEnd)
        val colon = json.indexOf(':', keyEnd)
        if (colon < 0) break
        val valueStart = json.indexOf('"', colon)
        if (valueStart < 0) break
        var valueEnd = valueStart + 1
        while (valueEnd < json.length && json[valueEnd] != '"') {
            if (json[valueEnd] == '\\') valueEnd++
            valueEnd++
        }
        val value = json.substring(valueStart + 1, minOf(valueEnd, json.length)).replace("\\\"", "\"").replace("\\n", "\n")
        result[key] = value
        index = valueEnd + 1
    }
    return result
}

private fun String.escapeJson(): String = this
    .replace("\\", "\\\\")
    .replace("\"", "\\\"")
    .replace("\n", "\\n")
    .replace("\r", "")
