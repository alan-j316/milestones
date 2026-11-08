package com.cs360.weighttracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.models.GoalType
import com.cs360.weighttracker.domain.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.shareIn

class HomeViewModel(private val repository: IMilestoneRepository) : ViewModel() {

    private var _state = MutableStateFlow<HomeUIState>(HomeUIState.IsLoading)
    val state = _state.asStateFlow()

    // Latest Weight
    val latestWeight = repository.getLatestLoggedWeight().shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    // Daily Weights Flow State
    val dailyWeights = repository.dailyWeights.shareIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        replay = 1
    )

    lateinit var currentUser: User // TODO: Init user
        private set


    fun onAction(action: HomeActions) {

        when (action) {
            is HomeActions.DeleteWeight -> TODO()
        }
    }


    /**
     * Returns whether the user has reached a goal weight depending on the type of goal(loss/gain).
     * @param newWeight The latest logged weight.
     * @return True if the user has reached their goal weight.
     * @return False otherwise.
     */
    private fun hasReachGoalWeight(newWeight: Float): Boolean {
        val weightGoal: Float = currentUser.goalWeight
        // In case of weight gain we need to check if current logged weight is greater or equal to
        // the goal weight and weight loss if the logged weight is less than or equal
        return if (currentUser.getGoalType() === GoalType.WEIGHT_GAIN) {
            newWeight >= weightGoal
        } else {
            newWeight <= weightGoal
        }
    }

}