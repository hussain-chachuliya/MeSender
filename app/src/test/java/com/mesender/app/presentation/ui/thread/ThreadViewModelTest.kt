package com.mesender.app.presentation.ui.thread

import android.net.Uri
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ThreadViewModelTest {

    private val inbox = Inbox(1, "Work", isLocked = true, 0, 0)
    private val item = Item(1, 1, ItemType.Text, "buy milk", null, null, null, 0, 0)

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun lockedInbox_withUnlockFlowFalse_isGated() = runTest {
        val vm = viewModelWith(inbox = inbox, lockedManager = FakeLockManager(unlocked = false))
        vm.start(1)
        assertEquals(false, vm.uiState.value.isUnlocked)
        assertTrue(vm.uiState.value.isGated)
    }

    @Test
    fun lockedInbox_liveUnlockEmission_flipsStateWithoutRestarting() = runTest {
        val lockManager = FakeLockManager(unlocked = false)
        val vm = viewModelWith(inbox = inbox, lockedManager = lockManager)
        vm.start(1)
        assertTrue(vm.uiState.value.isGated)

        lockManager.unlockedFlow.value = true

        assertEquals(true, vm.uiState.value.isUnlocked)
        assertFalse(vm.uiState.value.isGated)
        assertEquals(listOf(item), vm.uiState.value.items)

        vm.start(1)
        assertEquals(true, vm.uiState.value.isUnlocked)
    }

    @Test
    fun unlockedInbox_isNotGatedRegardlessOfUnlockFlow() = runTest {
        val vm = viewModelWith(
            inbox = inbox.copy(isLocked = false),
            lockedManager = FakeLockManager(unlocked = false)
        )
        vm.start(1)
        assertEquals(false, vm.uiState.value.isUnlocked)
        assertFalse(vm.uiState.value.isGated)
    }

    @Test
    fun unknownState_beforeFirstEmission_isGated() = runTest {
        val vm = viewModelWith(inbox = inbox, lockedManager = FakeLockManager(unlocked = false))
        assertTrue(vm.uiState.value.isGated)
    }

    private fun viewModelWith(inbox: Inbox, lockedManager: LockManager): ThreadViewModel {
        val itemRepo = object : ItemRepository {
            private val items = MutableStateFlow(listOf(item))
            override fun observeItemsByInbox(id: Long): Flow<List<Item>> = items
            override fun observeItem(id: Long): Flow<Item?> = flowOf(item)
            override suspend fun insertTextItem(id: Long, text: String) = item.copy(textContent = text, title = null)
            override suspend fun insertMediaItem(id: Long, uri: Uri, mime: String, title: String?) = item
            override suspend fun insertLinkItem(id: Long, url: String, title: String?) = item
            override suspend fun deleteItem(item: Item) = true
            override fun search(query: String): Flow<List<SearchResult>> = flowOf(emptyList())
            override suspend fun reloadMedia(item: Item) = null
        }
        val inboxRepo = object : InboxRepository {
            override fun observeInboxes(): Flow<List<Inbox>> = flowOf(listOf(inbox))
            override fun observeInbox(id: Long): Flow<Inbox?> = flowOf(inbox)
            override suspend fun createInbox(name: String) = inbox
            override suspend fun renameInbox(id: Long, name: String) {}
            override suspend fun deleteInbox(id: Long) {}
            override suspend fun setInboxLocked(id: Long, locked: Boolean) {}
        }
        return ThreadViewModel(
            GetItemsByInbox(itemRepo),
            GetInboxes(inboxRepo),
            ComposeTextItem(itemRepo),
            ShareMediaItem(itemRepo),
            DeleteItem(itemRepo),
            IsInboxUnlocked(lockedManager)
        )
    }

    private class FakeLockManager(private var unlocked: Boolean) : LockManager {
        val unlockedFlow = MutableStateFlow(unlocked)
        override val isAppUnlocked: Flow<Boolean> = flowOf(false)
        override fun isInboxUnlocked(inboxId: Long): Flow<Boolean> = unlockedFlow
        override suspend fun unlockApp() = Unit
        override suspend fun lockApp() = Unit
        override suspend fun unlockInbox(inboxId: Long) { unlockedFlow.value = true }
        override suspend fun lockAllInboxes() {}
    }
}