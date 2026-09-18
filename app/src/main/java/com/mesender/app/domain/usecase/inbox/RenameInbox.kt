package com.mesender.app.domain.usecase.inbox

import com.mesender.app.domain.repository.InboxRepository

class RenameInbox(private val repository: InboxRepository) {
    suspend operator fun invoke(inboxId: Long, newName: String) =
        repository.renameInbox(inboxId, newName)
}