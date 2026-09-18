package com.mesender.app.presentation.ui.search

import android.net.Uri
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.ItemRepository
import com.mesender.app.domain.usecase.search.SearchItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

private fun searchResult(text: String) = SearchResult(
    item = Item(1, 1, ItemType.Text, text, null, null, null, 0, 0),
    inboxName = "Groceries",
    inboxLocked = false
)

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    private val mainDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRepo : ItemRepository {
        val searchedQueries = mutableListOf<String>()
        var resultsFor: (String) -> Flow<List<SearchResult>> = { flowOf(emptyList()) }

        override fun search(query: String): Flow<List<SearchResult>> {
            searchedQueries += query
            return resultsFor(query)
        }

        override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
        override fun observeItem(id: Long): Flow<Item?> = flowOf(null)
        override suspend fun insertTextItem(id: Long, text: String) = searchResult(text).item
        override suspend fun insertMediaItem(id: Long, uri: Uri, mime: String, title: String?) = searchResult("x").item
        override suspend fun insertLinkItem(id: Long, url: String, title: String?) = searchResult("x").item
        override suspend fun deleteItem(item: Item) = true
        override suspend fun reloadMedia(item: Item): Item? = null
    }

    @Test
    fun blankQuery_returnsEmptyResultsWithoutHittingRepository() = runTest(mainDispatcher.scheduler) {
        val repo = FakeRepo()
        val vm = SearchViewModel(SearchItems(repo))

        vm.onQueryChange("   ")

        advanceUntilIdle()
        assertEquals("   ", vm.uiState.value.query)
        assertEquals(emptyList<SearchResult>(), vm.uiState.value.results)
        assertEquals(emptyList<String>(), repo.searchedQueries)
    }

    @Test
    fun query_populatesResultsOnlyAfterDebounce() = runTest(mainDispatcher.scheduler) {
        val repo = FakeRepo()
        repo.resultsFor = { flowOf(listOf(searchResult("milk"))) }
        val vm = SearchViewModel(SearchItems(repo))

        vm.onQueryChange("milk")
        runCurrent()
        assertEquals(emptyList<SearchResult>(), vm.uiState.value.results)

        advanceTimeBy(299)
        runCurrent()
        assertEquals(emptyList<SearchResult>(), vm.uiState.value.results)

        advanceTimeBy(1)
        advanceUntilIdle()
        assertEquals(1, vm.uiState.value.results.size)
        assertEquals("milk", vm.uiState.value.results[0].item.textContent)
        assertEquals(false, vm.uiState.value.isSearching)
    }

    @Test
    fun newerQuery_cancelsInFlightSearch() = runTest(mainDispatcher.scheduler) {
        val repo = FakeRepo()
        repo.resultsFor = { query ->
            flow {
                delay(2000)
                emit(listOf(searchResult("stale-$query")))
            }
        }
        val vm = SearchViewModel(SearchItems(repo))

        vm.onQueryChange("mil")
        advanceTimeBy(400)
        runCurrent()
        vm.onQueryChange("milk")
        advanceUntilIdle()

        assertEquals(2, repo.searchedQueries.size)
        assertEquals(1, vm.uiState.value.results.size)
        assertEquals("stale-${repo.searchedQueries.last()}", vm.uiState.value.results[0].item.textContent)
        assertTrue(vm.uiState.value.results.none {
            it.item.textContent == "stale-${repo.searchedQueries.first()}"
        })
        assertEquals(false, vm.uiState.value.isSearching)
    }
}