package com.mesender.app.domain.repository

import android.net.Uri
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.SearchResult
import kotlinx.coroutines.flow.Flow

interface ItemRepository {
    fun observeItemsByInbox(inboxId: Long): Flow<List<Item>>
    fun observeItem(itemId: Long): Flow<Item?>
    suspend fun insertTextItem(inboxId: Long, text: String): Item
    suspend fun insertMediaItem(
        inboxId: Long,
        sourceUri: Uri,
        mimeType: String,
        title: String?
    ): Item
    suspend fun insertLinkItem(inboxId: Long, url: String, title: String?): Item
    suspend fun deleteItem(item: Item): Boolean
    suspend fun updateItemText(item: Item, newText: String): Boolean
    fun search(query: String): Flow<List<SearchResult>>
    fun searchInInbox(inboxId: Long, query: String): Flow<List<Item>>
    suspend fun reloadMedia(item: Item): Item?
}