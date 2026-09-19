package com.mesender.app.presentation.ui.search

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.SearchResult

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val selectedIds: Set<Long> = emptySet(),
    val draft: String = "",
    val editingItem: Item? = null
) {
    val isSelectionMode: Boolean
        get() = selectedIds.isNotEmpty()
    val isEditing: Boolean
        get() = editingItem != null
}