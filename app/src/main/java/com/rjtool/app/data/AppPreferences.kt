package com.rjtool.app.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "rjtool_prefs")

class AppPreferences(private val context: Context) {
    companion object {
        val CONSENT_GIVEN = booleanPreferencesKey("consent_given")
        val LAST_USED = stringPreferencesKey("last_used")
        val WORKSPACE_PATH = stringPreferencesKey("workspace_path")
    }

    suspend fun setConsentGiven(given: Boolean) {
        context.dataStore.edit { prefs -> prefs[CONSENT_GIVEN] = given }
    }

    fun getConsentGiven(): Flow<Boolean> = context.dataStore.data.map { prefs -> prefs[CONSENT_GIVEN] ?: false }

    suspend fun setLastUsed(tool: String) {
        context.dataStore.edit { prefs -> prefs[LAST_USED] = tool }
    }

    suspend fun setWorkspacePath(path: String) {
        context.dataStore.edit { prefs -> prefs[WORKSPACE_PATH] = path }
    }
}
