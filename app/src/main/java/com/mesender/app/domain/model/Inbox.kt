package com.mesender.app.domain.model

data class Inbox(
    val id: Long,
    val name: String,
    val isLocked: Boolean,
    val createdAt: Long,
    val updatedAt: Long
)
