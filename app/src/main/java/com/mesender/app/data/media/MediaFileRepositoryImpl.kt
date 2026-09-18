package com.mesender.app.data.media

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import javax.inject.Inject

class MediaFileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : MediaFileRepository {

    override suspend fun copyMediaToInternal(
        inboxId: Long,
        sourceUri: Uri
    ): String? = withContext(Dispatchers.IO) {
        try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(sourceUri) ?: "application/octet-stream"
            val extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType) ?: "bin"
            val mediaDir = File(context.filesDir, "media/$inboxId").also { it.mkdirs() }
            val filename = "${UUID.randomUUID()}.$extension"
            val dest = File(mediaDir, filename)
            resolver.openInputStream(sourceUri)?.use { input ->
                dest.outputStream().use { output -> input.copyTo(output) }
            } ?: return@withContext null
            dest.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun deleteMediaFile(path: String): Boolean = withContext(Dispatchers.IO) {
        try {
            File(path).delete()
        } catch (_: Exception) {
            false
        }
    }

    override suspend fun deleteMediaForInbox(inboxId: Long) = withContext(Dispatchers.IO) {
        val dir = File(context.filesDir, "media/$inboxId")
        if (dir.exists()) dir.deleteRecursively()
    }
}