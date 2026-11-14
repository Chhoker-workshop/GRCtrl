package com.vanguard.grc.ui.screens.settings

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import android.widget.Toast
import com.vanguard.grc.data.settings.MentorProvider
import com.vanguard.grc.data.settings.ThemeMode
import com.vanguard.grc.domain.export.ExportResult
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.SectionHeader
import com.vanguard.grc.ui.components.VanguardCard
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: GrcViewModel) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var showWipeDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportMessage by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SectionHeader("Learner profile")
            VanguardCard {
                OutlinedTextField(
                    value = settings.learnerName,
                    onValueChange = { value -> vm.updateSettings { it.copy(learnerName = value) } },
                    label = { Text("Your name (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(12.dp))
                Eyebrow("Daily study goal")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${settings.dailyGoalMinutes} minutes per day",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = settings.dailyGoalMinutes.toFloat(),
                    onValueChange = { value ->
                        vm.updateSettings { it.copy(dailyGoalMinutes = value.toInt()) }
                    },
                    valueRange = 30f..300f,
                    steps = 8
                )
            }
        }

        item {
            SectionHeader("Appearance and accessibility")
            VanguardCard {
                ThemeMode.entries.forEach { mode ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.themeMode == mode,
                            onClick = { vm.updateSettings { it.copy(themeMode = mode) } }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = when (mode) {
                                ThemeMode.SYSTEM -> "Follow the system theme"
                                ThemeMode.LIGHT -> "Light"
                                ThemeMode.DARK -> "Dark"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                Eyebrow("Text size")
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "${(settings.textScale * 100).toInt()}% of base size",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Slider(
                    value = settings.textScale,
                    onValueChange = { value -> vm.updateSettings { it.copy(textScale = value) } },
                    valueRange = 0.85f..1.60f,
                    steps = 14
                )
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("A", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("A", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        item {
            SectionHeader("AI GRC Mentor")
            VanguardCard {
                MentorProvider.entries.forEach { provider ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = settings.mentorProvider == provider,
                            onClick = { vm.updateSettings { it.copy(mentorProvider = provider) } }
                        )
                        Spacer(Modifier.width(8.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                text = when (provider) {
                                    MentorProvider.OFFLINE_ONLY -> "Offline corpus only (recommended)"
                                    MentorProvider.LOCAL_MODEL -> "Local on-device model"
                                    MentorProvider.EXTERNAL_API -> "External provider"
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (provider) {
                                    MentorProvider.OFFLINE_ONLY -> "The mentor answers only from content packaged in this app. No network access at all."
                                    MentorProvider.LOCAL_MODEL -> "Reserved for a future on-device model. The offline corpus remains the source of truth."
                                    MentorProvider.EXTERNAL_API -> "Reserved for a future optional provider. You supply the endpoint; no credentials are stored in the app."
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                if (settings.mentorProvider != MentorProvider.OFFLINE_ONLY) {
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = settings.mentorEndpoint,
                        onValueChange = { value -> vm.updateSettings { it.copy(mentorEndpoint = value) } },
                        label = { Text("Provider endpoint URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = settings.mentorModelId,
                        onValueChange = { value -> vm.updateSettings { it.copy(mentorModelId = value) } },
                        label = { Text("Model identifier") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "No API key is stored by this application and no key is hardcoded. An optional provider integration would authenticate on your device, at your instruction.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            SectionHeader("Content and data")
            VanguardCard {
                Row(
                    Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = "Show sample data in the toolkit",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Sample records are clearly labelled and never mixed with your own entries.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = settings.showSampleData,
                        onCheckedChange = { checked -> vm.updateSettings { it.copy(showSampleData = checked) } }
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Curriculum version ${settings.curriculumVersion}. Content is packaged with the application, so the app works with no connectivity at all.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(14.dp))
                OutlinedButton(
                    onClick = { showExportDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Export learner data") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showWipeDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Outlined.DeleteForever, contentDescription = null, modifier = Modifier.size(17.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Delete all learner data")
                }
                if (exportMessage != null) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = exportMessage!!,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        item {
            SectionHeader("Account")
            VanguardCard {
                val username = settings.loggedInUsername.ifBlank { "Not signed in" }
                Text(
                    text = "Signed in as $username",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = settings.loginEndpoint,
                    onValueChange = { value -> vm.updateSettings { it.copy(loginEndpoint = value) } },
                    label = { Text("Login API URL") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        vm.updateSettings { it.copy(isLoggedIn = false, loggedInUsername = "") }
                        Toast.makeText(
                            context,
                            "Signed out. Restart the app to return to login.",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Sign out") }
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "After signing out, close and reopen the app to require login again.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            SectionHeader("Privacy and permissions")
            VanguardCard {
                Text(
                    text = "Everything you enter - progress, notes, assessment history, registers and exports - is stored only on this device. There is no analytics SDK, no advertising identifier, no crash reporting service and no account system. The single declared permission (INTERNET) exists solely to support the optional external mentor provider you can enable above; when that provider is off, the application makes no network requests.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "Local records are protected by the operating system's application sandbox. Exported files are written to the app's private storage and shared only when you explicitly ask the system to share them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        item {
            InfoCallout(
                text = "GRC VANGUARD is an independent educational platform. It is not affiliated with, endorsed by, or connected to ISO, ISACA, ISC2, NIST, the PCI Security Standards Council, AICPA, CIS, CSA, or any regulator named in the content.",
                kind = CalloutKind.INFO
            )
        }
    }

    if (showWipeDialog) {
        AlertDialog(
            onDismissRequest = { showWipeDialog = false },
            title = { Text("Delete all learner data") },
            text = {
                Text(
                    "This permanently removes your progress, notes, bookmarks, assessment history, competency scores, saved toolkit records, project work and simulator decisions from this device. It cannot be undone. Export first if you want a copy."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            vm.wipeLearnerData()
                            showWipeDialog = false
                            exportMessage = "All learner data deleted"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Delete everything") }
            },
            dismissButton = {
                TextButton(onClick = { showWipeDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showExportDialog) {
        val files = vm.exportedFiles()
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export learner data") },
            text = {
                Column {
                    Text(
                        "Your progress, notes, bookmarks and assessment history are included in the platform backup and survive a device change through the operating system's backup mechanism. Exported working files are listed below."
                    )
                    Spacer(Modifier.height(10.dp))
                    if (files.isEmpty()) {
                        Text(
                            "No exported files yet. Exports produced from the toolkit appear here.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        files.take(10).forEach { file ->
                            Text(
                                "- ${file.name}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                val result = vm.exportService.exportCsv(
                                    fileName = "grc-vanguard-learner-data",
                                    headers = listOf("Content", "Detail"),
                                    rows = listOf(
                                        listOf("Export date", java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date())),
                                        listOf("Curriculum version", settings.curriculumVersion),
                                        listOf("Note", "Full learner data is preserved by the operating system backup. Use this export for a portable summary.")
                                    ),
                                    title = "GRC VANGUARD learner data export"
                                )
                                exportMessage = if (result is ExportResult.Success) {
                                    "Exported as ${result.fileName}"
                                } else {
                                    "Export failed"
                                }
                                showExportDialog = false
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Create summary export") }
                }
            },
            confirmButton = {
                TextButton(onClick = { showExportDialog = false }) { Text("Close") }
            }
        )
    }
}
