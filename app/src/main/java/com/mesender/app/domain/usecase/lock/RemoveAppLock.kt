package com.mesender.app.domain.usecase.lock

import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore

class RemoveAppLock(
    private val pinStore: PinStore,
    private val lockManager: LockManager
) {
    suspend operator fun invoke() {
        pinStore.clearPin()
        lockManager.unlockApp()
        lockManager.lockAllInboxes()
    }
}