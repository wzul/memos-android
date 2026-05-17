package com.example.memos.ui.screens.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memos.data.repository.AuthRepository
import com.example.memos.data.repository.MemoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val memoRepository: MemoRepository
) : ViewModel() {

    var uiState by mutableStateOf(SettingsUiState())
        private set

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            memoRepository.clearLocalData()
            uiState = uiState.copy(signedOut = true)
        }
    }
}

data class SettingsUiState(
    val signedOut: Boolean = false
)
