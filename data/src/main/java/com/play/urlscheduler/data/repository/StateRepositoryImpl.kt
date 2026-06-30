package com.play.urlscheduler.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.play.urlscheduler.domain.model.PrivilegeMode
import com.play.urlscheduler.domain.repository.StateRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "rotator_state")

class StateRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : StateRepository {

    private val dataStore = context.dataStore

    private object PreferencesKeys {
        val PRIVILEGE_MODE = stringPreferencesKey("privilege_mode")
        val LAST_EXECUTION_TIME = longPreferencesKey("last_execution_time")
        val LAST_OPENED_URL = stringPreferencesKey("last_opened_url")
        val LAST_FAILURE_REASON = stringPreferencesKey("last_failure_reason")
        val IS_SERVICE_ENABLED = booleanPreferencesKey("is_service_enabled")
    }

    override val privilegeMode: Flow<PrivilegeMode>
        get() = dataStore.data.map { preferences ->
            val modeName = preferences[PreferencesKeys.PRIVILEGE_MODE] ?: PrivilegeMode.STANDARD.name
            PrivilegeMode.valueOf(modeName)
        }

    override val lastExecutionTime: Flow<Long>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_EXECUTION_TIME] ?: 0L
        }

    override val lastOpenedUrl: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_OPENED_URL]
        }

    override val lastFailureReason: Flow<String?>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.LAST_FAILURE_REASON]
        }
        
    override val isServiceEnabled: Flow<Boolean>
        get() = dataStore.data.map { preferences ->
            preferences[PreferencesKeys.IS_SERVICE_ENABLED] ?: false
        }

    override suspend fun setPrivilegeMode(mode: PrivilegeMode) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRIVILEGE_MODE] = mode.name
        }
    }
    
    override suspend fun setServiceEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SERVICE_ENABLED] = enabled
        }
    }

    override suspend fun setLastExecutionTime(timeMillis: Long) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_EXECUTION_TIME] = timeMillis
        }
    }

    override suspend fun setLastOpenedUrl(url: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_OPENED_URL] = url
        }
    }

    override suspend fun setLastFailureReason(reason: String?) {
        dataStore.edit { preferences ->
            if (reason == null) {
                preferences.remove(PreferencesKeys.LAST_FAILURE_REASON)
            } else {
                preferences[PreferencesKeys.LAST_FAILURE_REASON] = reason
            }
        }
    }
}
