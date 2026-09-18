package com.mesender.app.presentation.ui.lock

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesender.app.domain.lock.BiometricAuth
import com.mesender.app.domain.usecase.lock.SetAppLock
import com.mesender.app.domain.usecase.lock.UnlockInbox
import com.mesender.app.domain.usecase.lock.VerifyPin
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class LockViewModel @Inject constructor(
    private val verifyPinUseCase: VerifyPin,
    private val setAppLockUseCase: SetAppLock,
    private val unlockInboxUseCase: UnlockInbox,
    val biometric: BiometricAuth
) : ViewModel() {

    private val _enteredPin = MutableStateFlow("")
    val enteredPin: StateFlow<String> = _enteredPin.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _unlocked = MutableStateFlow(false)
    val unlocked: StateFlow<Boolean> = _unlocked.asStateFlow()

    // setupMode=true → first-run setup, else → unlock (app, or a specific inbox when targetInboxId is set)
    private var setupMode = false
    private var targetInboxId: Long? = null

    fun setSetupMode() {
        setupMode = true
        targetInboxId = null
    }

    fun setInboxUnlock(inboxId: Long) {
        setupMode = false
        targetInboxId = inboxId
    }

    fun onDigit(d: Char) {
        if (_enteredPin.value.length < 4) {
            _enteredPin.value += d
            _error.value = null
            if (_enteredPin.value.length == 4) checkPin()
        }
    }

    fun onDeleteLast() {
        _enteredPin.value = _enteredPin.value.dropLast(1)
        _error.value = null
    }

    fun onBiometricSuccess() { _unlocked.value = true }

    private fun checkPin() {
        val pin = _enteredPin.value
        viewModelScope.launch {
            if (setupMode) {
                setAppLockUseCase(pin)
                _unlocked.value = true
            } else {
                if (verifyPinUseCase(pin)) {
                    targetInboxId?.let { unlockInboxUseCase(it) }
                    _unlocked.value = true
                } else {
                    _enteredPin.value = ""
                    _error.value = "Wrong PIN. Try again."
                }
            }
        }
    }
}