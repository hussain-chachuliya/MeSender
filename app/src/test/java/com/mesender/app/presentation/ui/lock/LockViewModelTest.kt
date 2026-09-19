package com.mesender.app.presentation.ui.lock

import com.mesender.app.domain.lock.BiometricAuth
import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore
import com.mesender.app.domain.usecase.lock.SetAppLock
import com.mesender.app.domain.usecase.lock.UnlockInbox
import com.mesender.app.domain.usecase.lock.VerifyPin
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LockViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `biometric success in inbox mode unlocks the inbox before flipping the flag`() = runTest {
        lateinit var vm: LockViewModel
        val lockManager = object : LockManager {
            var unlockedAlreadyAtUnlockInbox: Boolean? = null
            override val isAppUnlocked: Flow<Boolean> = flowOf(false)
            override fun isInboxUnlocked(inboxId: Long): Flow<Boolean> = flowOf(false)
            override suspend fun unlockApp() = Unit
            override suspend fun lockApp() = Unit
            override suspend fun unlockInbox(inboxId: Long) {
                unlockedAlreadyAtUnlockInbox = vm.unlocked.value
            }
            override suspend fun lockAllInboxes() = Unit
        }
vm = LockViewModel(
            VerifyPin(mockk(), lockManager),
            SetAppLock(mockk()),
            UnlockInbox(lockManager),
            lockManager,
            mockk<BiometricAuth>()
        )
        vm.setInboxUnlock(7L)

        vm.onBiometricSuccess()

        assertTrue(vm.unlocked.value)
        // UnlockInbox ran while the UI was still locked — no reveal-before-unlock race.
        assertFalse(requireNotNull(lockManager.unlockedAlreadyAtUnlockInbox))
    }

    @Test
    fun `biometric success without inbox mode unlocks the app flag only`() = runTest {
        val lockManager = mockk<LockManager>(relaxed = true)
        val vm = LockViewModel(
            VerifyPin(mockk(), lockManager),
            SetAppLock(mockk()),
            UnlockInbox(lockManager),
            lockManager,
            mockk<BiometricAuth>()
        )

        vm.onBiometricSuccess()

        assertTrue(vm.unlocked.value)
        coVerify(exactly = 0) { lockManager.unlockInbox(any()) }
    }
}