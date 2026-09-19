package com.mesender.app.domain.usecase.search

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

class SearchItemsInInbox(private val repository: ItemRepository) {
    operator fun invoke(inboxId: Long, rawQuery: String): Flow<List<Item>> {
        val cleaned = sanitizeQuery(rawQuery)
        return if (cleaned.isEmpty()) kotlinx.coroutines.flow.flowOf(emptyList())
        else repository.searchInInbox(inboxId, cleaned)
    }
}
