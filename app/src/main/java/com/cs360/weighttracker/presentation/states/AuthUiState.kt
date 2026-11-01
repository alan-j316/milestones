package com.cs360.weighttracker.presentation.states

import com.cs360.weighttracker.domain.status.LoginStatus
import com.cs360.weighttracker.domain.status.RegisterStatus

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val registerStatus: RegisterStatus? = null,
    val loginStatus: LoginStatus? = null,
)
