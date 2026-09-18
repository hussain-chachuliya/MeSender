package com.mesender.app.domain.model

sealed class ItemType(val key: String) {
    data object Text : ItemType("TEXT")
    data object Photo : ItemType("PHOTO")
    data object Video : ItemType("VIDEO")
    data object Link : ItemType("LINK")

    companion object {
        fun fromKey(key: String): ItemType = when (key) {
            Text.key -> Text
            Photo.key -> Photo
            Video.key -> Video
            Link.key -> Link
            else -> throw IllegalArgumentException("Unknown ItemType key: $key")
        }
    }
}
