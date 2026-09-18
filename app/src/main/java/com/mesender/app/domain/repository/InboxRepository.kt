package com.mesender.app.domain.repository

import com.mesender.app.domain.model.Inbox
import kotlinx.coroutines.flow.Flow

interface InboxRepository {
    fun observeInboxes(): Flow<List<Inbox>>
    fun observeInbox(inboxId: Long): Flow<Inbox?>
    suspend fun createInbox(name: String): Inbox
    suspend fun renameInbox(inboxId: Long, newName: String)
    suspend fun deleteInbox(inboxId: Long)
    suspend fun setInboxLocked(inboxId: Long, locked: Boolean)
}