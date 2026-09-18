package com.mesender.app.presentation.ui.search

import com.mesender.app.domain.model.SearchResult

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false
)