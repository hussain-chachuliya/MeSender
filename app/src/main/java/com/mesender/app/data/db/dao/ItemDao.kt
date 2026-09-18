package com.mesender.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.mesender.app.data.db.entity.ItemEntity
import com.mesender.app.data.db.entity.ItemWithInbox
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Query("SELECT * FROM items WHERE inboxId = :inboxId ORDER BY createdAt DESC")
    fun observeItemsByInbox(inboxId: Long): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE id = :itemId")
    fun observeItem(itemId: Long): Flow<ItemEntity?>

    @Insert
    suspend fun insert(item: ItemEntity): Long

    @Query("DELETE FROM items WHERE id = :itemId")
    suspend fun delete(itemId: Long)

    @Query("DELETE FROM items WHERE inboxId = :inboxId")
    suspend fun deleteItemsForInbox(inboxId: Long)

    @Query(
        """
        SELECT * FROM items_fts
        JOIN items ON items.id = items_fts.rowid
        WHERE items_fts MATCH :query
        ORDER BY items.id DESC
        """
    )
    fun searchRaw(query: String): Flow<List<ItemEntity>>

    @Query(
        """
        SELECT * FROM items_fts
        JOIN items ON items.id = items_fts.rowid
        JOIN inboxes ON inboxes.id = items.inboxId
        WHERE items_fts MATCH :query
        ORDER BY items.id DESC
        """
    )
    fun searchWithInbox(query: String): Flow<List<ItemWithInbox>>

    @Query("UPDATE items SET mediaPath = :path, needsMediaRetry = 0 WHERE id = :itemId")
    suspend fun setMediaPath(itemId: Long, path: String)
}
