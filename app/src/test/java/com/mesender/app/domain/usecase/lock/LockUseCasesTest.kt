package com.mesender.app.domain.usecase.lock

import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore
import com.mesender.app.domain.util.PinHasher
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LockUseCasesTest {

    private val pinStore = mockk<PinStore>(relaxed = true)
    private val lockManager = mockk<LockManager>(relaxed = true)

    @Test
    fun `set lock stores hash`() = runTest {
        SetAppLock(pinStore)("1234")
        coVerify { pinStore.savePinHash(any()) }
    }

    @Test
    fun `verify correct pin unlocks app`() = runTest {
        val hash = PinHasher.hash("2468")
        coEvery { pinStore.pinHash() } returns hash
        assertTrue(VerifyPin(pinStore, lockManager)("2468"))
        coVerify { lockManager.unlockApp() }
    }

    @Test
    fun `verify wrong pin fails`() = runTest {
        coEvery { pinStore.pinHash() } returns PinHasher.hash("1111")
        assertEquals(false, VerifyPin(pinStore, lockManager)("2222"))
    }
}