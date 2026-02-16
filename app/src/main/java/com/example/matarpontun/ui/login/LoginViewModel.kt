package com.example.matarpontun.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.matarpontun.domain.model.Ward
import com.example.matarpontun.domain.service.WardService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class LoginViewModel(
    private val wardService: WardService
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState

    fun login(wardName: String, password: String) {

        _uiState.value = LoginUiState.Loading

        viewModelScope.launch {
            val result = wardService.login(wardName, password)

            _uiState.value = result.fold(
                onSuccess = { ward ->
                    LoginUiState.Success(ward)
                },
                onFailure = { error ->
                    LoginUiState.Error(
                        error.message ?: "Unknown error"
                    )
                }
            )
        }
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    data class Success(val ward: Ward) : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}

