package com.mesender.app.domain.usecase.item

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteItemTest {
    @Test
    fun `deletes item`() = runTest {
        val repo = mockk<ItemRepository>()
        val item = Item(1, 2, ItemType.Text, "hi", null, null, null, 0, 0)
        coEvery { repo.deleteItem(item) } returns true
        assertTrue(DeleteItem(repo)(item))
        coVerify { repo.deleteItem(item) }
    }
}