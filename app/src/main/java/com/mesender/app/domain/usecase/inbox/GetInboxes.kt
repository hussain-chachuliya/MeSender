package com.mesender.app.domain.usecase.inbox

import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.repository.InboxRepository
import kotlinx.coroutines.flow.Flow

class GetInboxes(private val repository: InboxRepository) {
    operator fun invoke(): Flow<List<Inbox>> = repository.observeInboxes()
}