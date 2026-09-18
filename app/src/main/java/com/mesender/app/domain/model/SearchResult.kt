package com.mesender.app.domain.model

data class SearchResult(
    val item: Item,
    val inboxName: String,
    val inboxLocked: Boolean
)
