package com.vanguard.grc.data.auth

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

sealed interface AuthResult {
    data class Success(val username: String, val message: String = "Signed in") : AuthResult
    data class Failure(val message: String) : AuthResult
}

/**
 * Validates credentials against a server-side endpoint on cyberpantology.in.
 * The endpoint is expected to query the `cyberpan_GRC_vangaurd.login` table and
 * return JSON indicating success/failure. No credentials are stored in the app.
 */
class AuthRepository {

    suspend fun login(endpoint: String, username: String, password: String): AuthResult =
        withContext(Dispatchers.IO) {
            val trimmedUser = username.trim()
            if (trimmedUser.isEmpty() || password.isEmpty()) {
                return@withContext AuthResult.Failure("Enter username and password.")
            }
            val urlStr = endpoint.trim()
            if (!urlStr.startsWith("https://")) {
                return@withContext AuthResult.Failure("Login endpoint must use https://")
            }
            var conn: HttpURLConnection? = null
            try {
                conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 15000
                    readTimeout = 15000
                    doOutput = true
                    setRequestProperty("Content-Type", "application/json; charset=utf-8")
                    setRequestProperty("Accept", "application/json")
                }
                val payload = JSONObject().apply {
                    put("username", trimmedUser)
                    put("password", password)
                }.toString()
                OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { it.write(payload) }

                val code = conn.responseCode
                val stream = if (code in 200..299) conn.inputStream else (conn.errorStream ?: conn.inputStream)
                val body = stream?.bufferedReader(Charsets.UTF_8)?.use(BufferedReader::readText).orEmpty()
                parseResponse(code, body, trimmedUser)
            } catch (e: Exception) {
                AuthResult.Failure("Unable to sign in: ${e.message ?: "network error"}")
            } finally {
                runCatching { conn?.disconnect() }
            }
        }

    private fun parseResponse(code: Int, body: String, username: String): AuthResult {
        val json = runCatching { JSONObject(body) }.getOrNull()
        if (json != null) {
            val ok = json.optBoolean("ok", false) ||
                json.optBoolean("success", false) ||
                json.optString("status").equals("ok", true) ||
                json.optString("status").equals("success", true)
            val token = json.optString("token")
            val user = json.optString("username", username).ifBlank { username }
            val msg = json.optString("message", json.optString("error", ""))
            if (ok || (code in 200..299 && token.isNotBlank())) {
                return AuthResult.Success(user, if (msg.isNotBlank()) msg else "Signed in")
            }
            if (code !in 200..299 || json.has("error") || json.has("ok") || json.has("success") || json.has("status")) {
                return AuthResult.Failure(if (msg.isNotBlank()) msg else "Invalid username or password.")
            }
        }
        // Fallback: plain-text responses
        val trimmed = body.trim()
        if (code in 200..299 && (trimmed.equals("ok", true) || trimmed.equals("success", true) || trimmed == "1")) {
            return AuthResult.Success(username)
        }
        if (code in 200..299 && trimmed.contains("ok", true) && !trimmed.contains("not", true) && !trimmed.contains("fail", true)) {
            return AuthResult.Success(username)
        }
        return AuthResult.Failure(if (trimmed.isNotBlank() && trimmed.length < 160) trimmed else "Invalid username or password.")
    }
}
