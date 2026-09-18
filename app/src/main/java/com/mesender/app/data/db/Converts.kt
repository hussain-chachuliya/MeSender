package com.mesender.app.data.db

import com.mesender.app.data.db.entity.InboxEntity
import com.mesender.app.data.db.entity.ItemEntity
import com.mesender.app.data.db.entity.ItemWithInbox
import com.mesender.app.domain.model.Inbox
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.model.ItemType
import com.mesender.app.domain.model.SearchResult

fun InboxEntity.toDomain() = Inbox(
    id = id,
    name = name,
    isLocked = isLocked,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ItemEntity.toDomain() = Item(
    id = id,
    inboxId = inboxId,
    type = ItemType.fromKey(type),
    textContent = text,
    title = title,
    mediaPath = mediaPath,
    mimeType = mimeType,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ItemWithInbox.toSearchResult() = SearchResult(
    item = item.toDomain(),
    inboxName = inbox.name,
    inboxLocked = inbox.isLocked
)
