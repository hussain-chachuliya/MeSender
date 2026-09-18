package com.mesender.app.domain.usecase.item

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.repository.ItemRepository

class DeleteItem(private val repository: ItemRepository) {
    suspend operator fun invoke(item: Item): Boolean = repository.deleteItem(item)
}