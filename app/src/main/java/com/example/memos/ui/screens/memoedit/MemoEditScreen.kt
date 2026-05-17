package com.example.memos.ui.screens.memoedit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memos.data.model.Visibility
import com.example.memos.ui.components.MarkdownToolbar
import com.example.memos.ui.components.TagInputField
import com.example.memos.ui.components.toMarkdownAnnotatedString

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MemoEditScreen(
    memoName: String?,
    viewModel: MemoEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState = viewModel.uiState
    var textFieldValue by remember { mutableStateOf(TextFieldValue(uiState.content)) }

    val displayValue = remember(textFieldValue) {
        TextFieldValue(
            annotatedString = textFieldValue.text.toMarkdownAnnotatedString(),
            selection = textFieldValue.selection,
            composition = textFieldValue.composition
        )
    }

    LaunchedEffect(uiState.saved) {
        if (uiState.saved) onNavigateBack()
    }

    LaunchedEffect(uiState.content) {
        if (textFieldValue.text != uiState.content) {
            textFieldValue = TextFieldValue(uiState.content)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (memoName == null) "New Note" else "Edit Note",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.save() },
                        enabled = !uiState.isSaving && uiState.content.isNotBlank()
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(Icons.Default.Done, contentDescription = "Save")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .imePadding()
        ) {
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Visibility.entries.forEach { visibility ->
                    FilterChip(
                        selected = uiState.visibility == visibility,
                        onClick = { viewModel.setVisibility(visibility) },
                        label = { Text(visibility.name.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
                FilterChip(
                    selected = uiState.pinned,
                    onClick = { viewModel.togglePinned() },
                    label = { Text("Pin") },
                    leadingIcon = {
                        if (uiState.pinned) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = null,
                                modifier = Modifier.height(18.dp)
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TagInputField(
                tags = uiState.tags,
                onTagsChanged = viewModel::setTags,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = displayValue,
                onValueChange = {
                    textFieldValue = TextFieldValue(
                        text = it.text,
                        selection = it.selection,
                        composition = it.composition
                    )
                    viewModel.setContent(it.text)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                placeholder = { Text("Write in Markdown...") }
            )

            SelectionToolbar(
                textFieldValue = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    viewModel.setContent(it.text)
                }
            )

            MarkdownToolbar(
                textFieldValue = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    viewModel.setContent(it.text)
                },
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.error != null) {
            AlertDialog(
                onDismissRequest = { viewModel.dismissError() },
                title = { Text("Error") },
                text = { Text(uiState.error!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.dismissError() }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
private fun SelectionToolbar(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    val hasSelection = textFieldValue.selection.start != textFieldValue.selection.end
    AnimatedVisibility(
        visible = hasSelection,
        enter = expandVertically(expandFrom = Alignment.Bottom),
        exit = shrinkVertically(shrinkTowards = Alignment.Bottom)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            shape = MaterialTheme.shapes.medium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 4.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FormatAction("H1") {
                    prefixLine(textFieldValue, onValueChange, "# ")
                }
                FormatAction("H2") {
                    prefixLine(textFieldValue, onValueChange, "## ")
                }
                FormatAction("B", fontWeight = FontWeight.Bold) {
                    wrapSelection(textFieldValue, onValueChange, "**", "**")
                }
                FormatAction("I", fontStyle = FontStyle.Italic) {
                    wrapSelection(textFieldValue, onValueChange, "_", "_")
                }
                FormatAction("U", textDecoration = TextDecoration.Underline) {
                    wrapSelection(textFieldValue, onValueChange, "<u>", "</u>")
                }
                FormatAction("S", textDecoration = TextDecoration.LineThrough) {
                    wrapSelection(textFieldValue, onValueChange, "~~", "~~")
                }
                FormatAction("`") {
                    wrapSelection(textFieldValue, onValueChange, "`", "`")
                }
            }
        }
    }
}

@Composable
private fun FormatAction(
    label: String,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textDecoration: TextDecoration? = null,
    onClick: () -> Unit
) {
    TextButton(onClick = onClick, modifier = Modifier.padding(horizontal = 2.dp)) {
        Text(
            text = label,
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            textDecoration = textDecoration,
            style = MaterialTheme.typography.labelLarge
        )
    }
}

private fun wrapSelection(
    current: TextFieldValue,
    onChange: (TextFieldValue) -> Unit,
    prefix: String,
    suffix: String
) {
    val text = current.text
    val start = kotlin.math.min(current.selection.start, current.selection.end)
    val end = kotlin.math.max(current.selection.start, current.selection.end)
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
