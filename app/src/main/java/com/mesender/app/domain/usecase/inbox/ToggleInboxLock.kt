package com.mesender.app.domain.usecase.inbox

import com.mesender.app.domain.repository.InboxRepository

class ToggleInboxLock(private val repository: InboxRepository) {
    suspend operator fun invoke(inboxId: Long, currentlyLocked: Boolean) {
        repository.setInboxLocked(inboxId, !currentlyLocked)
    }
}