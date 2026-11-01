package com.cs360.weighttracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.status.LoginStatus
import com.cs360.weighttracker.domain.status.RegisterStatus
import com.cs360.weighttracker.presentation.actions.AuthActions
import com.cs360.weighttracker.presentation.states.AuthUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MilestonesViewModel(private val repository: IMilestoneRepository) : ViewModel() {


    private val _authUIState = MutableStateFlow(AuthUiState())
    val authUiState: StateFlow<AuthUiState> = _authUIState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            _authUIState.update { state ->
                state.copy(
                    isLoading = false,
                    loginStatus = null,
                    registerStatus = null,
                    isLoggedIn = currentUser != null
                )
            }
        }
    }

    // Collect Backend State?

    fun onAction(action: AuthActions) {
        when (action) {
            is AuthActions.Login -> {
                viewModelScope.launch {
                    _authUIState.update { state ->
                        state.copy(
                            registerStatus = null,
                            loginStatus = null,
                            isLoading = true,
                            isLoggedIn = false
                        )
                    }
                    val result = repository.loginUser(action.username, action.password)
                    _authUIState.update { state ->
                        state.copy(
                            isLoading = false,
                            loginStatus = result,
                            isLoggedIn = result == LoginStatus.SUCCESS
                        )
                    }
                }
            }

            is AuthActions.Register -> {
                viewModelScope.launch {
                    _authUIState.update { state ->
                        state.copy(
                            registerStatus = null,
                            loginStatus = null,
                            isLoading = true,
                            isLoggedIn = false
                        )
                    }
                    val result = repository.registerUser(action.username, action.password)
                    _authUIState.update { state ->
                        state.copy(
                            isLoading = false,
                            registerStatus = result,
                            isLoggedIn = result == RegisterStatus.SUCCESS
                        )
                    }
                }
            }
        }
    }

}