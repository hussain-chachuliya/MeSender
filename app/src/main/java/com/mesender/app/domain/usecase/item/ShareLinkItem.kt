package com.mesender.app.domain.usecase.item

import com.mesender.app.domain.model.Item
import com.mesender.app.domain.repository.ItemRepository

class ShareLinkItem(private val repository: ItemRepository) {
    suspend operator fun invoke(inboxId: Long, url: String, title: String?): Item =
        repository.insertLinkItem(inboxId, url, title)
}