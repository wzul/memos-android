package com.example.memos.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.example.memos.R
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MemosWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                MemosWidgetContent()
            }
        }
    }
}

@Composable
private fun MemosWidgetContent() {
    val memosJson = "[]"
    val memos: List<WidgetMemo> = try {
        Gson().fromJson(memosJson, object : TypeToken<List<WidgetMemo>>() {}.type)
    } catch (_: Exception) {
        emptyList()
    }

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.surface)
            .padding(16)
    ) {
        Text(
            text = "Recent Notes",
            style = TextStyle(
                color = ColorProvider(R.color.purple_500)
            )
        )
        Spacer(GlanceModifier.height(8))

        if (memos.isEmpty()) {
            Text(
                text = "No recent notes",
                style = TextStyle(
                    color = ColorProvider(R.color.purple_200)
                )
            )
        } else {
            LazyColumn {
                items(memos.take(5)) { memo ->
                    Column(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .padding(vertical = 4)
                    ) {
                        Text(
                            text = memo.content.take(60).replace("\n", " ") +
                                    if (memo.content.length > 60) "…" else "",
                            style = TextStyle(
                                color = ColorProvider(R.color.black)
                            ),
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

data class WidgetMemo(
    val name: String,
    val content: String,
    val pinned: Boolean
)

class MemosWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MemosWidget()
}
