package com.cs360.weighttracker.presentation.states

import com.cs360.weighttracker.domain.status.LoginStatus
import com.cs360.weighttracker.domain.status.RegisterStatus


sealed interface AuthUiState {
    object IsLoading : AuthUiState
    object LogInSuccess : AuthUiState
    object PasswordError : AuthUiState
    object PasswordMismatchError : AuthUiState
    object UsernameError : AuthUiState
    object NoUserError : AuthUiState
    object UnknownError : AuthUiState
    object UserExistsError : AuthUiState

    object Logout : AuthUiState
}

