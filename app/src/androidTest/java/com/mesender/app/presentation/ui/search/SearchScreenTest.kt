package com.mesender.app.presentation.ui.search

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTouchInput
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.ItemRepository
import com.mesender.app.domain.usecase.item.DeleteItem
import com.mesender.app.domain.usecase.item.UpdateItemText
import com.mesender.app.domain.usecase.search.SearchItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SearchScreenTest {

    @get:Rule val composeRule = createComposeRule()
    private lateinit var vm: SearchViewModel

    @Before
    fun setUp() {
        val repo = object : ItemRepository {
            override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
            override fun observeItem(id: Long) = flowOf(null)
            override suspend fun insertTextItem(id: Long, text: String) = Item(1, id, ItemType.Text, text, null, null, null, 0, 0)
            override suspend fun insertMediaItem(id: Long, u: Uri, m: String, t: String?) = Item(1, id, ItemType.Photo, null, t, null, m, 0, 0)
            override suspend fun insertLinkItem(id: Long, url: String, t: String?) = Item(1, id, ItemType.Link, url, t, null, null, 0, 0)
            override suspend fun deleteItem(item: Item) = true
            override suspend fun updateItemText(item: Item, newText: String) = true
            override fun search(query: String) =
                flowOf(listOf(SearchResult(Item(1, 1, ItemType.Text, "milk", null, null, null, 0, 0), "Groceries", false)))
            override fun searchInInbox(inboxId: Long, query: String): Flow<List<Item>> = flowOf(emptyList())
            override suspend fun reloadMedia(item: Item) = null
        }
        vm = SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))
    }

    @Test fun search_returnsResult() {
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, vm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("milk")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("milk").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("milk").assertIsDisplayed()
    }

    @Test fun unlockedResultTap_opensInbox() {
        val repo = object : ItemRepository {
            override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
            override fun observeItem(id: Long) = flowOf(null)
            override suspend fun insertTextItem(id: Long, text: String) = Item(1, id, ItemType.Text, text, null, null, null, 0, 0)
            override suspend fun insertMediaItem(id: Long, u: Uri, m: String, t: String?) = Item(1, id, ItemType.Photo, null, t, null, m, 0, 0)
            override suspend fun insertLinkItem(id: Long, url: String, t: String?) = Item(1, id, ItemType.Link, url, t, null, null, 0, 0)
            override suspend fun deleteItem(item: Item) = true
            override suspend fun updateItemText(item: Item, newText: String) = true
            override fun search(query: String) =
                flowOf(listOf(SearchResult(Item(1, 1, ItemType.Text, "buy milk", null, null, null, 0, 0), "Groceries", false)))
            override fun searchInInbox(inboxId: Long, query: String): Flow<List<Item>> = flowOf(emptyList())
            override suspend fun reloadMedia(item: Item) = null
        }
        val tappableVm = SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))
        var openedInboxId: Long? = null
        composeRule.setContent {
            MaterialTheme { SearchScreen({}, { inboxId -> openedInboxId = inboxId }, tappableVm) }
        }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("milk")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("buy milk").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("buy milk").performClick()
        assertEquals(1L, openedInboxId)
    }

    @Test fun lockedInboxResult_showsInboxNameButNotContent() {
        val repo = object : ItemRepository {
            override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
            override fun observeItem(id: Long) = flowOf(null)
            override suspend fun insertTextItem(id: Long, text: String) = Item(1, id, ItemType.Text, text, null, null, null, 0, 0)
            override suspend fun insertMediaItem(id: Long, u: Uri, m: String, t: String?) = Item(1, id, ItemType.Photo, null, t, null, m, 0, 0)
            override suspend fun insertLinkItem(id: Long, url: String, t: String?) = Item(1, id, ItemType.Link, url, t, null, null, 0, 0)
            override suspend fun deleteItem(item: Item) = true
            override suspend fun updateItemText(item: Item, newText: String) = true
            override fun search(query: String) =
                flowOf(listOf(SearchResult(Item(1, 1, ItemType.Text, "secrets", null, null, null, 0, 0), "Work", true)))
            override fun searchInInbox(inboxId: Long, query: String): Flow<List<Item>> = flowOf(emptyList())
            override suspend fun reloadMedia(item: Item) = null
        }
        val lockedVm = SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, lockedVm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("secret")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Locked — Work").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Locked — Work").assertIsDisplayed()
        composeRule.onNodeWithText("secrets").assertDoesNotExist()
    }

    @Test fun longPress_unlockedResult_entersSelectionMode() {
        val selectionVm = vmWithSingleResult("buy milk")
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, selectionVm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("milk")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("buy milk").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("buy milk").performTouchInput { longClick() }
        composeRule.onNodeWithText("1 selected").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Edit").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Delete selected").assertIsDisplayed()
        composeRule.onNodeWithContentDescription("Share selected").assertIsDisplayed()
    }

    @Test fun longPress_lockedResult_doesNotEnterSelectionMode() {
        val lockedResult = SearchResult(Item(1, 1, ItemType.Text, "secrets", null, null, null, 0, 0), "Work", true)
        val repo = repoReturning(lockedResult)
        val lockedVm = SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, lockedVm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("secret")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Locked — Work").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Locked — Work").performTouchInput { longClick() }
        composeRule.onNodeWithText("1 selected").assertDoesNotExist()
    }

    @Test fun deleteSelected_removesResultAfterConfirmation() {
        val deleteVm = vmWithSingleResult("groceries run")
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, deleteVm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("milk")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("groceries run").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("groceries run").performTouchInput { longClick() }
        composeRule.onNodeWithContentDescription("Delete selected").performClick()
        composeRule.onNodeWithText("Delete 1 item(s)?").assertIsDisplayed()
        composeRule.onNodeWithText("Delete").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("groceries run").fetchSemanticsNodes().isEmpty()
        }
        composeRule.onNodeWithText("groceries run").assertDoesNotExist()
    }

    @Test fun editSelected_updatesResultText() {
        val editVm = vmWithSingleResult("milk list")
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, editVm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("milk")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("milk list").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("milk list").performTouchInput { longClick() }
        composeRule.onNodeWithContentDescription("Edit").performClick()
        composeRule.onNodeWithText("Edit message").performTextInput(" updated")
        composeRule.onNodeWithContentDescription("Save edit").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("milk list updated").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("milk list updated").assertIsDisplayed()
    }

    private fun vmWithSingleResult(text: String): SearchViewModel {
        val repo = repoReturning(SearchResult(Item(1, 1, ItemType.Text, text, null, null, null, 0, 0), "Groceries", false))
        return SearchViewModel(SearchItems(repo), DeleteItem(repo), UpdateItemText(repo))
    }

    private fun repoReturning(result: SearchResult = SearchResult(Item(1, 1, ItemType.Text, "milk", null, null, null, 0, 0), "Groceries", false)) = object : ItemRepository {
        override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
        override fun observeItem(id: Long) = flowOf(null)
        override suspend fun insertTextItem(id: Long, text: String) = Item(1, id, ItemType.Text, text, null, null, null, 0, 0)
        override suspend fun insertMediaItem(id: Long, u: Uri, m: String, t: String?) = Item(1, id, ItemType.Photo, null, t, null, m, 0, 0)
        override suspend fun insertLinkItem(id: Long, url: String, t: String?) = Item(1, id, ItemType.Link, url, t, null, null, 0, 0)
        override suspend fun deleteItem(item: Item) = true
        override suspend fun updateItemText(item: Item, newText: String) = true
        override fun search(query: String) = flowOf(listOf(result))
        override fun searchInInbox(inboxId: Long, query: String): Flow<List<Item>> = flowOf(emptyList())
        override suspend fun reloadMedia(item: Item) = null
    }
}