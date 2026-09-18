package com.mesender.app.data.media

import android.net.Uri

interface MediaFileRepository {
    suspend fun copyMediaToInternal(inboxId: Long, sourceUri: Uri): String?
    suspend fun deleteMediaFile(path: String): Boolean
    suspend fun deleteMediaForInbox(inboxId: Long)
}