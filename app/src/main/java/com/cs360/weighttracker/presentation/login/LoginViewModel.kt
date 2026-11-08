package com.cs360.weighttracker.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.status.LoginStatus
import com.cs360.weighttracker.presentation.validators.PasswordValidator
import com.cs360.weighttracker.presentation.validators.UsernameValidator
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LoginViewModel(private val repository: IMilestoneRepository) : ViewModel() {
    private val _state = Channel<LoginUIState>()
    val state = _state.receiveAsFlow()

    init {
        // Query the repository to check if the user is already logged in and if they are
        // then navigate emit login success action
        viewModelScope.launch {
            triggerLoginEvent()
        }
    }


    fun onAction(action: LoginActions) {
        when (action) {
            is LoginActions.Login -> {
                viewModelScope.launch {
                    _state.send(LoginUIState.IsLoading)
                    if (!validateLogin(action.username, action.password))
                        return@launch
                    val result = repository.loginUser(action.username, action.password)
                    when (result) {
                        LoginStatus.USERNAME_ERROR -> _state.send(LoginUIState.UsernameError)
                        LoginStatus.PASSWORD_ERROR -> _state.send(LoginUIState.PasswordError)
                        LoginStatus.SUCCESS -> triggerLoginEvent()
                        LoginStatus.UNKNOWN_FAILURE -> _state.send(LoginUIState.UnknownError)
                    }
                }
            }
        }
    }


    /**
     * Triggers an appropriate event based on whether the user has a goal set or not.
     */
    private suspend fun triggerLoginEvent() {
        // If login was success check whether the user has a goal set
        val currentUser = repository.getCurrentUser()
        currentUser?.let { user ->
            if (repository.userHasGoalSet(user.userId)) {
                _state.send(LoginUIState.LogInSuccess)
                return
            } else {
                // If not trigger goal not set event
                _state.send(LoginUIState.GoalNotSet)
            }
        }
    }


    /**
     * Validates the login and returns a boolean indicating whether
     * all the provided information is valid.
     */
    private suspend fun validateLogin(
        username: String,
        password: String,
    ): Boolean {
        if (!UsernameValidator.validate(username)) {
            _state.send(LoginUIState.UsernameError)
            return false
        }

        if (!PasswordValidator.validate(password)) {
            _state.send(LoginUIState.PasswordError)
            return false
        }

        return true
    }

}