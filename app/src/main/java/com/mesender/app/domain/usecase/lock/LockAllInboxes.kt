package com.mesender.app.domain.usecase.lock

import com.mesender.app.domain.lock.LockManager

class LockAllInboxes(private val lockManager: LockManager) {
    suspend operator fun invoke() = lockManager.lockAllInboxes()
}