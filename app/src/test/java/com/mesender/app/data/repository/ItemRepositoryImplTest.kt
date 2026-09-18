package com.mesender.app.data.repository

import android.content.Context
import android.net.Uri
import com.mesender.app.data.db.dao.ItemDao
import com.mesender.app.data.db.entity.InboxEntity
import com.mesender.app.data.db.entity.ItemEntity
import com.mesender.app.data.db.entity.ItemWithInbox
import com.mesender.app.data.media.MediaFileRepository
import com.mesender.app.domain.model.ItemType
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ItemRepositoryImplTest {

    private lateinit var itemDao: ItemDao
    private lateinit var mediaRepo: MediaFileRepository
    private lateinit var repo: ItemRepositoryImpl

    @Before
    fun setup() {
        itemDao = mockk(relaxed = true)
        mediaRepo = mockk(relaxed = true)
        repo = ItemRepositoryImpl(mockk<Context>(), itemDao, mediaRepo)
    }

    @Test
    fun insertTextItem_delegatesToDaoAndReturnsDomain() = runTest {
        coEvery { itemDao.insert(any()) } returns 1L
        val item = repo.insertTextItem(10L, "hello")
        assertEquals("hello", item.textContent)
        assertEquals(ItemType.Text, item.type)
        assertEquals(10L, item.inboxId)
        coVerify { itemDao.insert(any()) }
    }

    @Test
    fun insertMediaItem_marksRetryWhenCopyFails() = runTest {
        coEvery { mediaRepo.copyMediaToInternal(any(), any()) } returns null
        val uri = mockk<Uri>()
        val inserted = mutableListOf<ItemEntity>()
        coEvery { itemDao.insert(any()) } answers { inserted.add(firstArg()); 42L }
        val item = repo.insertMediaItem(7L, uri, "image/png", "pic")
        assertNull(item.mediaPath)
        assertEquals(ItemType.Photo, item.type)
        val entity = inserted.single()
        assertNull(entity.mediaPath)
        assertTrue(entity.needsMediaRetry)
        assertEquals("image/png", entity.mimeType)
    }

    @Test
    fun deleteItem_removesRowAndFile() = runTest {
        coEvery { mediaRepo.deleteMediaFile(any()) } returns true
        val item = mockk<com.mesender.app.domain.model.Item> {
            every { id } returns 5L
            every { mediaPath } returns "/fake/path"
            every { isMedia } returns true
            every { isRetryableMedia } returns false
        }
        assertTrue(repo.deleteItem(item))
        coVerify { mediaRepo.deleteMediaFile("/fake/path") }
        coVerify { itemDao.delete(5L) }
    }

    @Test
    fun search_mapsRowsToSearchResult() = runTest {
        val entity = ItemEntity(
            id = 3L, inboxId = 1L, type = "TEXT", text = "needle", title = null,
            mediaPath = null, mimeType = null, createdAt = 100L, updatedAt = 100L
        )
        val inbox = InboxEntity(id = 1L, name = "Inbox One", createdAt = 10L, updatedAt = 10L)
        every {
            itemDao.searchWithInbox("needle")
        } returns flowOf(listOf(ItemWithInbox(item = entity, inbox = inbox)))
        val results = repo.search("needle").first()
        assertEquals(1, results.size)
        val result = results[0]
        assertEquals(3L, result.item.id)
        assertEquals("needle", result.item.textContent)
        assertEquals("Inbox One", result.inboxName)
        assertFalse(result.inboxLocked)
    }
}