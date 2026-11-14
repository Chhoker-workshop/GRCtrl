package com.vanguard.grc.data.settings

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class MentorProvider { OFFLINE_ONLY, LOCAL_MODEL, EXTERNAL_API }

/**
 * Lightweight, typed user preferences. Deliberately excludes anything that
 * would constitute a credential or sensitive personal record - the external AI
 * endpoint settings hold only a user-supplied URL and model identifier.
 */
data class UserSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val textScale: Float = 1.0f,
    val dailyGoalMinutes: Int = 120,
    val learnerName: String = "",
    val mentorProvider: MentorProvider = MentorProvider.OFFLINE_ONLY,
    val mentorEndpoint: String = "",
    val mentorModelId: String = "",
    val showSampleData: Boolean = true,
    val simulatorDifficulty: String = "INTERMEDIATE",
    val reduceMotion: Boolean = false,
    val onboarded: Boolean = false,
    val curriculumVersion: String = "1.0.0",
    val isLoggedIn: Boolean = false,
    val loggedInUsername: String = "",
    val loginEndpoint: String = "https://cyberpantology.in/api/grc_login.php"
)

/**
 * Preferences are persisted with SharedPreferences (small, synchronous, and
 * safe to read before the first frame) and exposed as a [StateFlow] so the
 * Compose tree reacts to changes immediately.
 */
class SettingsStore(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _state = MutableStateFlow(readAll())
    val state: StateFlow<UserSettings> = _state.asStateFlow()

    private fun readAll(): UserSettings = UserSettings(
        themeMode = runCatching { ThemeMode.valueOf(prefs.getString(KEY_THEME, null) ?: "SYSTEM") }
            .getOrDefault(ThemeMode.SYSTEM),
        textScale = prefs.getFloat(KEY_TEXT_SCALE, 1.0f).coerceIn(0.85f, 1.6f),
        dailyGoalMinutes = prefs.getInt(KEY_DAILY_GOAL, 120).coerceIn(30, 360),
        learnerName = prefs.getString(KEY_LEARNER_NAME, "") ?: "",
        mentorProvider = runCatching {
            MentorProvider.valueOf(prefs.getString(KEY_MENTOR_PROVIDER, null) ?: "OFFLINE_ONLY")
        }.getOrDefault(MentorProvider.OFFLINE_ONLY),
        mentorEndpoint = prefs.getString(KEY_MENTOR_ENDPOINT, "") ?: "",
        mentorModelId = prefs.getString(KEY_MENTOR_MODEL, "") ?: "",
        showSampleData = prefs.getBoolean(KEY_SHOW_SAMPLES, true),
        simulatorDifficulty = prefs.getString(KEY_DIFFICULTY, "INTERMEDIATE") ?: "INTERMEDIATE",
        reduceMotion = prefs.getBoolean(KEY_REDUCE_MOTION, false),
        onboarded = prefs.getBoolean(KEY_ONBOARDED, false),
        curriculumVersion = prefs.getString(KEY_CURRICULUM_VERSION, "1.0.0") ?: "1.0.0",
        isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false),
        loggedInUsername = prefs.getString(KEY_LOGGED_IN_USERNAME, "") ?: "",
        loginEndpoint = prefs.getString(KEY_LOGIN_ENDPOINT, "https://cyberpantology.in/api/grc_login.php")
            ?: "https://cyberpantology.in/api/grc_login.php"
    )

    private fun write(settings: UserSettings) {
        prefs.edit()
            .putString(KEY_THEME, settings.themeMode.name)
            .putFloat(KEY_TEXT_SCALE, settings.textScale)
            .putInt(KEY_DAILY_GOAL, settings.dailyGoalMinutes)
            .putString(KEY_LEARNER_NAME, settings.learnerName)
            .putString(KEY_MENTOR_PROVIDER, settings.mentorProvider.name)
            .putString(KEY_MENTOR_ENDPOINT, settings.mentorEndpoint)
            .putString(KEY_MENTOR_MODEL, settings.mentorModelId)
            .putBoolean(KEY_SHOW_SAMPLES, settings.showSampleData)
            .putString(KEY_DIFFICULTY, settings.simulatorDifficulty)
            .putBoolean(KEY_REDUCE_MOTION, settings.reduceMotion)
            .putBoolean(KEY_ONBOARDED, settings.onboarded)
            .putString(KEY_CURRICULUM_VERSION, settings.curriculumVersion)
            .putBoolean(KEY_IS_LOGGED_IN, settings.isLoggedIn)
            .putString(KEY_LOGGED_IN_USERNAME, settings.loggedInUsername)
            .putString(KEY_LOGIN_ENDPOINT, settings.loginEndpoint)
            .apply()
    }

    fun update(transform: (UserSettings) -> UserSettings) {
        val updated = transform(_state.value)
        write(updated)
        _state.value = updated
    }

    fun reset() {
        val defaults = UserSettings()
        write(defaults)
        _state.value = defaults
    }

    companion object {
        private const val PREFS_NAME = "grc_vanguard_settings"
        private const val KEY_THEME = "theme_mode"
        private const val KEY_TEXT_SCALE = "text_scale"
        private const val KEY_DAILY_GOAL = "daily_goal_minutes"
        private const val KEY_LEARNER_NAME = "learner_name"
        private const val KEY_MENTOR_PROVIDER = "mentor_provider"
        private const val KEY_MENTOR_ENDPOINT = "mentor_endpoint"
        private const val KEY_MENTOR_MODEL = "mentor_model"
        private const val KEY_SHOW_SAMPLES = "show_sample_data"
        private const val KEY_DIFFICULTY = "simulator_difficulty"
        private const val KEY_REDUCE_MOTION = "reduce_motion"
        private const val KEY_ONBOARDED = "onboarded"
        private const val KEY_CURRICULUM_VERSION = "curriculum_version"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_LOGGED_IN_USERNAME = "logged_in_username"
        private const val KEY_LOGIN_ENDPOINT = "login_endpoint"
    }
}
