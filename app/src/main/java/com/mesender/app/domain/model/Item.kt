package com.mesender.app.domain.model

data class Item(
    val id: Long,
    val inboxId: Long,
    val type: ItemType,
    val textContent: String?,
    val title: String?,
    val mediaPath: String?,
    val mimeType: String?,
    val createdAt: Long,
    val updatedAt: Long
) {
    val isMedia: Boolean get() = type == ItemType.Photo || type == ItemType.Video
    val isRetryableMedia: Boolean get() = isMedia && mediaPath == null
    val isEdited: Boolean get() = updatedAt > createdAt
}
