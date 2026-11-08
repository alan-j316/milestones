package com.cs360.weighttracker.presentation.profile

import com.cs360.weighttracker.domain.models.User

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