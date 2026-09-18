package com.mesender.app.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mesender.app.data.db.dao.InboxDao
import com.mesender.app.data.db.dao.ItemDao
import com.mesender.app.data.db.entity.InboxEntity
import com.mesender.app.data.db.entity.ItemEntity
import com.mesender.app.data.db.entity.ItemsFtsEntity

@Database(
    entities = [InboxEntity::class, ItemEntity::class, ItemsFtsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class MeSenderDatabase : RoomDatabase() {
    abstract fun inboxDao(): InboxDao
    abstract fun itemDao(): ItemDao

    companion object {
        val createFtsCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "CREATE VIRTUAL TABLE IF NOT EXISTS items_fts USING fts4(" +
                        "id UNINDEXED, title, text, fileName, tokenize = 'unicode61')"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS items_ai AFTER INSERT ON items BEGIN " +
                        "INSERT INTO items_fts(rowid, id, title, text, fileName) VALUES (" +
                        "new.id, new.id, COALESCE(new.title,''), COALESCE(new.text,''), " +
                        "COALESCE(new.mediaPath,'')); END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS items_ad AFTER DELETE ON items BEGIN " +
                        "DELETE FROM items_fts WHERE docid = old.id; END"
                )
                db.execSQL(
                    "CREATE TRIGGER IF NOT EXISTS items_au AFTER UPDATE ON items BEGIN " +
                        "DELETE FROM items_fts WHERE docid = old.id; " +
                        "INSERT INTO items_fts(rowid, id, title, text, fileName) VALUES (" +
                        "new.id, new.id, COALESCE(new.title,''), COALESCE(new.text,''), " +
                        "COALESCE(new.mediaPath,'')); END"
                )
            }
        }
    }
}
