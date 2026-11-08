package com.cs360.weighttracker.presentation.profile

sealed interface ProfileActions {
    object Logout : ProfileActions
    data class UpdateSMSSetting(val setting: Boolean) : ProfileActions
    data class UpdatePhone(val phoneNumber: String) : ProfileActions
}