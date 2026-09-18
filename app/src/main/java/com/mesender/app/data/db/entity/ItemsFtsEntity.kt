package com.mesender.app.data.db.entity

import androidx.room.Entity
import androidx.room.Fts4
import androidx.room.FtsOptions

@Fts4(
    tokenizer = FtsOptions.TOKENIZER_UNICODE61,
    notIndexed = ["id"]
)
@Entity(tableName = "items_fts")
data class ItemsFtsEntity(
    val id: Long,
    val title: String?,
    val text: String?,
    val fileName: String?
)