package com.cs360.weighttracker.presentation.actions

sealed interface AuthActions {
    data class Login(val username: String, val password: String) : AuthActions
    data class Register(val username: String, val password: String, val confirmPassword: String) :
        AuthActions

    object Logout : AuthActions
}