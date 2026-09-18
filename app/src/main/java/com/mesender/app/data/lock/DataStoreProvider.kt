package com.mesender.app.data.lock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import javax.inject.Inject
import kotlinx.coroutines.flow.first

const val DATA_STORE_NAME = "mesender_prefs"

private val PIN_HASH = stringPreferencesKey("pin_hash")

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
}