package com.mesender.app.presentation.ui.thread

import com.mesender.app.domain.model.Item

data class ThreadListItem(
    val id: Long,
    val type: Type,
    val item: Item? = null,
    val dateLabel: String = ""
) {
    enum class Type { DATE_HEADER, NOTE }
}

fun buildThreadList(items: List<Item>): List<ThreadListItem> {
    val sdf = java.text.SimpleDateFormat(
        "d MMM yyyy", java.util.Locale.getDefault()
    )
    val grouped = items.groupBy { sdf.format(java.util.Date(it.createdAt)) }
    val result = mutableListOf<ThreadListItem>()
    val sortedDates = grouped.keys.sortedByDescending { sdf.parse(it)?.time ?: 0L }
    for (date in sortedDates) {
        val dateItems = grouped[date] ?: continue
        result.add(
            ThreadListItem(
                id = date.hashCode().toLong(),
                type = ThreadListItem.Type.DATE_HEADER,
                dateLabel = date
            )
        )
        result.addAll(dateItems.sortedByDescending { it.createdAt }.map {
            ThreadListItem(id = it.id, type = ThreadListItem.Type.NOTE, item = it)
        })
    }
    return result
}
