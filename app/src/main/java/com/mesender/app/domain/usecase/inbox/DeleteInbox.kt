package com.mesender.app.domain.usecase.inbox

import com.mesender.app.domain.repository.InboxRepository

class DeleteInbox(private val repository: InboxRepository) {
    suspend operator fun invoke(inboxId: Long) = repository.deleteInbox(inboxId)
}