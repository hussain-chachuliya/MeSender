package com.mesender.app.domain.usecase.lock

import com.mesender.app.domain.lock.LockManager

class UnlockInbox(private val lockManager: LockManager) {
    suspend operator fun invoke(inboxId: Long) = lockManager.unlockInbox(inboxId)
}