package com.cs360.weighttracker.presentation.login


sealed interface LoginUIState {
    object IsLoading : LoginUIState
    object LogInSuccess : LoginUIState
    object GoalNotSet : LoginUIState
    object PasswordError : LoginUIState
    object UsernameError : LoginUIState
    object UnknownError : LoginUIState
}

