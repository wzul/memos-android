package com.example.memos.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp

fun String.toMarkdownAnnotatedString(): AnnotatedString {
    return buildAnnotatedString {
        val lines = this@toMarkdownAnnotatedString.split("\n")
        lines.forEachIndexed { index, line ->
            if (index > 0) append("\n")
            parseLine(line)
        }
    }
}

private fun AnnotatedString.Builder.parseLine(line: String) {
    // Heading: # to ######
    val headingMatch = Regex("^(#{1,6})\\s+(.*)$").find(line)
    if (headingMatch != null) {
        val level = headingMatch.groupValues[1].length
        val content = headingMatch.groupValues[2]
        val size = when (level) {
            1 -> 28.sp
            2 -> 24.sp
            3 -> 20.sp
            4 -> 18.sp
            5 -> 16.sp
            else -> 14.sp
        }
        withStyle(
            SpanStyle(
                fontSize = size,
                fontWeight = FontWeight.Bold
            )
        ) {
            append(content)
        }
        return
    }

    // Quote: > text
    val quoteMatch = Regex("^>\\s+(.*)$").find(line)
    if (quoteMatch != null) {
        withStyle(
            SpanStyle(
                color = Color(0xFF666666),
                fontStyle = FontStyle.Italic
            )
        ) {
            appendInline(quoteMatch.groupValues[1])
        }
        return
    }

    appendInline(line)
}

private fun AnnotatedString.Builder.appendInline(text: String) {
    var cursor = 0
    val patterns = listOf(
        Triple(Regex("\\*\\*(.+?)\\*\\*"), SpanStyle(fontWeight = FontWeight.Bold), 2),
        Triple(Regex("_(.+?)_"), SpanStyle(fontStyle = FontStyle.Italic), 1),
        Triple(Regex("~~(.+?)~~"), SpanStyle(textDecoration = TextDecoration.LineThrough), 2),
        Triple(Regex("`(.+?)`"), SpanStyle(fontFamily = FontFamily.Monospace, color = Color(0xFFCC3300)), 1),
        Triple(Regex("\\[(.+?)]\\((.+?)\\)"), SpanStyle(color = Color(0xFF0066CC), textDecoration = TextDecoration.Underline), 0)
    )

    while (cursor < text.length) {
        var nearestMatch: MatchResult? = null
        var nearestStyle: SpanStyle? = null
        var nearestOffset = 0

        for ((regex, style, offset) in patterns) {
            val match = regex.find(text, cursor) ?: continue
            if (nearestMatch == null || match.range.first < nearestMatch.range.first) {
                nearestMatch = match
                nearestStyle = style
                nearestOffset = offset
            }
        }

        if (nearestMatch == null) {
            append(text.substring(cursor))
            break
        }

        if (nearestMatch.range.first > cursor) {
            append(text.substring(cursor, nearestMatch.range.first))
        }

        val contentIndex = if (nearestMatch.groupValues.size > 1) 1 else 0
        withStyle(nearestStyle!!) {
            append(nearestMatch.groupValues[contentIndex])
        }

        cursor = nearestMatch.range.last + 1 + nearestOffset
    }
}
