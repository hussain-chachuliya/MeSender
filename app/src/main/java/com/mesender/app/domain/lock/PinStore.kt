package com.mesender.app.domain.lock

interface PinStore {
    suspend fun isPinSet(): Boolean
    suspend fun savePinHash(hash: String)
    suspend fun pinHash(): String?
    suspend fun clearPin()
}