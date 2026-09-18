package com.mesender.app.domain.usecase.search

/**
 * Sanitizes a raw query into a safe FTS literal phrase query (
 * neutralizes double quotes so arbitrary user text cannot break MATCH
 * on FTS4, which has no quote-escape mechanism).
 */
fun sanitizeQuery(raw: String): String {
    val cleaned = raw.trim().replace("\"", " ")
    return if (cleaned.isEmpty()) "" else "\"$cleaned*\""
}