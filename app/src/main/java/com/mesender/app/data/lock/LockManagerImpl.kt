package com.mesender.app.data.lock

import com.mesender.app.domain.lock.LockManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

@Singleton
class LockManagerImpl @Inject constructor() : LockManager {

    private val _isAppUnlocked = MutableStateFlow(false)
    override val isAppUnlocked: Flow<Boolean> = _isAppUnlocked

    private val unlockedInboxes = mutableSetOf<Long>()

    override fun isInboxUnlocked(inboxId: Long): Flow<Boolean> =
        kotlinx.coroutines.flow.flow {
            while (true) {
                emit(inboxId in unlockedInboxes)
                kotlinx.coroutines.delay(50)
            }
        }

    override suspend fun unlockApp() { _isAppUnlocked.value = true }

    override suspend fun lockApp() { _isAppUnlocked.value = false }

    override suspend fun unlockInbox(inboxId: Long) { unlockedInboxes.add(inboxId) }

    override suspend fun lockAllInboxes() { unlockedInboxes.clear() }
}