package com.mesender.app.domain.usecase.lock

import com.mesender.app.domain.lock.LockManager
import kotlinx.coroutines.flow.Flow

class IsInboxUnlocked(private val lockManager: LockManager) {
    operator fun invoke(inboxId: Long): Flow<Boolean> = lockManager.isInboxUnlocked(inboxId)
}