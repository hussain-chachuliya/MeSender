package com.mesender.app.data.repository

import android.content.Context
import android.net.Uri
import com.mesender.app.data.db.dao.InboxDao
import com.mesender.app.data.db.dao.ItemDao
import com.mesender.app.data.db.entity.ItemEntity
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
}