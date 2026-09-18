package com.mesender.app.presentation.share

import android.net.Uri
import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.InboxRepository
import com.mesender.app.domain.repository.ItemRepository
import com.mesender.app.domain.usecase.inbox.GetInboxes
import com.mesender.app.domain.usecase.item.ComposeTextItem
import com.mesender.app.domain.usecase.item.ShareLinkItem
import com.mesender.app.domain.usecase.item.ShareMediaItem
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShareViewModelTest {

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun init_setsPendingAndCollectsInboxes() = runTest {
        val vm = viewModelWith()
        vm.init(PendingShare.Text("hello", null))
        assertEquals("hello", (vm.uiState.value.pending as PendingShare.Text).body)
        assertEquals(listOf(unlocked, locked), vm.uiState.value.inboxes)
    }

    @Test
    fun init_secondPending_isIgnored() = runTest {
        val vm = viewModelWith()
        vm.init(PendingShare.Text("first", null))
        vm.init(PendingShare.Link("https://example.com", null))
        assertTrue(vm.uiState.value.pending is PendingShare.Text)
    }

    @Test
    fun saveText_savesToChosenInbox() = runTest {
        val repos = FakeRepos()
        val vm = viewModelWith(repos)
        vm.init(PendingShare.Text("note", "A title"))
        vm.saveTo(1)
        assertEquals(1, repos.insertedTexts.size)
        assertEquals(1L to "note", repos.insertedTexts[0])
        assertEquals(false, vm.uiState.value.saving)
        assertEquals("Home", vm.uiState.value.savedTo)
        assertFalse(vm.uiState.value.error)
    }

    @Test
    fun saveLink_savesUrlWithTitle() = runTest {
        val repos = FakeRepos()
        val vm = viewModelWith(repos)
        vm.init(PendingShare.Link("https://example.com/page", "Page"))
        vm.saveTo(1)
        assertEquals(1, repos.insertedLinks.size)
        assertEquals(Triple(1L, "https://example.com/page", "Page"), repos.insertedLinks[0])
        assertEquals("Home", vm.uiState.value.savedTo)
    }

    @Test
    fun saveMedia_passesSourceUriMimeAndTitle() = runTest {
        val repos = FakeRepos()
        val vm = viewModelWith(repos)
        val uri = mockk<Uri>(relaxed = true)
        vm.init(PendingShare.Media(uri, "image/png", "photo"))
        vm.saveTo(1)
        assertEquals(1, repos.insertedMedia.size)
        val (inboxId, source, mime, title) = repos.insertedMedia[0]
        assertEquals(1L, inboxId)
        assertEquals(uri, source)
        assertEquals("image/png", mime)
        assertEquals("photo", title)
    }

    @Test
    fun saveMultiple_dispatchesEveryChild() = runTest {
        val repos = FakeRepos()
        val vm = viewModelWith(repos)
        val uri = mockk<Uri>(relaxed = true)
        vm.init(
            PendingShare.Multiple(
                listOf(
                    PendingShare.Text("one", null),
                    PendingShare.Link("https://example.com/a", null),
                    PendingShare.Media(uri, "image/jpeg", null)
                )
            )
        )
        vm.saveTo(1)
        assertEquals(1, repos.insertedTexts.size)
        assertEquals(1, repos.insertedLinks.size)
        assertEquals(1, repos.insertedMedia.size)
        assertEquals("Home", vm.uiState.value.savedTo)
    }

    @Test
    fun saveToLockedInbox_isAllowedWithoutUnlock() = runTest {
        val repos = FakeRepos()
        val vm = viewModelWith(repos)
        vm.init(PendingShare.Text("secret", null))
        vm.saveTo(2)
        assertEquals(2L, repos.insertedTexts[0].first)
        assertEquals(false, vm.uiState.value.error)
        assertEquals("Private", vm.uiState.value.savedTo)
    }

    @Test
    fun saveFailure_setsErrorAndClearsSaving() = runTest {
        val repos = FakeRepos(saveFails = true)
        val vm = viewModelWith(repos)
        vm.init(PendingShare.Text("note", null))
        vm.saveTo(1)
        assertFalse(vm.uiState.value.saving)
        assertTrue(vm.uiState.value.error)
        assertNull(vm.uiState.value.savedTo)
    }

    @Test
    fun saveWithoutPending_doesNothing() = runTest {
        val repos = FakeRepos()
        val vm = viewModelWith(repos)
        vm.saveTo(1)
        assertTrue(repos.insertedTexts.isEmpty())
        assertFalse(vm.uiState.value.saving)
    }

    private fun viewModelWith(repos: FakeRepos = FakeRepos()): ShareViewModel =
        ShareViewModel(
            GetInboxes(repos),
            ComposeTextItem(repos),
            ShareMediaItem(repos),
            ShareLinkItem(repos)
        )

    private class FakeRepos(
        private val saveFails: Boolean = false
    ) : InboxRepository, ItemRepository {
        val insertedTexts = mutableListOf<Pair<Long, String>>()
        val insertedLinks = mutableListOf<Triple<Long, String, String?>>()
        val insertedMedia = mutableListOf<Quad<Long, Uri, String, String?>>()

        override fun observeInboxes(): Flow<List<Inbox>> = flowOf(listOf(unlocked, locked))
        override fun observeInbox(id: Long): Flow<Inbox?> = flowOf(unlocked)
        override suspend fun createInbox(name: String) = unlocked
        override suspend fun renameInbox(id: Long, name: String) {}
        override suspend fun deleteInbox(id: Long) {}
        override suspend fun setInboxLocked(id: Long, locked: Boolean) {}

        override fun observeItemsByInbox(id: Long): Flow<List<Item>> = flowOf(emptyList())
        override fun observeItem(id: Long): Flow<Item?> = flowOf(null)
        override fun search(query: String): Flow<List<SearchResult>> = flowOf(emptyList())
        override suspend fun insertTextItem(id: Long, text: String): Item {
            if (saveFails) throw IllegalStateException("disk full")
            insertedTexts += id to text
            return item.copy(inboxId = id, textContent = text)
        }
        override suspend fun insertMediaItem(id: Long, uri: Uri, mime: String, title: String?): Item {
            if (saveFails) throw IllegalStateException("disk full")
            insertedMedia += Quad(id, uri, mime, title)
            return item.copy(inboxId = id, type = ItemType.Photo, mediaPath = null, mimeType = mime)
        }
        override suspend fun insertLinkItem(id: Long, url: String, title: String?): Item {
            if (saveFails) throw IllegalStateException("disk full")
            insertedLinks += Triple(id, url, title)
            return item.copy(inboxId = id, title = title)
        }
        override suspend fun deleteItem(item: Item) = true
        override suspend fun reloadMedia(item: Item): Item? = null
    }

    private data class Quad<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
}

private val unlocked = Inbox(1, "Home", isLocked = false, 0, 0)
private val locked = Inbox(2, "Private", isLocked = true, 0, 0)
private val item = Item(1, 1, ItemType.Text, "body", null, null, null, 0, 0)