package com.cs360.weighttracker.presentation.register

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.status.RegisterStatus
import com.cs360.weighttracker.presentation.validators.PasswordValidator
import com.cs360.weighttracker.presentation.validators.UsernameValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: IMilestoneRepository) : ViewModel() {
    private val _state = Channel<RegisterUIState>()
    val state = _state.receiveAsFlow()


    fun onAction(action: RegisterActions) {
        when (action) {
            is RegisterActions.Register -> {
                viewModelScope.launch {
                    _state.send(RegisterUIState.IsLoading)
                    if (!validateRegistration(
                            action.username,
                            action.password,
                            action.confirmPassword
                        )
                    )
                        return@launch
                    val result = repository.registerUser(action.username, action.password)
                    when (result) {
                        RegisterStatus.SUCCESS -> _state.send(RegisterUIState.RegistrationSuccess)
                        RegisterStatus.UNKNOWN_FAILURE -> _state.send(RegisterUIState.UnknownError)
                        RegisterStatus.USER_EXISTS -> _state.send(RegisterUIState.UserExistsError)
                    }
                }
            }
        }
    }


    /**
     * Validates the registration and returns a boolean indicating whether
     * all the provided information is valid.
     */
    private suspend fun validateRegistration(
        username: String,
        password: String,
        confirmPassword: String
    ): Boolean {
        if (!UsernameValidator.validate(username)) {
            _state.send(RegisterUIState.UsernameError)
            return false
        }

        if (!PasswordValidator.validate(password)) {
            _state.send(RegisterUIState.PasswordError)
            return false
        }

        if (!PasswordValidator.validate(confirmPassword) || password != confirmPassword) {
            _state.send(RegisterUIState.PasswordMismatchError)
            return false
        }

        return true
    }

}