package com.cs360.weighttracker.presentation.login


sealed interface LoginUIState {
    object IsLoading : LoginUIState
    object LogInSuccess : LoginUIState
    object PasswordError : LoginUIState
    object PasswordMismatchError : LoginUIState
    object UsernameError : LoginUIState
    object NoUserError : LoginUIState
    object UnknownError : LoginUIState
    object UserExistsError : LoginUIState

    object Logout : LoginUIState
}

