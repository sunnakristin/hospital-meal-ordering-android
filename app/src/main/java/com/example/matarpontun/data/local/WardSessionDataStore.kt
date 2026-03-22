package com.example.matarpontun.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.matarpontun.data.remote.dto.LoginRequest
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "ward_session")

/**
 * Persists the ward login so staff don't have to re-authenticate on every launch.
 */
class WardSessionDataStore(private val context: Context) {

    private object Keys {
        val WARD_ID = longPreferencesKey("ward_id")
        val WARD_NAME = stringPreferencesKey("ward_name")
        val PASSWORD = stringPreferencesKey("password")
    }

    suspend fun saveSession(wardId: Long, wardName: String, password: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WARD_ID] = wardId
            prefs[Keys.WARD_NAME] = wardName
            prefs[Keys.PASSWORD] = password
        }
    }

    suspend fun getSavedLoginRequest(): LoginRequest? =
        context.dataStore.data.map { prefs ->
            val name = prefs[Keys.WARD_NAME] ?: return@map null
            val password = prefs[Keys.PASSWORD] ?: return@map null
            LoginRequest(name, password)
        }.firstOrNull()

    suspend fun getSavedWardId(): Long? =
        context.dataStore.data.map { prefs ->
            prefs[Keys.WARD_ID]
        }.firstOrNull()

    suspend fun clearSession() {
        context.dataStore.edit { it.clear() }
    }
}
