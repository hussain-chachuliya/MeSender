package com.mesender.app.presentation.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val pinStore: PinStore,
    private val lockManager: LockManager
) : ViewModel() {

    val appLockEnabled: StateFlow<Boolean> = pinStore.isAppLockEnabled
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    val isPinSet: StateFlow<Boolean> = kotlinx.coroutines.flow.flow {
        emit(pinStore.isPinSet())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    fun setAppLockEnabled(enabled: Boolean) {
        viewModelScope.launch {
            pinStore.setAppLockEnabled(enabled)
            if (enabled) {
                lockManager.lockApp()
            } else {
                lockManager.unlockApp()
            }
        }
    }

    fun disableAppLock() {
        viewModelScope.launch {
            pinStore.setAppLockEnabled(false)
            pinStore.clearPin()
            lockManager.unlockApp()
        }
    }
}
