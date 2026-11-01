package com.cs360.weighttracker.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.status.RegisterStatus
import com.cs360.weighttracker.presentation.validators.PasswordValidator
import com.cs360.weighttracker.presentation.validators.UsernameValidator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: IMilestoneRepository) : ViewModel() {
    private val _state = MutableStateFlow<RegisterUIState>(RegisterUIState.IsLoading)
    val state: StateFlow<RegisterUIState> = _state.asStateFlow()


    fun onAction(action: RegisterActions) {
        when (action) {
            is RegisterActions.Register -> {
                viewModelScope.launch {
                    _state.update {
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
                        RegisterStatus.SUCCESS -> _state.update { RegisterUIState.RegistrationSuccess }
                        RegisterStatus.UNKNOWN_FAILURE -> _state.update { RegisterUIState.UnknownError }
                        RegisterStatus.USER_EXISTS -> _state.update { RegisterUIState.UserExistsError }
                    }
                }
            }
        }
    }



    /**
     * Validates the registration and returns a boolean indicating whether
     * all the provided information is valid.
     */
    private fun validateRegistration(
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        var isValid = true
        if (!UsernameValidator.validate(username)) {
            _state.update { RegisterUIState.UsernameError }
            isValid = false
        }

        if (!PasswordValidator.validate(password)) {
            _state.update { RegisterUIState.PasswordError }
            isValid = false
        }

        if (!PasswordValidator.validate(confirmPassword) || password != confirmPassword) {
            _state.update { RegisterUIState.PasswordMismatchError }
            isValid = false
        }

        return isValid
    }

}