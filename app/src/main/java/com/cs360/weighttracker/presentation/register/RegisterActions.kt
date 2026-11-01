package com.cs360.weighttracker.presentation.register

sealed interface RegisterActions {
    data class Register(val username: String, val password: String, val confirmPassword: String) :
        RegisterActions
}