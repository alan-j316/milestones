package com.cs360.weighttracker.presentation.register

sealed interface RegisterUIState {
    object IsLoading : RegisterUIState
    object RegistrationSuccess : RegisterUIState
    object PasswordError : RegisterUIState
    object PasswordMismatchError : RegisterUIState
    object UsernameError : RegisterUIState
    object UserExistsError : RegisterUIState
    object UnknownError : RegisterUIState
}