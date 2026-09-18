package com.mesender.app.data.media

import android.content.Context
import android.net.Uri
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

class MediaFileRepositoryImplTest {

    private lateinit var repo: MediaFileRepositoryImpl
    private lateinit var filesDir: File

    @Before
    fun setup() {
        val context = mockk<Context>(relaxed = true)
        filesDir = File(System.getProperty("java.io.tmpdir"), "mesender_test_${System.nanoTime()}")
        filesDir.mkdirs()
        io.mockk.every { context.filesDir } returns filesDir
        repo = MediaFileRepositoryImpl(context)
    }

    @Test
    fun deleteMediaForInbox_removesDirectory() = runTest {
        val inboxDir = File(filesDir, "media/1").also { it.mkdirs() }
        File(inboxDir, "pic.jpg").createNewFile()
        assertTrue(File(inboxDir, "pic.jpg").exists())
        repo.deleteMediaForInbox(1)
        assertTrue(!inboxDir.exists())
    }

    @Test
    fun deleteMediaFile_returnsTrueOnSuccess() = runTest {
        val tmpFile = File(filesDir, "test.jpg")
        tmpFile.writeText("x")
        val result = repo.deleteMediaFile(tmpFile.absolutePath)
        assertTrue(result)
        assertTrue(!tmpFile.exists())
    }
}