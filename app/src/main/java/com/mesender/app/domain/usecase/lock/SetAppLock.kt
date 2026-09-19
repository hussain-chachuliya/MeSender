package com.mesender.app.domain.usecase.lock

import com.mesender.app.domain.lock.PinStore
import com.mesender.app.domain.util.PinHasher

class SetAppLock(
    private val pinStore: PinStore
) {
    suspend operator fun invoke(pin: String) {
        pinStore.savePinHash(PinHasher.hash(pin))
    }
}