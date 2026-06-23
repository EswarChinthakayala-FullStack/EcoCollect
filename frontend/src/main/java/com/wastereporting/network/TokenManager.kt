package com.wastereporting.network

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings
import android.content.Context
import com.wastereporting.WasteReportingApp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * SessionManager — stores the current logged-in user's role and session token locally.
 * No JWT, no backend. Purely local persistence.
 */
object TokenManager {
    private val settings: Settings by lazy {
        val sharedPrefs = WasteReportingApp.appContext.getSharedPreferences("wastereporting_prefs", Context.MODE_PRIVATE)
        SharedPreferencesSettings(sharedPrefs)
    }
    private const val SESSION_KEY = "local_session_token"
    private const val ROLE_KEY = "local_user_role"

    private val _isLoggedInFlow = MutableStateFlow(isLoggedIn())
    val isLoggedInFlow: StateFlow<Boolean> = _isLoggedInFlow.asStateFlow()

    var jwtToken: String?
        get() = settings.getStringOrNull(SESSION_KEY)
        set(value) {
            if (value != null) settings.putString(SESSION_KEY, value)
            else settings.remove(SESSION_KEY)
            _isLoggedInFlow.value = (value != null)
        }

    var userRole: String?
        get() = settings.getStringOrNull(ROLE_KEY)
        set(value) {
            if (value != null) settings.putString(ROLE_KEY, value)
            else settings.remove(ROLE_KEY)
        }

    fun isLoggedIn(): Boolean = jwtToken != null

    fun clearToken() {
        jwtToken = null
        userRole = null
    }
}

