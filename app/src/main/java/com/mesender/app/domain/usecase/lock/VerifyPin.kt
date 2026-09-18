package com.mesender.app.domain.usecase.lock

import com.mesender.app.domain.lock.PinStore
import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.util.PinHasher

class VerifyPin(
    private val pinStore: PinStore,
    private val lockManager: LockManager
) {
    suspend operator fun invoke(pin: String): Boolean {
        val stored = pinStore.pinHash() ?: return false
        if (PinHasher.verify(pin, stored)) {
            lockManager.unlockApp()
            return true
        }
        return false
    }
}