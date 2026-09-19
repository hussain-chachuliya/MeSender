package com.mesender.app.presentation.ui.thread

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration

fun formatWhatsAppText(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < text.length) {
            val start = length
            when {
                text.startsWith("```", i) -> {
                    val end = text.indexOf("```", i + 3)
                    if (end != -1) {
                        append(text.substring(i + 3, end))
                        addStyle(SpanStyle(fontFamily = FontFamily.Monospace), start, length)
                        i = end + 3
                    } else {
                        append(text.substring(i))
                        i = text.length
                    }
                }
                text.startsWith("*", i) && i + 1 < text.length && text[i + 1] != '*' -> {
                    val end = text.indexOf("*", i + 1)
                    if (end != -1) {
                        append(text.substring(i + 1, end))
                        addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, length)
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("_", i) && i + 1 < text.length && text[i + 1] != '_' -> {
                    val end = text.indexOf("_", i + 1)
                    if (end != -1) {
                        append(text.substring(i + 1, end))
                        addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("~", i) && i + 1 < text.length -> {
                    val end = text.indexOf("~", i + 1)
                    if (end != -1) {
                        append(text.substring(i + 1, end))
                        addStyle(SpanStyle(textDecoration = TextDecoration.LineThrough), start, length)
                        i = end + 1
                    } else {
                        append(text[i])
                        i++
                    }
                }
                text.startsWith("> ", i) -> {
                    val lineEnd = text.indexOf('\n', i)
                    val end = if (lineEnd != -1) lineEnd else text.length
                    append(text.substring(i, end))
                    addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, length)
                    i = end
                }
                else -> {
                    append(text[i])
                    i++
                }
            }
        }
    }
}
