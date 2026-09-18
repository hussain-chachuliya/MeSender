package com.mesender.app.domain.usecase.search

/**
 * Sanitizes a raw query into a safe FTS5 prefix MatchQuery (
 * escapes double quotes so arbitrary user text cannot break MATCH).
 */
fun sanitizeQuery(raw: String): String {
    val escaped = raw.trim().replace("\"", "\"\"")
    return if (escaped.isEmpty()) "" else "\"$escaped*\""
}