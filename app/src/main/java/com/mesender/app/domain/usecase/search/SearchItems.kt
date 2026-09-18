package com.mesender.app.domain.usecase.search

import com.mesender.app.domain.model.SearchResult
import com.mesender.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

class SearchItems(private val repository: ItemRepository) {
    operator fun invoke(rawQuery: String): Flow<List<SearchResult>> {
        val cleaned = sanitizeQuery(rawQuery)
        return if (cleaned.isEmpty()) kotlinx.coroutines.flow.flowOf(emptyList())
        else repository.search(cleaned)
    }
}