package com.example.memos.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(horizontal = 8.dp)
            .height(48.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Headings
        TextActionButton("H1") {
            prefixLine(textFieldValue, onValueChange, "# ")
        }
        TextActionButton("H2") {
            prefixLine(textFieldValue, onValueChange, "## ")
        }

        ThinDivider()

        // Style dropdown (Aa)
        StyleDropdown(textFieldValue, onValueChange)

        ThinDivider()

        // Basic formatting
        TextActionButton("B", fontWeight = FontWeight.Bold) {
            wrapSelection(textFieldValue, onValueChange, "**", "**")
        }
        TextActionButton("I", fontStyle = FontStyle.Italic) {
            wrapSelection(textFieldValue, onValueChange, "_", "_")
        }
        TextActionButton("U", textDecoration = TextDecoration.Underline) {
            wrapSelection(textFieldValue, onValueChange, "<u>", "</u>")
        }
        TextActionButton("S", textDecoration = TextDecoration.LineThrough) {
            wrapSelection(textFieldValue, onValueChange, "~~", "~~")
        }

        ThinDivider()

        // Lists
        TextActionButton("•") {
            prefixLine(textFieldValue, onValueChange, "- ")
        }
        TextActionButton("1.") {
            prefixLine(textFieldValue, onValueChange, "1. ")
        }

        ThinDivider()

        // Code & quote
        TextActionButton("` ") {
            wrapSelection(textFieldValue, onValueChange, "`", "`")
        }
        TextActionButton("\"") {
            prefixLine(textFieldValue, onValueChange, "> ")
        }

        ThinDivider()

        // Undo / Redo
        TextActionButton("↶", enabled = false, onClick = onUndo)
        TextActionButton("↷", enabled = false, onClick = onRedo)
    }
}

@Composable
private fun ThinDivider() {
    VerticalDivider(
        modifier = Modifier
            .height(24.dp)
            .padding(horizontal = 4.dp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
    )
}

@Composable
private fun TextActionButton(
    label: String,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .height(40.dp)
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = textDecoration,
            color = if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            }
        )
    }
}

@Composable
private fun StyleDropdown(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .padding(horizontal = 2.dp)
                .height(40.dp)
        ) {
            Text(
                text = "Aa",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Heading 1") },
                onClick = {
                    expanded = false
                    prefixLine(textFieldValue, onValueChange, "# ")
                }
            )
            DropdownMenuItem(
                text = { Text("Heading 2") },
                onClick = {
                    expanded = false
                    prefixLine(textFieldValue, onValueChange, "## ")
                }
            )
            DropdownMenuItem(
                text = { Text("Heading 3") },
                onClick = {
                    expanded = false
                    prefixLine(textFieldValue, onValueChange, "### ")
                }
            )
            DropdownMenuItem(
                text = { Text("Heading 4") },
                onClick = {
                    expanded = false
                    prefixLine(textFieldValue, onValueChange, "#### ")
                }
            )
            DropdownMenuItem(
                text = { Text("Heading 5") },
                onClick = {
                    expanded = false
                    prefixLine(textFieldValue, onValueChange, "##### ")
                }
            )
            DropdownMenuItem(
                text = { Text("Heading 6") },
                onClick = {
                    expanded = false
                    prefixLine(textFieldValue, onValueChange, "###### ")
                }
            )
            DropdownMenuItem(
                text = { Text("Normal") },
                onClick = {
                    expanded = false
                    // Remove heading prefix from current line
                    removeHeadingPrefix(textFieldValue, onValueChange)
                }
            )
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

private fun removeHeadingPrefix(
    current: TextFieldValue,
    onChange: (TextFieldValue) -> Unit
) {
    val text = current.text
    val cursor = current.selection.start
    val lineStart = if (cursor > 0) text.lastIndexOf('\n', cursor - 1) + 1 else 0
    val lineEnd = text.indexOf('\n', cursor).takeIf { it >= 0 } ?: text.length
    val line = text.substring(lineStart, lineEnd)

    val headingRegex = "^#{1,6}\\s+(.*)$".toRegex()
    val newLine = headingRegex.replace(line, "$1")

    val newText = text.substring(0, lineStart) + newLine + text.substring(lineEnd)
    val newCursor = lineStart + newLine.length
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
