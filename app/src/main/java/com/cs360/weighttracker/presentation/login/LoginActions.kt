package com.cs360.weighttracker.presentation.login

sealed interface LoginActions {
    data class Login(val username: String, val password: String) : LoginActions
}