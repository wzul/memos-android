package com.example.memos.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagInputField(
    tags: List<String>,
    onTagsChanged: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var tagInput by remember { mutableStateOf("") }

    Column(modifier = modifier) {
        OutlinedTextField(
            value = tagInput,
            onValueChange = { value ->
                if (value.endsWith(" ") || value.endsWith(",")) {
                    val newTag = value.trim().removeSuffix(",").lowercase()
                    if (newTag.isNotBlank() && !tags.contains(newTag)) {
                        onTagsChanged(tags + newTag)
                    }
                    tagInput = ""
                } else {
                    tagInput = value
                }
            },
            label = { Text("Tags") },
            placeholder = { Text("Type a tag and press space or comma") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (tags.isNotEmpty()) {
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                tags.forEach { tag ->
                    InputChip(
                        selected = false,
                        onClick = {},
                        label = { Text("#$tag") },
                        trailingIcon = {
                            IconButton(
                                onClick = { onTagsChanged(tags - tag) },
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Remove $tag",
                                    modifier = Modifier.padding(2.dp)
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
