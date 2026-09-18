package com.mesender.app.data.repository

import com.mesender.app.data.db.dao.InboxDao
import com.mesender.app.data.db.entity.InboxEntity
import com.mesender.app.data.db.toDomain
import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.repository.InboxRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InboxRepositoryImpl @Inject constructor(
    private val inboxDao: InboxDao
) : InboxRepository {

    override fun observeInboxes(): Flow<List<Inbox>> =
        inboxDao.observeInboxes().map { list -> list.map { it.toDomain() } }

    override fun observeInbox(inboxId: Long): Flow<Inbox?> =
        inboxDao.observeInbox(inboxId).map { it?.toDomain() }

    override suspend fun createInbox(name: String): Inbox {
        val now = System.currentTimeMillis()
        val id = inboxDao.insert(InboxEntity(name = name, createdAt = now, updatedAt = now))
        return Inbox(id = id, name = name, isLocked = false, createdAt = now, updatedAt = now)
    }

    override suspend fun renameInbox(inboxId: Long, newName: String) =
        inboxDao.rename(inboxId, newName, System.currentTimeMillis())

    override suspend fun deleteInbox(inboxId: Long) = inboxDao.delete(inboxId)

    override suspend fun setInboxLocked(inboxId: Long, locked: Boolean) =
        inboxDao.setLocked(inboxId, locked, System.currentTimeMillis())
}