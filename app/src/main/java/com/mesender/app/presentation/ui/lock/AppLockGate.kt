package com.mesender.app.presentation.ui.lock

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class AppLockViewModel @Inject constructor(
    private val lockManager: LockManager,
    private val pinStore: PinStore
) : ViewModel() {
    val isUnlocked: StateFlow<Boolean> = lockManager.isAppUnlocked
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    private var _pinSet by mutableStateOf<Boolean?>(null)
    val pinSet: Boolean? get() = _pinSet

    init {
        viewModelScope.launch {
            _pinSet = pinStore.isPinSet()
        }
    }

    fun onSetupComplete() {
        viewModelScope.launch {
            lockManager.unlockApp()
            _pinSet = true
        }
    }

    fun skipSetup() {
        _pinSet = true
    }
}

@Composable
fun AppLockGate(
    lockViewModel: AppLockViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
    content: @Composable () -> Unit
) {
    val unlocked by lockViewModel.isUnlocked.collectAsState()
    val pinSet = lockViewModel.pinSet

    if (pinSet == null) return

    if (!pinSet) {
        LockScreen(
            setupMode = true,
            onUnlocked = { lockViewModel.onSetupComplete() },
            onSkip = { lockViewModel.skipSetup() }
        )
    } else if (unlocked) {
        content()
    } else {
        LockScreen(setupMode = false, onUnlocked = {})
    }
}
