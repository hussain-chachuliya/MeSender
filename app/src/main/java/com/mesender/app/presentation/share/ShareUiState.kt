package com.mesender.app.presentation.share

import com.mesender.app.domain.model.Inbox

data class ShareUiState(
    val pending: PendingShare? = null,
    val inboxes: List<Inbox> = emptyList(),
    val saving: Boolean = false,
    val savedTo: String? = null,
    val error: Boolean = false
)