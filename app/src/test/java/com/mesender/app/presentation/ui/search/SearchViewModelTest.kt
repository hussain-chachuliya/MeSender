package com.mesender.app.presentation.ui.search

import android.net.Uri
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.ItemRepository
import com.mesender.app.domain.usecase.item.DeleteItem
import com.mesender.app.domain.usecase.item.UpdateItemText
import com.mesender.app.domain.usecase.search.SearchItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

private fun searchResult(text: String, id: Long = 1L, locked: Boolean = false) = SearchResult(
    item = Item(id, 1, ItemType.Text, text, null, null, null, 0, 0),
    inboxName = "Groceries",
    inboxLocked = locked
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
        val deletedItems = mutableListOf<Item>()
        var updatedItem: Pair<Item, String>? = null

        override fun search(query: String): Flow<List<SearchResult>> {
            searchedQueries += query
            return resultsFor(query)
        }

        override fun searchInInbox(inboxId: Long, query: String): Flow<List<Item>> = flowOf(emptyList())
        override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
        override fun observeItem(id: Long): Flow<Item?> = flowOf(null)
        override suspend fun insertTextItem(id: Long, text: String) = searchResult(text).item
        override suspend fun insertMediaItem(id: Long, uri: Uri, mime: String, title: String?) = searchResult("x").item
        override suspend fun insertLinkItem(id: Long, url: String, title: String?) = searchResult("x").item
        override suspend fun deleteItem(item: Item): Boolean {
            deletedItems += item
            return true
        }
        override suspend fun updateItemText(item: Item, newText: String): Boolean {
            updatedItem = item to newText
            return true
        }
        override suspend fun reloadMedia(item: Item): Item? = null
    }

    private fun vm(repo: FakeRepo = FakeRepo()): SearchViewModel =
        SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))

    private fun TestScope.vmWithResults(vararg results: SearchResult): Pair<FakeRepo, SearchViewModel> {
        val repo = FakeRepo()
        repo.resultsFor = { flowOf(results.toList()) }
        val viewModel = vm(repo)
        viewModel.onQueryChange("x")
        advanceUntilIdle()
        return repo to viewModel
    }

    @Test
    fun blankQuery_returnsEmptyResultsWithoutHittingRepository() = runTest(mainDispatcher.scheduler) {
        val repo = FakeRepo()
        val vm = SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))

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
        val vm = SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))

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
        val vm = SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))

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

    @Test
    fun toggleSelection_addsAndRemovesId() = runTest(mainDispatcher.scheduler) {
        val (_, vm) = vmWithResults(searchResult("a", id = 10), searchResult("b", id = 20))

        val first = vm.uiState.value.results[0].item
        val second = vm.uiState.value.results[1].item

        vm.toggleSelection(first)
        assertEquals(setOf(10L), vm.uiState.value.selectedIds)
        vm.toggleSelection(second)
        assertEquals(setOf(10L, 20L), vm.uiState.value.selectedIds)
        assertTrue(vm.uiState.value.isSelectionMode)
        vm.toggleSelection(first)
        assertEquals(setOf(20L), vm.uiState.value.selectedIds)
    }

    @Test
    fun lockedResult_ignoresSelectionToggle() = runTest(mainDispatcher.scheduler) {
        val (_, vm) = vmWithResults(searchResult("secret", id = 10, locked = true))

        vm.toggleSelection(vm.uiState.value.results[0].item)

        assertEquals(emptySet<Long>(), vm.uiState.value.selectedIds)
    }

    @Test
    fun selectAllResults_selectsOnlyUnlockedResults() = runTest(mainDispatcher.scheduler) {
        val (_, vm) = vmWithResults(
            searchResult("a", id = 10),
            searchResult("b", id = 20, locked = true),
            searchResult("c", id = 30)
        )

        vm.selectAllResults()

        assertEquals(setOf(10L, 30L), vm.uiState.value.selectedIds)
    }

    @Test
    fun deleteSelected_deletesFromRepoAndFiltersResults() = runTest(mainDispatcher.scheduler) {
        val (repo, vm) = vmWithResults(searchResult("a", id = 10), searchResult("b", id = 20))

        val first = vm.uiState.value.results[0].item
        val second = vm.uiState.value.results[1].item
        vm.toggleSelection(first)
        vm.toggleSelection(second)
        assertEquals(listOf(first, second), vm.getSelectedItems())

        vm.deleteSelected()
        advanceUntilIdle()

        assertEquals(listOf(first, second), repo.deletedItems)
        assertEquals(emptyList<SearchResult>(), vm.uiState.value.results)
        assertEquals(emptySet<Long>(), vm.uiState.value.selectedIds)
    }

    @Test
    fun getSelectedItem_requiresExactlyOneSelection() = runTest(mainDispatcher.scheduler) {
        val (_, vm) = vmWithResults(searchResult("a", id = 10), searchResult("b", id = 20))

        assertEquals(null, vm.getSelectedItem())
        vm.toggleSelection(vm.uiState.value.results[0].item)
        assertEquals(10L, vm.getSelectedItem()?.id)
        vm.toggleSelection(vm.uiState.value.results[1].item)
        assertEquals(null, vm.getSelectedItem())
    }

    @Test
    fun saveEdit_updatesResultTextClearsSelectionAndEditing() = runTest(mainDispatcher.scheduler) {
        val (repo, vm) = vmWithResults(searchResult("old milk", id = 10))

        val item = vm.uiState.value.results[0].item
        vm.toggleSelection(item)
        vm.startEditing(item)
        assertEquals(item, vm.uiState.value.editingItem)
        assertEquals("old milk", vm.uiState.value.draft)
        assertEquals(emptySet<Long>(), vm.uiState.value.selectedIds)
        assertTrue(vm.uiState.value.isEditing)

        vm.updateDraft("fresh milk")
        vm.saveEdit()
        advanceUntilIdle()

        assertEquals("fresh milk", vm.uiState.value.results[0].item.textContent)
        assertEquals(null, vm.uiState.value.editingItem)
        assertEquals("", vm.uiState.value.draft)
        assertEquals(item to "fresh milk", repo.updatedItem)
    }

    @Test
    fun blankOrUnchangedEdit_cancelsWithoutUpdating() = runTest(mainDispatcher.scheduler) {
        val (repo, vm) = vmWithResults(searchResult("same", id = 10))

        val item = vm.uiState.value.results[0].item
        vm.startEditing(item)
        vm.updateDraft("same")
        vm.saveEdit()

        assertEquals(null, vm.uiState.value.editingItem)
        assertEquals(null, repo.updatedItem)
        assertEquals("same", vm.uiState.value.results[0].item.textContent)
    }
}