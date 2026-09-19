package com.mesender.app.data.lock

import com.mesender.app.domain.lock.PinStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class PinStoreImpl @Inject constructor(
    private val provider: DataStoreProvider
) : PinStore {

    override suspend fun isPinSet(): Boolean = provider.readPinHash() != null

    override suspend fun savePinHash(hash: String) = provider.writePinHash(hash)

    override suspend fun pinHash(): String? = provider.readPinHash()

    override suspend fun clearPin() = provider.clearPinHash()

    override val isAppLockEnabled: Flow<Boolean> = provider.appLockEnabled

    override suspend fun setAppLockEnabled(enabled: Boolean) = provider.setAppLockEnabled(enabled)
}