package com.mesender.app.presentation.share

import android.content.ClipData
import android.content.Intent
import android.net.Uri
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class ShareIntentParserTest {

    @Test fun textShare_parsesAsText() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "just a note")
        }
        val parsed = ShareIntentParser.parse(intent)
        assertTrue(parsed is PendingShare.Text)
        assertEquals("just a note", (parsed as PendingShare.Text).body)
    }

    @Test fun urlShare_parsesAsLink() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/page")
        }
        val url = ShareIntentParser.parse(intent)
        assertTrue(url is PendingShare.Link)
        assertEquals("https://example.com/page", (url as PendingShare.Link).url)
    }

    @Test fun imageShare_parsesAsMedia() {
        val uri = Uri.parse("content://media/1")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val media = ShareIntentParser.parse(intent)
        assertTrue(media is PendingShare.Media)
        assertEquals(uri, (media as PendingShare.Media).uri)
        assertEquals("image/png", media.mimeType)
    }

    @Test fun emptyIntent_returnsNull() {
        val intent = Intent(Intent.ACTION_SEND)
        assertNull(ShareIntentParser.parse(intent))
    }

    @Test fun textShare_readsSubjectAsTitle() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "just a note")
            putExtra(Intent.EXTRA_SUBJECT, "A title")
        }
        val parsed = ShareIntentParser.parse(intent)
        assertEquals("A title", (parsed as PendingShare.Text).title)
    }

    @Test fun textShare_readsTitleWhenSubjectAbsent() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "just a note")
            putExtra(Intent.EXTRA_TITLE, "Title extra")
        }
        val parsed = ShareIntentParser.parse(intent)
        assertEquals("Title extra", (parsed as PendingShare.Text).title)
    }

    @Test fun videoShare_parsesAsMedia() {
        val uri = Uri.parse("content://media/video1")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val media = ShareIntentParser.parse(intent)
        assertTrue(media is PendingShare.Media)
        assertEquals("video/mp4", (media as PendingShare.Media).mimeType)
    }

    @Test fun imageShare_missingStream_returnsNull() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
        }
        assertNull(ShareIntentParser.parse(intent))
    }

    @Test fun uriList_parsesAsLinkFromText() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/uri-list"
            putExtra(Intent.EXTRA_TEXT, "https://example.com/page")
        }
        val link = ShareIntentParser.parse(intent)
        assertTrue(link is PendingShare.Link)
        assertEquals("https://example.com/page", (link as PendingShare.Link).url)
    }

    @Test fun uriList_parsesAsLinkFromStream() {
        val uri = Uri.parse("https://example.com/page")
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/uri-list"
            putExtra(Intent.EXTRA_STREAM, uri)
        }
        val link = ShareIntentParser.parse(intent)
        assertTrue(link is PendingShare.Link)
        assertEquals("https://example.com/page", (link as PendingShare.Link).url)
    }

    @Test fun multipleShare_withClipData_parsesEachItemPropagatingIntentType() {
        val uri1 = Uri.parse("content://media/1")
        val uri2 = Uri.parse("content://media/2")
        val clip = ClipData("photos", arrayOf("image/png"), ClipData.Item(uri1))
        clip.addItem(ClipData.Item(uri2))
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            clipData = clip
        }
        val multiple = ShareIntentParser.parse(intent) as? PendingShare.Multiple
        assertTrue(multiple != null)
        assertEquals(2, multiple!!.shares.size)
        assertEquals(uri1, (multiple.shares[0] as PendingShare.Media).uri)
        assertEquals(uri2, (multiple.shares[1] as PendingShare.Media).uri)
        assertEquals("image/*", (multiple.shares[0] as PendingShare.Media).mimeType)
        assertEquals("image/*", (multiple.shares[1] as PendingShare.Media).mimeType)
    }

    @Test fun multipleShare_withoutClipData_fallsBackToStreamExtra() {
        val uri = Uri.parse("content://media/3")
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayListOf(uri))
        }
        val multiple = ShareIntentParser.parse(intent) as? PendingShare.Multiple
        assertTrue(multiple != null)
        assertEquals(1, multiple!!.shares.size)
        assertEquals(uri, (multiple.shares[0] as PendingShare.Media).uri)
        assertEquals("image/*", (multiple.shares[0] as PendingShare.Media).mimeType)
    }

    @Test fun multipleShare_emptyStreams_returnsNull() {
        val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "image/*"
        }
        assertNull(ShareIntentParser.parse(intent))
    }

    @Test fun unknownType_returnsNull() {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/octet-stream"
            putExtra(Intent.EXTRA_TEXT, "binary")
        }
        assertNull(ShareIntentParser.parse(intent))
    }
}