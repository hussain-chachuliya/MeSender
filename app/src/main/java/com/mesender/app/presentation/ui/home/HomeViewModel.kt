package com.mesender.app.presentation.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesender.app.domain.usecase.inbox.CreateInbox
import com.mesender.app.domain.usecase.inbox.DeleteInbox
import com.mesender.app.domain.usecase.inbox.GetInboxes
import com.mesender.app.domain.usecase.inbox.RenameInbox
import com.mesender.app.domain.usecase.inbox.ToggleInboxLock
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getInboxes: GetInboxes,
    private val createInboxUseCase: CreateInbox,
    private val renameInboxUseCase: RenameInbox,
    private val deleteInboxUseCase: DeleteInbox,
    private val toggleInboxLockUseCase: ToggleInboxLock
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getInboxes().collect { inboxes ->
                _uiState.value = HomeUiState(inboxes = inboxes, isLoading = false)
            }
        }
    }

    fun createInbox(name: String) {
        viewModelScope.launch { createInboxUseCase(name) }
    }

    fun renameInbox(inboxId: Long, newName: String) {
        viewModelScope.launch { renameInboxUseCase(inboxId, newName) }
    }

    fun deleteInbox(inboxId: Long) {
        viewModelScope.launch { deleteInboxUseCase(inboxId) }
    }

    fun toggleInboxLock(inboxId: Long, currentlyLocked: Boolean) {
        viewModelScope.launch { toggleInboxLockUseCase(inboxId, currentlyLocked) }
    }
}