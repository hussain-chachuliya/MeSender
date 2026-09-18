package com.mesender.app.data.lock

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LockManagerImplTest {

    @Test
    fun `app unlock and lock roundtrip`() = runTest {
        val manager = LockManagerImpl()
        assertFalse(manager.isAppUnlocked.first())
        manager.unlockApp()
        assertTrue(manager.isAppUnlocked.first())
        manager.lockApp()
        assertFalse(manager.isAppUnlocked.first())
    }

    @Test
    fun `inbox unlock survives until lockAllInboxes`() = runTest {
        val manager = LockManagerImpl()
        manager.unlockInbox(7)
        assertTrue(manager.isInboxUnlocked(7).first())
        manager.lockAllInboxes()
        assertFalse(manager.isInboxUnlocked(7).first())
    }
}