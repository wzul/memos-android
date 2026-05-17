package com.example.memos.ui.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memos.data.repository.AuthRepository
import com.example.memos.data.security.BiometricHelper
import com.example.memos.data.security.EncryptedTokenStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val encryptedTokenStore: EncryptedTokenStore,
    private val biometricHelper: BiometricHelper
) : ViewModel() {

    var uiState by mutableStateOf(LoginUiState())
        private set

    val canUseBiometric: Boolean
        get() = biometricHelper.canAuthenticate()

    val isLoggedIn: Boolean
        get() = authRepository.isLoggedIn()

    fun login(instanceUrl: String, accessToken: String, enableBiometric: Boolean = false) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            val result = authRepository.signIn(instanceUrl, accessToken)
            uiState = if (result.isSuccess) {
                encryptedTokenStore.saveInstanceUrl(instanceUrl)
                encryptedTokenStore.saveAccessToken(accessToken)
                encryptedTokenStore.setBiometricEnabled(enableBiometric)
                uiState.copy(isLoading = false, isLoggedIn = true)
            } else {
                uiState.copy(
                    isLoading = false,
                    error = result.exceptionOrNull()?.message ?: "Login failed"
                )
            }
        }
    }

    fun dismissError() {
        uiState = uiState.copy(error = null)
    }
}

data class LoginUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)
