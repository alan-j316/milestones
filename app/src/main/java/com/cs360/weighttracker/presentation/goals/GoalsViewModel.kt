package com.cs360.weighttracker.presentation.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.presentation.validators.WeightValidator.validate
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GoalsViewModel(private val repository: IMilestoneRepository) : ViewModel() {

    private var _state = Channel<GoalsUIState>()
    val state = _state.receiveAsFlow()

    val currentUser = repository.getCurrentUser()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun onAction(action: GoalsActions) {
        when (action) {
            is GoalsActions.Save -> {
                viewModelScope.launch {
                    _state.send(GoalsUIState.IsLoading)
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
                        _state.send(GoalsUIState.SaveError)
                    } else {
                        _state.send(GoalsUIState.SaveSuccess)
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
    private suspend fun validateInputs(
        fullName: String,
        curWeight: Float,
        goalWeight: Float
    ): Boolean {
        if (fullName.isEmpty()) {
            _state.send(GoalsUIState.FullNameError)
            return false
        }
        if (!validate(curWeight)) {
            _state.send(GoalsUIState.CurrentWeightError)
            return false
        }
        if (!validate(goalWeight)) {
            _state.send(GoalsUIState.GoalWeighError)
            return false
        }
        return true
    }

}