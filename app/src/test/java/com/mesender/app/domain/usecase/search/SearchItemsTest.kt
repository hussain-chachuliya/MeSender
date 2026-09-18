package com.mesender.app.domain.usecase.search

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.ItemRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchItemsTest {

    private fun result() = SearchResult(
        item = Item(1, 2, ItemType.Text, "milk", null, null, null, 0, 0),
        inboxName = "Groceries",
        inboxLocked = false
    )

    @Test
    fun `empty query returns empty flow without touching repo`() = runTest {
        val repo = mockk<ItemRepository>()
        assertEquals(emptyList<SearchResult>(), SearchItems(repo)("  ").first())
    }

    @Test
    fun `query is escaped before hitting repo`() = runTest {
        val repo = mockk<ItemRepository>()
        coEvery { repo.search("\"mil*k*\"") } returns flowOf(listOf(result()))
        val out = SearchItems(repo)("mil*k").first()
        assertEquals(1, out.size)
    }
}