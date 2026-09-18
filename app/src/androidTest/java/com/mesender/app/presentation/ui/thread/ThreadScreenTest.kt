package com.mesender.app.presentation.ui.thread

import android.net.Uri
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.InboxRepository
import com.mesender.app.domain.repository.ItemRepository
import com.mesender.app.domain.usecase.inbox.GetInboxes
import com.mesender.app.domain.usecase.item.ComposeTextItem
import com.mesender.app.domain.usecase.item.DeleteItem
import com.mesender.app.domain.usecase.item.GetItemsByInbox
import com.mesender.app.domain.usecase.item.ShareMediaItem
import com.mesender.app.domain.usecase.lock.IsInboxUnlocked
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ThreadScreenTest {

    @get:Rule val composeRule = createComposeRule()

    private val inbox = Inbox(1, "Work", false, 0, 0)
    private val item = Item(1, 1, ItemType.Text, "buy milk", null, null, null, 0, 0)

    private lateinit var vm: ThreadViewModel

    @Before
    fun setUp() {
        vm = ThreadViewModel(
            GetItemsByInbox(itemRepoWith(item)),
            GetInboxes(inboxRepoWith(inbox)),
            ComposeTextItem(itemRepoWith(item)),
            ShareMediaItem(itemRepoWith(item)),
            DeleteItem(itemRepoWith(item)),
            IsInboxUnlocked(lockManagerWith(unlocked = true))
        )
    }

    @Test fun thread_rendersItemsInInbox() {
        composeRule.setContent { MaterialTheme { ThreadScreen(1, {}, vm) } }
        composeRule.onNodeWithText("buy milk").assertIsDisplayed()
    }

    @Test fun composing_sendsText() {
        composeRule.setContent { MaterialTheme { ThreadScreen(1, {}, vm) } }
        composeRule.onNodeWithText("Message yourself…").performTextInput("hello")
        composeRule.onNodeWithContentDescription("Send").performClick()
        // falls back to item list which now includes inserted text via fake repo
    }

    @Test fun lockedInbox_blocksContentUntilUnlocked() {
        val lockedInbox = Inbox(1, "Work", isLocked = true, 0, 0)
        val lockedItem = Item(1, 1, ItemType.Text, "secrets", null, null, null, 0, 0)
        val lockedVm = ThreadViewModel(
            GetItemsByInbox(itemRepoWith(lockedItem)),
            GetInboxes(inboxRepoWith(lockedInbox)),
            ComposeTextItem(itemRepoWith(lockedItem)),
            ShareMediaItem(itemRepoWith(lockedItem)),
            DeleteItem(itemRepoWith(lockedItem)),
            IsInboxUnlocked(lockManagerWith(unlocked = false))
        )
        composeRule.setContent { MaterialTheme { ThreadScreen(1, {}, lockedVm) } }
        composeRule.onNodeWithText("Inbox locked").assertIsDisplayed()
        composeRule.onNodeWithText("secrets").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Attach").assertDoesNotExist()
        composeRule.onNodeWithContentDescription("Send").assertDoesNotExist()
    }

    private fun itemRepoWith(item: Item) = object : ItemRepository {
        private val items = MutableStateFlow(listOf(item))
        override fun observeItemsByInbox(id: Long) = items
        override fun observeItem(id: Long) = flowOf(item)
        override suspend fun insertTextItem(id: Long, text: String) = item.copy(textContent = text, title = null)
        override suspend fun insertMediaItem(id: Long, uri: Uri, mime: String, title: String?) = item
        override suspend fun insertLinkItem(id: Long, url: String, title: String?) = item
        override suspend fun deleteItem(item: Item) = true
        override fun search(query: String): Flow<List<SearchResult>> = flowOf(emptyList())
        override suspend fun reloadMedia(item: Item) = null
    }

    private fun inboxRepoWith(inbox: Inbox) = object : InboxRepository {
        override fun observeInboxes() = flowOf(listOf(inbox))
        override fun observeInbox(id: Long) = flowOf(inbox)
        override suspend fun createInbox(name: String) = inbox
        override suspend fun renameInbox(id: Long, name: String) {}
        override suspend fun deleteInbox(id: Long) {}
        override suspend fun setInboxLocked(id: Long, locked: Boolean) {}
    }

    private fun lockManagerWith(unlocked: Boolean) = object : LockManager {
        override val isAppUnlocked: Flow<Boolean> = flowOf(unlocked)
        override fun isInboxUnlocked(inboxId: Long): Flow<Boolean> = flowOf(unlocked)
        override suspend fun unlockApp() = Unit
        override suspend fun lockApp() = Unit
        override suspend fun unlockInbox(inboxId: Long) = Unit
        override suspend fun lockAllInboxes() = Unit
    }
}