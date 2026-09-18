package com.mesender.app.presentation.ui.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BiometricAuthImplTest {

    @Test
    fun `authentication succeeded invokes onSuccess only`() {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onError = mockk<(cancelled: Boolean) -> Unit>(relaxed = true)
        val callback = BiometricAuthCallback(onSuccess, onError)

        callback.onAuthenticationSucceeded(mockk<BiometricPrompt.AuthenticationResult>())

        verify(exactly = 1) { onSuccess.invoke() }
        verify(exactly = 0) { onError.invoke(any()) }
    }

    @Test
    fun `authentication failed invokes onError with cancelled false`() {
        val onSuccess = mockk<() -> Unit>(relaxed = true)
        val onError = mockk<(cancelled: Boolean) -> Unit>(relaxed = true)
        val callback = BiometricAuthCallback(onSuccess, onError)

        callback.onAuthenticationFailed()

        verify(exactly = 1) { onError.invoke(false) }
        verify(exactly = 0) { onSuccess.invoke() }
    }

    @Test
    fun `cancel error codes invoke onError with cancelled true`() {
        val onError = mockk<(cancelled: Boolean) -> Unit>(relaxed = true)
        val callback = BiometricAuthCallback(mockk(), onError)

        callback.onAuthenticationError(BiometricPrompt.ERROR_CANCELED, "cancelled")
        callback.onAuthenticationError(BiometricPrompt.ERROR_USER_CANCELED, "user cancelled")
        callback.onAuthenticationError(BiometricPrompt.ERROR_NEGATIVE_BUTTON, "negative button")

        verify(exactly = 3) { onError.invoke(true) }
    }

    @Test
    fun `non cancel error codes invoke onError with cancelled false`() {
        val onError = mockk<(cancelled: Boolean) -> Unit>(relaxed = true)
        val callback = BiometricAuthCallback(mockk(), onError)

        callback.onAuthenticationError(BiometricPrompt.ERROR_LOCKOUT, "lockout")
        callback.onAuthenticationError(BiometricPrompt.ERROR_TIMEOUT, "timeout")
        callback.onAuthenticationError(BiometricPrompt.ERROR_HW_UNAVAILABLE, "hw unavailable")

        verify(exactly = 3) { onError.invoke(false) }
    }

    @Test
    fun `isAvailable reflects canAuthenticate success`() {
        val context = mockk<Context>()
        val manager = mockk<BiometricManager>()
        mockkStatic(BiometricManager::class)
        every { BiometricManager.from(context) } returns manager
        val impl = BiometricAuthImpl(context)

        every { manager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_SUCCESS
        assertTrue(impl.isAvailable())

        every { manager.canAuthenticate(any()) } returns BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED
        assertFalse(impl.isAvailable())
    }
}