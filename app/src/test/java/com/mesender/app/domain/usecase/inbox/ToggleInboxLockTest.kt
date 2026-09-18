package com.mesender.app.domain.usecase.inbox

import com.mesender.app.domain.repository.InboxRepository
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ToggleInboxLockTest {
    @Test
    fun `unlocks a currently-locked inbox`() = runTest {
        val repo = mockk<InboxRepository>(relaxed = true)
        ToggleInboxLock(repo)(inboxId = 7, currentlyLocked = true)
        coVerify { repo.setInboxLocked(7, false) }
    }
}