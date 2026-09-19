package com.mesender.app.presentation.ui.thread

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.usecase.inbox.GetInboxes
import com.mesender.app.domain.usecase.item.ComposeTextItem
import com.mesender.app.domain.usecase.item.DeleteItem
import com.mesender.app.domain.usecase.item.GetItemsByInbox
import com.mesender.app.domain.usecase.item.MediaShare
import com.mesender.app.domain.usecase.item.ShareMediaItem
import com.mesender.app.domain.usecase.lock.IsInboxUnlocked
import com.mesender.app.domain.usecase.search.SearchItemsInInbox
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThreadViewModel @Inject constructor(
    private val getItemsByInboxUseCase: GetItemsByInbox,
    private val getInboxesUseCase: GetInboxes,
    private val composeTextItemUseCase: ComposeTextItem,
    private val shareMediaItemUseCase: ShareMediaItem,
    private val deleteItemUseCase: DeleteItem,
    private val isInboxUnlockedUseCase: IsInboxUnlocked,
    private val searchItemsInInboxUseCase: SearchItemsInInbox
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThreadUiState())
    val uiState: StateFlow<ThreadUiState> = _uiState.asStateFlow()

    private var inboxId: Long = -1L
    private var searchJob: Job? = null

    fun start(inboxId: Long) {
        if (this.inboxId == inboxId) return
        this.inboxId = inboxId
        viewModelScope.launch {
            combine(
                getInboxesUseCase(),
                getItemsByInboxUseCase(inboxId),
                isInboxUnlockedUseCase(inboxId)
            ) { inboxes, items, isUnlocked ->
                _uiState.value.copy(
                    inbox = inboxes.firstOrNull { it.id == inboxId },
                    items = items,
                    isUnlocked = isUnlocked
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query, isSearchMode = true)
        searchJob?.cancel()
        if (query.isBlank()) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList(), isSearching = false)
            return
        }
        searchJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            delay(300)
            searchItemsInInboxUseCase(inboxId, query).collect { results ->
                _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
            }
        }
    }

    fun toggleSearchMode() {
        val current = _uiState.value
        if (current.isSearchMode) {
            _uiState.value = current.copy(isSearchMode = false, searchQuery = "", searchResults = emptyList())
            searchJob?.cancel()
        } else {
            _uiState.value = current.copy(isSearchMode = true)
        }
    }

    fun clearSearch() {
        _uiState.value = _uiState.value.copy(
            isSearchMode = false,
            searchQuery = "",
            searchResults = emptyList(),
            isSearching = false
        )
        searchJob?.cancel()
    }

    fun updateDraft(text: String) {
        _uiState.value = _uiState.value.copy(draft = text)
    }

    fun sendText() {
        val text = _uiState.value.draft.trim()
        if (text.isEmpty()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSending = true)
            composeTextItemUseCase(inboxId, text)
            _uiState.value = _uiState.value.copy(isSending = false, draft = "")
        }
    }

    fun sendMedia(uri: Uri, mimeType: String, title: String?) {
        viewModelScope.launch {
            shareMediaItemUseCase(inboxId, MediaShare(uri, mimeType, title))
        }
    }

    fun deleteItem(item: Item) {
        viewModelScope.launch { deleteItemUseCase(item) }
    }
}
