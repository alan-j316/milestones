package com.cs360.weighttracker.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.status.RegisterStatus
import com.cs360.weighttracker.presentation.actions.AuthActions
import com.cs360.weighttracker.presentation.states.RegisterUIState
import com.cs360.weighttracker.presentation.validators.PasswordValidator
import com.cs360.weighttracker.presentation.validators.UsernameValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MilestonesViewModel(private val repository: IMilestoneRepository) : ViewModel() {


    private val _registerUIState = MutableStateFlow<RegisterUIState>(RegisterUIState.IsLoading)
    val registerUIState: StateFlow<RegisterUIState> = _registerUIState.asStateFlow()

    init {
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            if (currentUser != null)
                null
            // TODO: Update to update login ui state update
//                _registerUIState.update { AuthUiState.LogInSuccess }
        }
    }

    // Collect Backend State?

    fun onAction(action: AuthActions) {
        when (action) {
            is AuthActions.Login -> {
//                viewModelScope.launch {
//                    _registerUIState.update {
//                        AuthUiState.IsLoading
//                    }
//                    if (!validateLogin(action.username, action.password))
//                        return@launch
//                    val result = repository.loginUser(action.username, action.password)
//                    when (result) {
//                        LoginStatus.USERNAME_ERROR -> _registerUIState.update { AuthUiState.UsernameError }
//                        LoginStatus.PASSWORD_ERROR -> _registerUIState.update { AuthUiState.PasswordError }
//                        LoginStatus.SUCCESS -> _registerUIState.update { AuthUiState.LogInSuccess }
//                        LoginStatus.UNKNOWN_FAILURE -> _registerUIState.update { AuthUiState.UnknownError }
//                    }
//                }
            }

            is AuthActions.Register -> {
                viewModelScope.launch {
                    _registerUIState.update {
                        RegisterUIState.IsLoading
                    }
                    if (!validateRegistration(
                            action.username,
                            action.password,
                            action.confirmPassword
                        )
                    )
                        return@launch
                    val result = repository.registerUser(action.username, action.password)
                    when (result) {
                        RegisterStatus.SUCCESS -> _registerUIState.update { RegisterUIState.RegistrationSuccess }
                        RegisterStatus.UNKNOWN_FAILURE -> _registerUIState.update { RegisterUIState.UnknownError }
                        RegisterStatus.USER_EXISTS -> _registerUIState.update { RegisterUIState.UserExistsError }
                    }
                }
            }

            is AuthActions.Logout -> {
                // TODO;
//                _registerUIState.update { AuthUiState.Logout }
            }
        }
    }


//    private fun validateLogin(
//        username: String,
//        password: String,
//    ): Boolean {
//        var isValidLogin = true
//        if (!UsernameValidator.validate(username)) {
//            _registerUIState.update { AuthUiState.UsernameError }
//            isValidLogin = false
//        }
//
//        if (!PasswordValidator.validate(password)) {
//            _registerUIState.update { AuthUiState.PasswordError }
//            isValidLogin = false
//        }
//
//        return isValidLogin
//    }

    private fun validateRegistration(
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValidLogin = true
        if (!UsernameValidator.validate(username)) {
            _registerUIState.update { RegisterUIState.UsernameError }
            isValidLogin = false
        }

        if (!PasswordValidator.validate(password)) {
            _registerUIState.update { RegisterUIState.PasswordError }
            isValidLogin = false
        }

        if (!PasswordValidator.validate(confirmPassword) || password != confirmPassword) {
            _registerUIState.update { RegisterUIState.PasswordMismatchError }
            isValidLogin = false
        }

        return isValidLogin
    }

}