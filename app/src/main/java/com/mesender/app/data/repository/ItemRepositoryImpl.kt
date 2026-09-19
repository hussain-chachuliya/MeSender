package com.mesender.app.data.repository

import android.content.Context
import android.net.Uri
import com.mesender.app.data.db.dao.ItemDao
import com.mesender.app.data.db.entity.ItemEntity
import com.mesender.app.data.db.toDomain
import com.mesender.app.data.db.toSearchResult
import com.mesender.app.data.media.MediaFileRepository
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.ItemRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ItemRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val itemDao: ItemDao,
    private val mediaFileRepository: MediaFileRepository
) : ItemRepository {

    override fun observeItemsByInbox(inboxId: Long): Flow<List<Item>> =
        itemDao.observeItemsByInbox(inboxId).map { list -> list.map { it.toDomain() } }

    override fun observeItem(itemId: Long): Flow<Item?> =
        itemDao.observeItem(itemId).map { it?.toDomain() }

    override suspend fun insertTextItem(inboxId: Long, text: String): Item {
        val now = System.currentTimeMillis()
        val id = itemDao.insert(
            ItemEntity(
                inboxId = inboxId,
                type = "TEXT",
                text = text,
                title = null,
                mediaPath = null,
                mimeType = null,
                createdAt = now,
                updatedAt = now
            )
        )
        return Item(
            id = id, inboxId = inboxId,
            type = com.mesender.app.domain.model.ItemType.Text,
            textContent = text, title = null,
            mediaPath = null, mimeType = null,
            createdAt = now, updatedAt = now
        )
    }

    override suspend fun insertMediaItem(
        inboxId: Long,
        sourceUri: Uri,
        mimeType: String,
        title: String?
    ): Item {
        val now = System.currentTimeMillis()
        val path = mediaFileRepository.copyMediaToInternal(inboxId, sourceUri)
        val needsRetry = path == null
        val id = itemDao.insert(
            ItemEntity(
                inboxId = inboxId,
                type = if (mimeType.startsWith("video/")) "VIDEO" else "PHOTO",
                text = null,
                title = title,
                mediaPath = path,
                mimeType = mimeType,
                needsMediaRetry = needsRetry,
                createdAt = now,
                updatedAt = now
            )
        )
        return Item(
            id = id, inboxId = inboxId,
            type = if (mimeType.startsWith("video/"))
                com.mesender.app.domain.model.ItemType.Video
            else com.mesender.app.domain.model.ItemType.Photo,
            textContent = null, title = title,
            mediaPath = path, mimeType = mimeType,
            createdAt = now, updatedAt = now
        )
    }

    override suspend fun insertLinkItem(inboxId: Long, url: String, title: String?): Item {
        val now = System.currentTimeMillis()
        val id = itemDao.insert(
            ItemEntity(
                inboxId = inboxId,
                type = "LINK",
                text = url,
                title = title,
                mediaPath = null,
                mimeType = "text/uri-list",
                createdAt = now,
                updatedAt = now
            )
        )
        return Item(
            id = id, inboxId = inboxId,
            type = com.mesender.app.domain.model.ItemType.Link,
            textContent = url, title = title,
            mediaPath = null, mimeType = "text/uri-list",
            createdAt = now, updatedAt = now
        )
    }

    override suspend fun deleteItem(item: Item): Boolean {
        item.mediaPath?.let { mediaFileRepository.deleteMediaFile(it) }
        itemDao.delete(item.id)
        return true
    }

    override fun search(query: String): Flow<List<SearchResult>> =
        itemDao.searchWithInbox(query).map { list -> list.map { it.toSearchResult() } }

    override fun searchInInbox(inboxId: Long, query: String): Flow<List<Item>> =
        itemDao.searchInInbox(inboxId, query).map { list -> list.map { it.toDomain() } }

    override suspend fun reloadMedia(item: Item): Item? {
        if (!item.isRetryableMedia) return null
        // Implementation: re-read item to get sourceUri, retry copy — called from use case with
        // original sourceUri stored in title as a workaround, or by re-launching picker.
        // For v1, retry re-launches the Photo Picker; this returns null.
        return null
    }
}