package com.mesender.app.presentation.ui.thread

import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.model.Item

data class ThreadUiState(
    val inbox: Inbox? = null,
    val items: List<Item> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val isUnlocked: Boolean = true
)