package com.mesender.app.di

import android.content.Context
import androidx.room.Room
import com.mesender.app.data.db.MeSenderDatabase
import com.mesender.app.data.db.dao.InboxDao
import com.mesender.app.data.db.dao.ItemDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MeSenderDatabase =
        Room.databaseBuilder(context, MeSenderDatabase::class.java, "mesender.db")
            .addCallback(MeSenderDatabase.createFtsCallback)
            .build()

    @Provides
    fun provideInboxDao(db: MeSenderDatabase): InboxDao = db.inboxDao()

    @Provides
    fun provideItemDao(db: MeSenderDatabase): ItemDao = db.itemDao()
}