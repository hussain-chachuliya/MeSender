package com.mesender.app.domain.usecase.item

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.repository.ItemRepository

class ComposeTextItem(private val repository: ItemRepository) {
    suspend operator fun invoke(inboxId: Long, text: String): Item =
        repository.insertTextItem(inboxId, text)
}