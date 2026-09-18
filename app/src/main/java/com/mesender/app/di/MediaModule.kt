package com.mesender.app.di

import com.mesender.app.data.media.MediaFileRepository
import com.mesender.app.data.media.MediaFileRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class MediaModule {
    @Binds abstract fun bindMediaRepository(
        impl: MediaFileRepositoryImpl
    ): MediaFileRepository
}