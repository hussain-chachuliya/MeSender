package com.mesender.app.data.db

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.mesender.app.data.db.entity.InboxEntity
import com.mesender.app.data.db.entity.ItemEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MeSenderDatabaseTest {

    private lateinit var db: MeSenderDatabase
    private val now = 1_700_000_000_000L

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, MeSenderDatabase::class.java)
            .allowMainThreadQueries()
            .addCallback(MeSenderDatabase.createFtsCallback)
            .build()
    }

    @Test
    fun inboxAndItemsCrudAndSearch_work() = runBlocking {
        val inboxId = db.inboxDao().insert(InboxEntity(name = "inbox", createdAt = now, updatedAt = now))
        val itemId = db.itemDao().insert(
            ItemEntity(
                inboxId = inboxId,
                type = "TEXT",
                text = "buy milk and bread",
                title = null,
                mediaPath = null,
                mimeType = null,
                createdAt = now,
                updatedAt = now
            )
        )

        val items = db.itemDao().observeItemsByInbox(inboxId).first()
        assertEquals(1, items.size)
        assertEquals("buy milk and bread", items[0].text)

        val results = db.itemDao().searchWithInbox("milk").first()
        assertTrue(results.isNotEmpty())
        assertEquals(itemId, results[0].item.id)
        assertEquals("inbox", results[0].inbox.name)

        db.itemDao().delete(itemId)
        assertTrue(db.itemDao().observeItemsByInbox(inboxId).first().isEmpty())
    }

    @Test
    fun deletingInbox_cascadesToItems() = runBlocking {
        val inboxId = db.inboxDao().insert(InboxEntity(name = "i", createdAt = now, updatedAt = now))
        db.itemDao().insert(
            ItemEntity(inboxId = inboxId, type = "TEXT", text = "x", title = null, mediaPath = null, mimeType = null, createdAt = now, updatedAt = now)
        )
        db.inboxDao().delete(inboxId)
        assertTrue(db.itemDao().observeItemsByInbox(inboxId).first().isEmpty())
    }
}
