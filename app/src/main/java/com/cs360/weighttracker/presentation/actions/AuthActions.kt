package com.cs360.weighttracker.presentation.actions

sealed interface AuthActions {
    data class Login(val username: String, val password: String)
    data class Register(val username: String, val password: String)
    object Logout
}