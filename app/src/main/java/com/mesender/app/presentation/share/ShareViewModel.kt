package com.mesender.app.presentation.share

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mesender.app.domain.usecase.inbox.GetInboxes
import com.mesender.app.domain.usecase.item.ComposeTextItem
import com.mesender.app.domain.usecase.item.MediaShare
import com.mesender.app.domain.usecase.item.ShareLinkItem
import com.mesender.app.domain.usecase.item.ShareMediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val getInboxesUseCase: GetInboxes,
    private val composeTextItemUseCase: ComposeTextItem,
    private val shareMediaItemUseCase: ShareMediaItem,
    private val shareLinkItemUseCase: ShareLinkItem
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    fun init(pending: PendingShare) {
        if (_uiState.value.pending == null) {
            _uiState.value = _uiState.value.copy(pending = pending)
            viewModelScope.launch {
                getInboxesUseCase().collect { inboxes ->
                    _uiState.value = _uiState.value.copy(inboxes = inboxes)
                }
            }
        }
    }

    fun saveTo(inboxId: Long) {
        val pending = _uiState.value.pending ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(saving = true)
            val inbox = _uiState.value.inboxes.firstOrNull { it.id == inboxId }
            try {
                when (pending) {
                    is PendingShare.Text -> composeTextItemUseCase(inboxId, pending.body)
                    is PendingShare.Link -> shareLinkItemUseCase(inboxId, pending.url, pending.title)
                    is PendingShare.Media -> shareMediaItemUseCase(inboxId, MediaShare(pending.uri, pending.mimeType, pending.title))
                    is PendingShare.Multiple -> pending.shares.forEach { share ->
                        when (share) {
                            is PendingShare.Text -> composeTextItemUseCase(inboxId, share.body)
                            is PendingShare.Link -> shareLinkItemUseCase(inboxId, share.url, share.title)
                            is PendingShare.Media -> shareMediaItemUseCase(inboxId, MediaShare(share.uri, share.mimeType, share.title))
                            else -> Unit
                        }
                    }
                }
                _uiState.value = _uiState.value.copy(
                    saving = false,
                    savedTo = inbox?.name ?: "inbox",
                    error = false
                )
            } catch (_: Exception) {
                _uiState.value = _uiState.value.copy(saving = false, error = true)
            }
        }
    }
}