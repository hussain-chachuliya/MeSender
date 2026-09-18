package com.mesender.app.data.lock

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class PinStoreImplTest {

    private lateinit var store: DataStore<Preferences>
    private lateinit var tmpFile: File

    @Before
    fun setup() {
        tmpFile = File(System.getProperty("java.io.tmpdir"), "datastore_test_${System.nanoTime()}.preferences_pb")
        store = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO)
        ) { tmpFile }
    }

    private fun provider() = DataStoreProvider(store)

    @Test
    fun `save and read roundtrip`() = runTest {
        val pinStore = PinStoreImpl(provider())
        assertFalse(pinStore.isPinSet())
        pinStore.savePinHash("salt:digest")
        assertEquals("salt:digest", pinStore.pinHash())
        assertTrue(pinStore.isPinSet())
        pinStore.clearPin()
        assertNull(pinStore.pinHash())
    }
}