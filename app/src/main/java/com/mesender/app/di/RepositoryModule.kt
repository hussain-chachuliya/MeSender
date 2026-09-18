package com.mesender.app.di

import com.mesender.app.data.repository.InboxRepositoryImpl
import com.mesender.app.data.repository.ItemRepositoryImpl
import com.mesender.app.domain.repository.InboxRepository
import com.mesender.app.domain.repository.ItemRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds abstract fun bindInboxRepository(impl: InboxRepositoryImpl): InboxRepository
    @Binds abstract fun bindItemRepository(impl: ItemRepositoryImpl): ItemRepository
}