package com.example.memos.ui.screens.memolist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memos.data.model.Memo
import com.example.memos.data.repository.MemoRepository
import com.example.memos.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoListViewModel @Inject constructor(
    private val memoRepository: MemoRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    var uiState by mutableStateOf(MemoListUiState())
        private set

    val memos: Flow<List<Memo>>
        get() = if (uiState.searchQuery.isBlank()) {
            memoRepository.observeMemos()
        } else {
            memoRepository.searchMemos(uiState.searchQuery)
        }

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            uiState = uiState.copy(isRefreshing = true)
            try {
                memoRepository.sync()
            } catch (_: Exception) {
                // offline — sync will retry when connectivity returns
            }
            uiState = uiState.copy(isRefreshing = false)
        }
    }

    fun onSearch(query: String) {
        uiState = uiState.copy(searchQuery = query)
    }

    fun togglePin(name: String) {
        viewModelScope.launch {
            val memo = memoRepository.getMemo(name) ?: return@launch
            memoRepository.updateMemo(name, null, null, !memo.pinned)
        }
    }

    fun deleteMemo(name: String) {
        viewModelScope.launch {
            memoRepository.deleteMemo(name)
        }
    }

    fun dismissDelete() {
        uiState = uiState.copy(showDeleteConfirm = false, memoToDelete = null)
    }

    fun confirmDelete() {
        val name = uiState.memoToDelete ?: return
        viewModelScope.launch {
            memoRepository.deleteMemo(name)
            uiState = uiState.copy(showDeleteConfirm = false, memoToDelete = null)
        }
    }

    fun requestDelete(name: String) {
        uiState = uiState.copy(showDeleteConfirm = true, memoToDelete = name)
    }

    val isOnline: Boolean
        get() = syncManager.isOnline()
}

data class MemoListUiState(
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val showDeleteConfirm: Boolean = false,
    val memoToDelete: String? = null
)
