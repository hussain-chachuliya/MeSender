package com.mesender.app.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class ItemWithInbox(
    @Embedded val item: ItemEntity,
    @Relation(
        parentColumn = "inboxId",
        entityColumn = "id"
    )
    val inbox: InboxEntity
)
