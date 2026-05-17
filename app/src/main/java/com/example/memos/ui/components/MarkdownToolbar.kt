package com.example.memos.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

@Composable
fun MarkdownToolbar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .horizontalScroll(scrollState)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FloatingButton(Icons.Default.FormatBold, "Bold") {
            wrapSelection(textFieldValue, onValueChange, "**", "**")
        }
        FloatingButton(Icons.Default.FormatItalic, "Italic") {
            wrapSelection(textFieldValue, onValueChange, "_", "_")
        }
        FloatingButton(Icons.Default.FormatStrikethrough, "Strikethrough") {
            wrapSelection(textFieldValue, onValueChange, "~~", "~~")
        }
        HeadingDropdown(textFieldValue, onValueChange)
        FloatingButton(Icons.Default.FormatListBulleted, "Bullet list") {
            prefixLine(textFieldValue, onValueChange, "- ")
        }
        FloatingButton(Icons.Default.FormatListNumbered, "Numbered list") {
            prefixLine(textFieldValue, onValueChange, "1. ")
        }
        FloatingButton(Icons.Default.Code, "Inline code") {
            wrapSelection(textFieldValue, onValueChange, "`", "`")
        }
        FloatingButton(Icons.Default.Terminal, "Code block") {
            insertCodeBlock(textFieldValue, onValueChange)
        }
        FloatingButton(Icons.Default.FormatQuote, "Quote") {
            prefixLine(textFieldValue, onValueChange, "> ")
        }
        FloatingButton(Icons.Default.Link, "Link") {
            insertLink(textFieldValue, onValueChange)
        }
        FloatingButton(Icons.AutoMirrored.Filled.Undo, "Undo", onClick = onUndo)
        FloatingButton(Icons.AutoMirrored.Filled.Redo, "Redo", onClick = onRedo)
    }
}

@Composable
private fun FloatingButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.size(40.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun HeadingDropdown(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        FloatingButton(Icons.Default.Title, "Heading") { expanded = true }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            (1..6).forEach { level ->
                DropdownMenuItem(
                    text = { Text("Heading $level") },
                    onClick = {
                        expanded = false
                        prefixLine(textFieldValue, onValueChange, "${"#".repeat(level)} ")
                    }
                )
            }
        }
    }
}

private fun wrapSelection(
    current: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    prefix: String,
    suffix: String
) {
    val text = current.text
    val start = min(current.selection.start, current.selection.end)
    val end = max(current.selection.start, current.selection.end)
    val selected = if (start <= end && start >= 0 && end <= text.length) {
        text.substring(start, end)
    } else ""

    val newText = text.substring(0, start) + prefix + selected + suffix + text.substring(end)
    val newCursor = if (selected.isEmpty()) {
        start + prefix.length
    } else {
        start + prefix.length + selected.length + suffix.length
    }

    onChange(TextFieldValue(newText, TextRange(newCursor)))
}

private fun prefixLine(
    current: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    prefix: String
) {
    val text = current.text
    val cursor = current.selection.start
    val lineStart = if (cursor > 0) text.lastIndexOf('\n', cursor - 1) + 1 else 0
    val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
    val newCursor = cursor + prefix.length
    onChange(TextFieldValue(newText, TextRange(newCursor)))
}

private fun insertCodeBlock(
    current: TextFieldValue,
    onChange: (TextFieldValue) -> Unit
) {
    val text = current.text
    val cursor = current.selection.start
    val insert = "\n```\n\n```\n"
    val newText = text.substring(0, cursor) + insert + text.substring(cursor)
    val newCursor = cursor + 5
    onChange(TextFieldValue(newText, TextRange(newCursor)))
}

private fun insertLink(
    current: TextFieldValue,
    onChange: (TextFieldValue) -> Unit
) {
    val text = current.text
    val start = min(current.selection.start, current.selection.end)
    val end = max(current.selection.start, current.selection.end)
    val selected = if (start <= end && start >= 0 && end <= text.length) {
        text.substring(start, end)
    } else ""

    val linkText = if (selected.isEmpty()) "text" else selected
    val insert = "[$linkText](url)"
    val newText = text.substring(0, start) + insert + text.substring(end)
    val newCursor = if (selected.isEmpty()) {
        start + 1
    } else {
        start + insert.length - 4
    }

    onChange(TextFieldValue(newText, TextRange(newCursor)))
}
