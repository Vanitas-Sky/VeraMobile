package com.vera.mobile.data.local

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "vera_session")

class SessionManager(private val context: Context) {
    companion object {
        val TOKEN_KEY = stringPreferencesKey("auth_token")
        val USER_ROLE_KEY = stringPreferencesKey("user_role")
        val IS_AUDITOR_KEY = booleanPreferencesKey("is_auditor")

        private val _sessionExpiredEvent = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
        val sessionExpiredEvent = _sessionExpiredEvent.asSharedFlow()

        fun notifySessionExpired() {
            _sessionExpiredEvent.tryEmit(Unit)
        }
    }

    // Token JWT / Sanctum
    val authToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[TOKEN_KEY]
    }

    // Bandera para saber si el usuario logueado es auditor externo
    val isAuditor: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_AUDITOR_KEY] ?: false
    }

    // Nombre del rol (SUPER_ADMIN, ADMIN_PYME, ACCOUNTANT, AUDITOR_EXTERNO)
    val userRole: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_ROLE_KEY]
    }

    // Guardar sesión tras login
    suspend fun saveSession(token: String, role: String? = null, isAuditorUser: Boolean = false) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[IS_AUDITOR_KEY] = isAuditorUser || (role == "AUDITOR_EXTERNO")
            if (role != null) {
                preferences[USER_ROLE_KEY] = role
            }
        }
    }

    suspend fun saveAuthToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    // Limpiar toda la sesión al cerrar cuenta
    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USER_ROLE_KEY)
            preferences.remove(IS_AUDITOR_KEY)
        }
    }

    suspend fun clearAuthToken() {
        clearSession()
    }
}
