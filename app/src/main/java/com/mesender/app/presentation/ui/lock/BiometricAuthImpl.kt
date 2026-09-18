package com.mesender.app.presentation.ui.lock

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.mesender.app.domain.lock.BiometricAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.Executor
import javax.inject.Inject

class BiometricAuthImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BiometricAuth {

    override fun isAvailable(): Boolean =
        BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK or
                BiometricManager.Authenticators.DEVICE_CREDENTIAL
        ) == BiometricManager.BIOMETRIC_SUCCESS

    override fun authenticate(
        activity: FragmentActivity,
        onSuccess: () -> Unit,
        onError: (cancelled: Boolean) -> Unit
    ) {
        val executor: Executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(activity, executor, BiometricAuthCallback(onSuccess, onError))
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Unlock MeSender")
            .setSubtitle("Confirm it's you to unlock")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_WEAK or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()
        prompt.authenticate(info)
    }
}

internal class BiometricAuthCallback(
    private val onSuccess: () -> Unit,
    private val onError: (cancelled: Boolean) -> Unit
) : BiometricPrompt.AuthenticationCallback() {

    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        onSuccess()
    }

    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
        val cancelled = errorCode == BiometricPrompt.ERROR_CANCELED ||
            errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
            errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON
        onError(cancelled)
    }

    override fun onAuthenticationFailed() {
        onError(false)
    }
}
