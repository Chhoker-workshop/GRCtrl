package com.vanguard.grc.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vanguard.grc.data.auth.AuthResult
import com.vanguard.grc.ui.GrcViewModel
import com.vanguard.grc.ui.components.CalloutKind
import com.vanguard.grc.ui.components.Eyebrow
import com.vanguard.grc.ui.components.InfoCallout
import com.vanguard.grc.ui.components.VanguardCard
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    vm: GrcViewModel,
    onLoggedIn: () -> Unit
) {
    val settings by vm.settings.collectAsStateWithLifecycle()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var endpoint by rememberSaveable { mutableStateOf(settings.loginEndpoint) }
    var endpointInitialized by rememberSaveable { mutableStateOf(false) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(settings.loginEndpoint) {
        if (!endpointInitialized && settings.loginEndpoint.isNotBlank()) {
            endpoint = settings.loginEndpoint
            endpointInitialized = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(PaddingValues(horizontal = 20.dp, vertical = 28.dp)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VanguardCard(modifier = Modifier.fillMaxWidth(), accent = MaterialTheme.colorScheme.primary) {
            Icon(
                Icons.Outlined.Lock,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(10.dp))
            Eyebrow("GRC VANGUARD")
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Sign in",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Authenticate with your account to access the platform. Credentials are validated by the configured server endpoint.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it; error = null },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it; error = null },
                label = { Text("Password") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = endpoint,
                onValueChange = { endpoint = it; error = null },
                label = { Text("Login API URL") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                supportingText = { Text("HTTPS endpoint that validates against your website database.") }
            )
            if (error != null) {
                Spacer(Modifier.height(10.dp))
                InfoCallout(text = error!!, kind = CalloutKind.CAUTION)
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = {
                    val user = username.trim()
                    val pass = password
                    val ep = endpoint.trim()
                    scope.launch {
                        loading = true
                        error = null
                        vm.updateSettings { it.copy(loginEndpoint = ep) }
                        when (val result = vm.authRepository.login(ep, user, pass)) {
                            is AuthResult.Success -> {
                                vm.updateSettings {
                                    it.copy(
                                        isLoggedIn = true,
                                        loggedInUsername = result.username,
                                        loginEndpoint = ep,
                                        learnerName = if (it.learnerName.isBlank()) result.username else it.learnerName
                                    )
                                }
                                onLoggedIn()
                            }
                            is AuthResult.Failure -> error = result.message
                        }
                        loading = false
                    }
                },
                enabled = !loading && username.isNotBlank() && password.isNotEmpty() && endpoint.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (loading) {
                    CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    Spacer(Modifier.size(8.dp))
                    Text("Signing in...")
                } else {
                    Text("Sign in")
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text(
            text = "A PHP endpoint template (grc_login.php) is included in the project tools folder for hosting on cyberpantology.in.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
