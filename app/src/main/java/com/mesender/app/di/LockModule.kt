package com.mesender.app.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.mesender.app.data.lock.DATA_STORE_NAME
import com.mesender.app.data.lock.LockManagerImpl
import com.mesender.app.data.lock.PinStoreImpl
import com.mesender.app.domain.lock.BiometricAuth
import com.mesender.app.domain.lock.LockManager
import com.mesender.app.domain.lock.PinStore
import com.mesender.app.presentation.ui.lock.BiometricAuthImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class LockModule {
    @Binds abstract fun bindPinStore(impl: PinStoreImpl): PinStore
    @Binds abstract fun bindLockManager(impl: LockManagerImpl): LockManager
    @Binds abstract fun bindBiometricAuth(impl: BiometricAuthImpl): BiometricAuth

    companion object {
        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            PreferenceDataStoreFactory.create {
                context.preferencesDataStoreFile(DATA_STORE_NAME)
            }
    }
}