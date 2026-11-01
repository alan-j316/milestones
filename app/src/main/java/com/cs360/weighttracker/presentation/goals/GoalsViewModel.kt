package com.cs360.weighttracker.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.presentation.validators.WeightValidator.validate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GoalsViewModel(private val repository: IMilestoneRepository) : ViewModel() {

    private var _state = MutableStateFlow<GoalsUIState>(GoalsUIState.IsLoading)
    val state = _state.asStateFlow()

    init {
        // Send an event to show the initial data if it exists.
        viewModelScope.launch {
            val currentUser = repository.getCurrentUser()
            currentUser?.let { user ->
                _state.update {
                    GoalsUIState.InitialDataLoaded(
                        user.fullName,
                        user.currentWeight,
                        user.goalWeight
                    )
                }
            }
        }
    }


    fun onAction(action: GoalsActions) {
        when (action) {
            is GoalsActions.Save -> {
                viewModelScope.launch {
                    _state.update { GoalsUIState.IsLoading }
                    val (fullName, currentWeight, goalWeight) = action
                    if (!validateInputs(fullName, currentWeight, goalWeight))
                        return@launch
                    val result = repository.updateUserInfo(
                        action.fullName,
                        action.currentWeight,
                        action.goalWeight
                    )
                    if (!result) {
                        // Operation failed
                        _state.update { GoalsUIState.SaveError }
                    } else {
                        _state.update { GoalsUIState.SaveSuccess }
                    }
                }
            }
        }
    }


    /**
     * Validates the user input.
     *
     * @param fullName   The user's full name.
     * @param curWeight  The user's current weight.
     * @param goalWeight The user's goal weight.
     * @return Boolean whether all the fields are valid.
     */
    private fun validateInputs(
        fullName: String,
        curWeight: Float,
        goalWeight: Float
    ): Boolean {
        var isValid = true
        if (fullName.isEmpty()) {
            _state.update { GoalsUIState.FullNameError }
            isValid = false
        }
        if (!validate(curWeight)) {
            _state.update { GoalsUIState.CurrentWeightError }
            isValid = false
        }
        if (!validate(goalWeight)) {
            _state.update { GoalsUIState.GoalWeighError }
            isValid = false
        }
        return isValid
    }

}