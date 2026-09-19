package com.mesender.app.domain.lock

import com.mesender.app.presentation.theme.AppTheme
import kotlinx.coroutines.flow.Flow

interface PinStore {
    suspend fun isPinSet(): Boolean
    suspend fun savePinHash(hash: String)
    suspend fun pinHash(): String?
    suspend fun clearPin()
    val isAppLockEnabled: Flow<Boolean>
    suspend fun setAppLockEnabled(enabled: Boolean)
    val appTheme: Flow<AppTheme>
    suspend fun setAppTheme(theme: AppTheme)
}