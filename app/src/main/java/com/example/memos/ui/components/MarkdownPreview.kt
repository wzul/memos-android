package com.example.memos.ui.components

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import io.noties.markwon.Markwon

@Composable
fun MarkdownPreview(
    content: String,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            TextView(context).apply {
                val markwon = Markwon.create(context)
                markwon.setMarkdown(this, content)
            }
        },
        update = { view ->
            val markwon = Markwon.create(view.context)
            markwon.setMarkdown(view, content)
        }
    )
}
