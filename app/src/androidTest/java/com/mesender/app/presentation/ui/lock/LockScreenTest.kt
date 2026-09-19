package com.mesender.app.presentation.ui.lock

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.fragment.app.FragmentActivity
import com.mesender.app.domain.lock.BiometricAuth
import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore
import com.mesender.app.domain.usecase.lock.SetAppLock
import com.mesender.app.domain.usecase.lock.UnlockInbox
import com.mesender.app.domain.usecase.lock.VerifyPin
import com.mesender.app.domain.util.PinHasher
import com.mesender.app.presentation.theme.AppTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class LockScreenTest {

    @get:Rule val composeRule = createComposeRule()

    @Test
    fun lockScreen_setupMode_acceptsPin() {
        var unlocked = false
        val pinStore = FakePinStore(storedHash = null)
        val vm = lockViewModel(pinStore = pinStore)
        composeRule.setContent {
            MaterialTheme {
                LockScreen(setupMode = true, onUnlocked = { unlocked = true }, viewModel = vm)
            }
        }
        enterPin("1234")
        composeRule.waitUntil(2_000) { unlocked }
        val persisted = runBlocking { pinStore.pinHash() }
        assertTrue(persisted != null && PinHasher.verify("1234", persisted))
    }

    @Test
    fun lockScreen_unlockMode_correctPin_unlocks() {
        var unlocked = false
        composeRule.setContent {
            MaterialTheme {
                LockScreen(
                    setupMode = false,
                    onUnlocked = { unlocked = true },
                    viewModel = lockViewModel(storedPin = pinHashOf("1234"))
                )
            }
        }
        enterPin("1234")
        composeRule.waitUntil(2_000) { unlocked }
    }

    @Test
    fun lockScreen_unlockMode_wrongPin_showsErrorAndStaysLocked() {
        var unlocked = false
        composeRule.setContent {
            MaterialTheme {
                LockScreen(
                    setupMode = false,
                    onUnlocked = { unlocked = true },
                    viewModel = lockViewModel(storedPin = pinHashOf("9999"))
                )
            }
        }
        enterPin("1234")
        composeRule.waitForIdle()
        assertEquals(false, unlocked)
        composeRule.onNodeWithText("Wrong PIN. Try again.").assertIsDisplayed()
    }

    @Test
    fun lockScreen_inboxMode_correctPin_unlocksThatInbox() {
        var unlocked = false
        val lockManager = FakeLockManager()
        composeRule.setContent {
            MaterialTheme {
                LockScreen(
                    setupMode = false,
                    onUnlocked = { unlocked = true },
                    inboxId = 7L,
                    viewModel = lockViewModel(storedPin = pinHashOf("1234"), lockManager = lockManager)
                )
            }
        }
        enterPin("1234")
        composeRule.waitUntil(2_000) { unlocked }
        assertEquals(listOf(7L), lockManager.unlockedInboxes)
    }

    @Test
    fun lockScreen_biometricUnavailable_hidesBiometricSlot() {
        var unlocked = false
        composeRule.setContent {
            MaterialTheme {
                LockScreen(setupMode = false, onUnlocked = { unlocked = true }, viewModel = lockViewModel())
            }
        }
        composeRule.onNodeWithText("Enter your PIN").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Unlock with biometric").assertDoesNotExist()
    }

    private fun enterPin(pin: String) {
        pin.forEach { composeRule.onNodeWithText(it.toString()).performClick() }
    }

    private fun pinHashOf(pin: String): String = PinHasher.hash(pin)

    private fun lockViewModel(
        storedPin: String? = null,
        lockManager: LockManager = FakeLockManager(),
        pinStore: FakePinStore? = null
    ): LockViewModel {
        val store = pinStore ?: FakePinStore(storedHash = storedPin)
        return LockViewModel(
            VerifyPin(store, lockManager),
            SetAppLock(store),
            UnlockInbox(lockManager),
            lockManager,
            FakeBiometric()
        )
    }

    private class FakePinStore(private var storedHash: String?) : PinStore {
        override suspend fun isPinSet(): Boolean = storedHash != null
        override suspend fun savePinHash(hash: String) { storedHash = hash }
        override suspend fun pinHash(): String? = storedHash
        override suspend fun clearPin() { storedHash = null }
        override val isAppLockEnabled: Flow<Boolean> = MutableStateFlow(storedHash != null)
        override suspend fun setAppLockEnabled(enabled: Boolean) = Unit
        override val appTheme: Flow<AppTheme> = MutableStateFlow(AppTheme.GREEN)
        override suspend fun setAppTheme(theme: AppTheme) = Unit
    }

    private class FakeLockManager : LockManager {
        val unlockedInboxes = mutableListOf<Long>()
        override val isAppUnlocked: Flow<Boolean> = MutableStateFlow(true)
        override fun isInboxUnlocked(inboxId: Long): Flow<Boolean> = MutableStateFlow(true)
        override suspend fun unlockApp() = Unit
        override suspend fun lockApp() = Unit
        override suspend fun unlockInbox(inboxId: Long) { unlockedInboxes.add(inboxId) }
        override suspend fun lockAllInboxes() { unlockedInboxes.clear() }
    }

    private class FakeBiometric : BiometricAuth {
        override fun isAvailable(): Boolean = false
        override fun authenticate(
            activity: FragmentActivity,
            onSuccess: () -> Unit,
            onError: (cancelled: Boolean) -> Unit
        ) {
            onSuccess()
        }
    }
}