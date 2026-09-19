package com.mesender.app.presentation.ui.thread

import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.model.Item

data class ThreadUiState(
    val inbox: Inbox? = null,
    val items: List<Item> = emptyList(),
    val draft: String = "",
    val isSending: Boolean = false,
    val error: String? = null,
    val isUnlocked: Boolean? = null
) {
    /**
     * Lock gating (RULING-3): gated unless we positively know the inbox is
     * unlocked (or positively not locked). Unknown inbox / unknown unlock
     * state renders the gate so locked content and the composer never flash.
     */
    val isGated: Boolean
        get() = inbox != null && inbox.isLocked && isUnlocked != true
}