package com.mesender.app.presentation.ui.thread

import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.model.Item

data class ThreadUiState(
    val inbox: Inbox? = null,
    val items: List<Item> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val isUnlocked: Boolean? = null,
    val isSearchMode: Boolean = false,
    val searchQuery: String = "",
    val searchResults: List<Item> = emptyList(),
    val isSearching: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val editingItem: Item? = null
) {
    val isGated: Boolean
        get() = inbox != null && inbox.isLocked && isUnlocked != true
    val isSelectionMode: Boolean
        get() = selectedIds.isNotEmpty()
    val isEditing: Boolean
        get() = editingItem != null
}