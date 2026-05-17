package com.example.memos.ui.screens.memoedit

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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memos.data.model.Visibility
import com.example.memos.ui.components.MarkdownToolbar
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
    var isEditorFocused by remember { mutableStateOf(false) }

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
                .padding(
                    horizontal = if (isEditorFocused) 0.dp else 16.dp,
                    vertical = if (isEditorFocused) 0.dp else 16.dp
                )
                .imePadding()
        ) {
            AnimatedVisibility(
                visible = !isEditorFocused,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
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

                    if (uiState.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            uiState.tags.forEach { tag ->
                                FilterChip(
                                    selected = true,
                                    onClick = {},
                                    label = { Text("#$tag") },
                                    enabled = false
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

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
                    .weight(1f)
                    .onFocusChanged { isEditorFocused = it.isFocused },
                placeholder = { Text("Write in Markdown...") }
            )

            if (uiState.attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                AttachmentsSection(attachments = uiState.attachments)
            }

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
private fun AttachmentsSection(
    attachments: List<com.example.memos.data.model.Attachment>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
    ) {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AttachFile,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Attachments (${attachments.size})",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        attachments.forEach { attachment ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = attachment.filename,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = listOfNotNull(
                        attachment.type?.substringAfterLast('/')?.uppercase(),
                        attachment.size?.let { formatBytes(it) }
                    ).joinToString(" · "),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatBytes(size: Long): String {
    return when {
        size >= 1_048_576 -> "%.1f MB".format(size / 1_048_576.0)
        size >= 1024 -> "%.1f KB".format(size / 1024.0)
        else -> "$size B"
    }
}
