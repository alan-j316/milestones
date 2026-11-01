package com.cs360.weighttracker.presentation.home

sealed interface HomeActions {
    data class DeleteWeight(val id: Long) : HomeActions
}