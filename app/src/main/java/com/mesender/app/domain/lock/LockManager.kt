package com.mesender.app.domain.lock

import kotlinx.coroutines.flow.Flow

interface LockManager {
    val isAppUnlocked: Flow<Boolean>
    fun isInboxUnlocked(inboxId: Long): Flow<Boolean>
    suspend fun unlockApp()
    suspend fun lockApp()
    suspend fun unlockInbox(inboxId: Long)
    suspend fun lockAllInboxes()
}