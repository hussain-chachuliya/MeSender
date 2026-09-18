package com.mesender.app.presentation.ui.home

import com.mesender.app.domain.model.Inbox

data class HomeUiState(
    val inboxes: List<Inbox> = emptyList(),
    val isLoading: Boolean = true
)