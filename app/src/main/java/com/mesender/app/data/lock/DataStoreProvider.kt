package com.mesender.app.data.lock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mesender.app.presentation.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlinx.coroutines.flow.first

const val DATA_STORE_NAME = "mesender_prefs"

private val PIN_HASH = stringPreferencesKey("pin_hash")
private val APP_LOCK_ENABLED = booleanPreferencesKey("app_lock_enabled")
private val APP_THEME = stringPreferencesKey("app_theme")

class DataStoreProvider @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    suspend fun readPinHash(): String? =
        dataStore.data.first()[PIN_HASH]

    suspend fun writePinHash(hash: String) {
        dataStore.edit { it[PIN_HASH] = hash }
    }

    suspend fun clearPinHash() {
        dataStore.edit { it.remove(PIN_HASH) }
    }

    val appLockEnabled: Flow<Boolean> =
        dataStore.data.map { it[APP_LOCK_ENABLED] == true }

    suspend fun setAppLockEnabled(enabled: Boolean) {
        dataStore.edit { it[APP_LOCK_ENABLED] = enabled }
    }

    val appTheme: Flow<AppTheme> =
        dataStore.data.map {
            try {
                AppTheme.valueOf(it[APP_THEME] ?: AppTheme.GREEN.name)
            } catch (_: Exception) {
                AppTheme.GREEN
            }
        }

    suspend fun setAppTheme(theme: AppTheme) {
        dataStore.edit { it[APP_THEME] = theme.name }
    }
}