package com.mesender.app.presentation.ui.search

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.ItemRepository
import com.mesender.app.domain.usecase.search.SearchItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
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
            override fun search(query: String) =
                flowOf(listOf(SearchResult(Item(1, 1, ItemType.Text, "milk", null, null, null, 0, 0), "Groceries", false)))
            override suspend fun reloadMedia(item: Item) = null
        }
        vm = SearchViewModel(SearchItems(repo))
    }

    @Test fun search_returnsResult() {
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, vm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("milk")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("milk").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("milk").assertIsDisplayed()
    }

    @Test fun lockedInboxResult_showsInboxNameButNotContent() {
        val repo = object : ItemRepository {
            override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
            override fun observeItem(id: Long) = flowOf(null)
            override suspend fun insertTextItem(id: Long, text: String) = Item(1, id, ItemType.Text, text, null, null, null, 0, 0)
            override suspend fun insertMediaItem(id: Long, u: Uri, m: String, t: String?) = Item(1, id, ItemType.Photo, null, t, null, m, 0, 0)
            override suspend fun insertLinkItem(id: Long, url: String, t: String?) = Item(1, id, ItemType.Link, url, t, null, null, 0, 0)
            override suspend fun deleteItem(item: Item) = true
            override fun search(query: String) =
                flowOf(listOf(SearchResult(Item(1, 1, ItemType.Text, "secrets", null, null, null, 0, 0), "Work", true)))
            override suspend fun reloadMedia(item: Item) = null
        }
        val lockedVm = SearchViewModel(SearchItems(repo))
        composeRule.setContent { MaterialTheme { SearchScreen({}, {}, lockedVm) } }
        composeRule.onNodeWithText("Search everything you saved").performTextInput("secret")
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodesWithText("Locked — Work").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("Locked — Work").assertIsDisplayed()
        composeRule.onNodeWithText("secrets").assertDoesNotExist()
    }
}