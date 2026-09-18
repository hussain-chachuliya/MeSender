package com.mesender.app.domain.usecase.inbox

import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.repository.InboxRepository

class CreateInbox(private val repository: InboxRepository) {
    suspend operator fun invoke(name: String): Inbox = repository.createInbox(name)
}