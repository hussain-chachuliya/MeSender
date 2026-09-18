package com.mesender.app.domain.usecase.item

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ComposeTextItemTest {
    @Test
    fun `inserts text into inbox`() = runTest {
        val repo = mockk<ItemRepository>()
        coEvery { repo.insertTextItem(1, "note") } returns Item(
            id = 5, inboxId = 1, type = ItemType.Text, textContent = "note",
            title = null, mediaPath = null, mimeType = null, createdAt = 0, updatedAt = 0
        )
        val item = ComposeTextItem(repo)(1, "note")
        assertEquals("note", item.textContent)
        assertEquals(ItemType.Text, item.type)
    }
}