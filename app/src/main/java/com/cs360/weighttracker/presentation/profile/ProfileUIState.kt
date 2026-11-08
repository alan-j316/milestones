package com.cs360.weighttracker.presentation.profile

sealed interface ProfileUIState {
    object IsLoading : ProfileUIState

    // Success states
    object UpdateNumberSuccess : ProfileUIState
    object UpdateSMSSettingSuccess : ProfileUIState
    object LogoutSuccess : ProfileUIState

    // Error states
    object UpdateNumberError : ProfileUIState
    object UpdateSMSSettingError : ProfileUIState
    object LogoutError : ProfileUIState
}