package com.taskflow.app.data.repo

import android.content.Context
import androidx.work.WorkManager
import com.taskflow.app.data.SessionManager
import com.taskflow.app.data.local.AppDatabase
import com.taskflow.app.data.remote.ApiResult
import com.taskflow.app.data.remote.ApiService
import com.taskflow.app.data.remote.AuthResponse
import com.taskflow.app.data.remote.GoogleRequest
import com.taskflow.app.data.remote.LoginRequest
import com.taskflow.app.data.remote.PasswordRequest
import com.taskflow.app.data.remote.RegisterRequest
import com.taskflow.app.data.remote.safeCall
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserRepository(
    private val ctx: Context,
    private val api: ApiService,
    private val session: SessionManager,
    private val db: AppDatabase
) {
    suspend fun register(name: String, email: String, password: String) =
        store(safeCall { api.register(RegisterRequest(name.trim(), email.trim(), password)) })

    suspend fun login(email: String, password: String) =
        store(safeCall { api.login(LoginRequest(email.trim(), password)) })

    suspend fun google(idToken: String) = store(safeCall { api.google(GoogleRequest(idToken)) })

    private suspend fun store(result: ApiResult<AuthResponse>): ApiResult<AuthResponse> {
        if (result is ApiResult.Success) {
            // Make sure no data from a previous account is left behind.
            withContext(Dispatchers.IO) { db.clearAllTables() }
            session.saveLogin(result.data.user, result.data.token)
        }
        return result
    }

    /** Settings are saved on the device instantly; this sends them to the server (retried later if it fails). */
    suspend fun pushSettingsIfDirty() {
        if (!session.settingsDirty || !session.isLoggedIn) return
        val body: Map<String, Any> = mapOf(
            "fullName" to session.fullName,
            "preferredLanguage" to session.language,
            "theme" to session.theme,
            "notificationsEnabled" to session.notifications
        )
        if (safeCall { api.updateMe(body) } is ApiResult.Success) session.settingsDirty = false
    }

    suspend fun changePassword(current: String, new: String): ApiResult<Unit> =
        when (val r = safeCall { api.changePassword(PasswordRequest(current, new)) }) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> r
        }

    suspend fun logout() {
        WorkManager.getInstance(ctx).cancelAllWork()
        session.clearUser()
        withContext(Dispatchers.IO) { db.clearAllTables() }
    }
}
