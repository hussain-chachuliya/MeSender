package com.mesender.app.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "inboxes",
    indices = [Index("updatedAt")]
)
data class InboxEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(defaultValue = "0") val isLocked: Boolean = false,
    val createdAt: Long,
    val updatedAt: Long
)
