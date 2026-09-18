package com.mesender.app.domain.usecase.item

import android.net.Uri
import com.mesender.app.domain.model.Item
import com.mesender.app.domain.repository.ItemRepository

data class MediaShare(
    val sourceUri: Uri,
    val mimeType: String,
    val title: String? = null
)

class ShareMediaItem(private val repository: ItemRepository) {
    suspend operator fun invoke(inboxId: Long, share: MediaShare): Item =
        repository.insertMediaItem(inboxId, share.sourceUri, share.mimeType, share.title)
}