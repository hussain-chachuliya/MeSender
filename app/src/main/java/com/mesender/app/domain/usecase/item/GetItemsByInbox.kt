package com.mesender.app.domain.usecase.item

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.repository.ItemRepository
import kotlinx.coroutines.flow.Flow

class GetItemsByInbox(private val repository: ItemRepository) {
    operator fun invoke(inboxId: Long): Flow<List<Item>> = repository.observeItemsByInbox(inboxId)
}