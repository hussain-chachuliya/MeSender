package com.mesender.app.domain.usecase.inbox

import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.repository.InboxRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateInboxTest {
    @Test
    fun `creates inbox with given name`() = runTest {
        val repo = mockk<InboxRepository>()
        coEvery { repo.createInbox("Saved") } returns Inbox(1, "Saved", false, 0, 0)
        val result = CreateInbox(repo)("Saved")
        assertEquals("Saved", result.name)
        coVerify { repo.createInbox("Saved") }
    }
}