package com.mesender.app.presentation.share

import android.content.Intent
import android.net.Uri

sealed class PendingShare {
    data class Text(val body: String, val title: String?) : PendingShare()
    data class Link(val url: String, val title: String?) : PendingShare()
    data class Media(val uri: Uri, val mimeType: String, val title: String?) : PendingShare()
    data class Multiple(val shares: List<PendingShare>) : PendingShare()
}

object ShareIntentParser {

    fun parse(intent: Intent): PendingShare? {
        val type = intent.type ?: return null
        val isMultiple = intent.action == Intent.ACTION_SEND_MULTIPLE

        if (isMultiple) {
            val uris = extractStreamUris(intent)
            if (uris.isEmpty()) return null
            val mime = intent.type ?: "application/octet-stream"
            return PendingShare.Multiple(uris.map { PendingShare.Media(it, mime, null) })
        }

        val title = intent.getStringExtra(Intent.EXTRA_SUBJECT)
            ?: intent.getStringExtra(Intent.EXTRA_TITLE)

        return when {
            type.startsWith("image/") || type.startsWith("video/") -> {
                val uri = intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                    ?: return null
                PendingShare.Media(uri, type, title)
            }
            "text/plain" == type || "text/*" == type -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return null
                if (isLikelyUrl(text)) PendingShare.Link(text, title)
                else PendingShare.Text(text, title)
            }
            "text/uri-list" == type -> {
                val text = intent.getStringExtra(Intent.EXTRA_TEXT)
                    ?: intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.toString()
                if (text.isNullOrBlank()) null else PendingShare.Link(text, title)
            }
            else -> null
        }
    }

    private fun isLikelyUrl(text: String): Boolean =
        text.startsWith("http://") || text.startsWith("https://")

    private fun extractStreamUris(intent: Intent): List<Uri> {
        val fromClip = intent.clipData?.let { clip ->
            (0 until clip.itemCount).mapNotNull { i ->
                val item = clip.getItemAt(i)
                val uri = item.uri ?: return@mapNotNull null
                uri
            }
        }
        if (!fromClip.isNullOrEmpty()) return fromClip

        return intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
            ?.filterNotNull()
            ?: emptyList()
    }
}