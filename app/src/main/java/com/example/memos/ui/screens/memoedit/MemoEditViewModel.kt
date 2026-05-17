package com.example.memos.ui.screens.memoedit

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memos.data.model.Attachment
import com.example.memos.data.model.Memo
import com.example.memos.data.model.Visibility
import com.example.memos.data.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val memoRepository: MemoRepository
) : ViewModel() {

    private val memoName: String? = savedStateHandle.get<String>("name")

    var uiState by mutableStateOf(MemoEditUiState())
        private set

    private val history = mutableListOf<String>()
    private var historyIndex = -1
    private var lastSavedContent = ""

    init {
        if (memoName != null && memoName != "new") {
            loadMemo(memoName)
        }
    }

    private fun loadMemo(name: String) {
        viewModelScope.launch {
            val memo = memoRepository.getMemo(name)
            if (memo != null) {
                uiState = uiState.copy(
                    content = memo.content,
                    visibility = memo.visibility,
                    pinned = memo.pinned,
                    tags = memo.tags,
                    attachments = memo.attachments,
                    isLoading = false
                )
                pushHistory(memo.content)
            }
            // Refresh from server to get latest attachments — silently ignore network errors
            try {
                memoRepository.refreshMemo(name).onSuccess { refreshed ->
                    uiState = uiState.copy(
                        content = refreshed.content,
                        visibility = refreshed.visibility,
                        pinned = refreshed.pinned,
                        tags = refreshed.tags,
                        attachments = refreshed.attachments
                    )
                }
            } catch (_: Exception) {
                // offline — local data is sufficient
            }
        }
    }

    fun setTags(tags: List<String>) {
        uiState = uiState.copy(tags = tags)
    }

    fun setContent(content: String) {
        if (content != uiState.content) {
            pushHistory(content)
            uiState = uiState.copy(content = content)
        }
    }

    fun setVisibility(visibility: Visibility) {
        uiState = uiState.copy(visibility = visibility)
    }

    fun togglePinned() {
        uiState = uiState.copy(pinned = !uiState.pinned)
    }

    fun undo() {
        if (historyIndex > 0) {
            historyIndex--
            val previous = history[historyIndex]
            uiState = uiState.copy(content = previous)
        }
    }

    fun redo() {
        if (historyIndex < history.size - 1) {
            historyIndex++
            val next = history[historyIndex]
            uiState = uiState.copy(content = next)
        }
    }

    val canUndo: Boolean
        get() = historyIndex > 0

    val canRedo: Boolean
        get() = historyIndex < history.size - 1

    private fun pushHistory(content: String) {
        if (historyIndex < history.size - 1) {
            history.subList(historyIndex + 1, history.size).clear()
        }
        if (history.isEmpty() || history.last() != content) {
            history.add(content)
            historyIndex = history.size - 1
            if (history.size > 50) {
                history.removeAt(0)
                historyIndex--
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            uiState = uiState.copy(isSaving = true)
            val result = if (memoName == null || memoName == "new") {
                memoRepository.createMemo(uiState.content, uiState.visibility)
            } else {
                memoRepository.updateMemo(
                    memoName,
                    uiState.content,
                    uiState.visibility,
                    uiState.pinned
                )
            }
            uiState = if (result.isSuccess) {
                uiState.copy(isSaving = false, saved = true)
            } else {
                uiState.copy(
                    isSaving = false,
                    error = result.exceptionOrNull()?.message ?: "Failed to save"
                )
            }
        }
    }

    fun dismissError() {
        uiState = uiState.copy(error = null)
    }
}

data class MemoEditUiState(
    val content: String = "",
    val visibility: Visibility = Visibility.PRIVATE,
    val pinned: Boolean = false,
    val tags: List<String> = emptyList(),
    val attachments: List<Attachment> = emptyList(),
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val saved: Boolean = false,
    val error: String? = null
)
