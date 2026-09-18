package com.mesender.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "items",
    foreignKeys = [
        ForeignKey(
            entity = InboxEntity::class,
            parentColumns = ["id"],
            childColumns = ["inboxId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("inboxId"), Index("inboxId", "createdAt")]
)
data class ItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val inboxId: Long,
    val type: String,
    val text: String?,
    val title: String?,
    val mediaPath: String?,
    val mimeType: String?,
    @ColumnInfo(defaultValue = "0") val needsMediaRetry: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
