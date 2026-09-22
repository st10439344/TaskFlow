package com.taskflow.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.taskflow.app.data.remote.UserDto
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow

/** Stores the JWT and user settings in EncryptedSharedPreferences (falls back to plain prefs if the keystore fails). */
class SessionManager(context: Context) {

    data class Stats(val streak: Int, val weekly: Int)

    private val prefs: SharedPreferences = createPrefs(context)

    val stats = MutableStateFlow(Stats(prefs.getInt("streak", 0), prefs.getInt("weekly", 0)))
    val sessionExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    var token: String?
        get() = prefs.getString("token", null)
        set(v) = prefs.edit().putString("token", v).apply()

    var fullName: String
        get() = prefs.getString("fullName", "") ?: ""
        set(v) = prefs.edit().putString("fullName", v).apply()

    var email: String
        get() = prefs.getString("email", "") ?: ""
        set(v) = prefs.edit().putString("email", v).apply()

    var authProvider: String
        get() = prefs.getString("authProvider", "local") ?: "local"
        set(v) = prefs.edit().putString("authProvider", v).apply()

    var language: String
        get() = prefs.getString("language", "en") ?: "en"
        set(v) = prefs.edit().putString("language", v).apply()

    var theme: String
        get() = prefs.getString("theme", "light") ?: "light"
        set(v) = prefs.edit().putString("theme", v).apply()

    var notifications: Boolean
        get() = prefs.getBoolean("notifications", true)
        set(v) = prefs.edit().putBoolean("notifications", v).apply()

    /** True when a settings change has not yet reached the server (retried by the sync worker). */
    var settingsDirty: Boolean
        get() = prefs.getBoolean("settingsDirty", false)
        set(v) = prefs.edit().putBoolean("settingsDirty", v).apply()

    val isLoggedIn: Boolean get() = !token.isNullOrEmpty()

    fun saveLogin(user: UserDto, jwt: String) {
        token = jwt
        fullName = user.fullName
        email = user.email
        authProvider = user.authProvider ?: "local"
        language = user.preferredLanguage ?: "en"
        theme = user.theme ?: "light"
        notifications = user.notificationsEnabled ?: true
        settingsDirty = false
        saveStats(user.streakCount ?: 0, user.weeklyCompletedCount ?: 0)
    }

    fun saveStats(streak: Int, weekly: Int) {
        prefs.edit().putInt("streak", streak).putInt("weekly", weekly).apply()
        stats.value = Stats(streak, weekly)
    }

    fun clearUser() {
        prefs.edit()
            .remove("token").remove("fullName").remove("email").remove("authProvider")
            .remove("settingsDirty").remove("streak").remove("weekly")
            .apply()
        stats.value = Stats(0, 0)
    }

    private fun createPrefs(context: Context): SharedPreferences = try {
        val key = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
        EncryptedSharedPreferences.create(
            context, "taskflow_secure", key,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        context.getSharedPreferences("taskflow_plain", Context.MODE_PRIVATE)
    }
}
