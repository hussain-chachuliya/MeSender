package com.mesender.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.mesender.app.data.db.entity.InboxEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InboxDao {
    @Query("SELECT * FROM inboxes ORDER BY updatedAt DESC")
    fun observeInboxes(): Flow<List<InboxEntity>>

    @Query("SELECT * FROM inboxes WHERE id = :inboxId")
    fun observeInbox(inboxId: Long): Flow<InboxEntity?>

    @Insert
    suspend fun insert(inbox: InboxEntity): Long

    @Update
    suspend fun update(inbox: InboxEntity)

    @Query("UPDATE inboxes SET isLocked = :locked, updatedAt = :now WHERE id = :inboxId")
    suspend fun setLocked(inboxId: Long, locked: Boolean, now: Long)

    @Query("UPDATE inboxes SET name = :name, updatedAt = :now WHERE id = :inboxId")
    suspend fun rename(inboxId: Long, name: String, now: Long)

    @Query("DELETE FROM inboxes WHERE id = :inboxId")
    suspend fun delete(inboxId: Long)

    @Query("SELECT EXISTS(SELECT 1 FROM inboxes WHERE id = :inboxId)")
    suspend fun exists(inboxId: Long): Boolean
}
