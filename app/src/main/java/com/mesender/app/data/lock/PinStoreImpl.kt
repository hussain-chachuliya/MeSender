package com.mesender.app.data.lock

import com.mesender.app.domain.lock.PinStore
import javax.inject.Inject

class PinStoreImpl @Inject constructor(
    private val provider: DataStoreProvider
) : PinStore {

    override suspend fun isPinSet(): Boolean = provider.readPinHash() != null

    override suspend fun savePinHash(hash: String) = provider.writePinHash(hash)

    override suspend fun pinHash(): String? = provider.readPinHash()

    override suspend fun clearPin() = provider.clearPinHash()
}