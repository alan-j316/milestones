package com.cs360.weighttracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.models.GoalType
import com.cs360.weighttracker.domain.models.User
import com.cs360.weighttracker.presentation.shared.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: IMilestoneRepository) : ViewModel() {

    private var _state = MutableStateFlow<HomeUIState>(HomeUIState.IsLoading)
    val state = _state.asStateFlow()

    // Daily Weights Flow State
    val dailyWeights = repository.dailyWeights.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val userData =
        combine(repository.getLatestLoggedWeight(), repository.getCurrentUser()) { weight, user ->
            return@combine UserData(user, weight)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = UserData())


    fun onAction(action: HomeActions) {

        when (action) {
            is HomeActions.DeleteWeight -> {
                _state.update { HomeUIState.IsLoading }
                viewModelScope.launch {
                    val deleteSuccess = repository.deleteDailyWeight(action.id)
                    if (deleteSuccess) {
                        _state.update { HomeUIState.WeightDeleted }
                    } else {
                        _state.update { HomeUIState.WeightDeleteError }
                    }
                }
            }

            is HomeActions.TrackWeight -> {
                _state.update { HomeUIState.IsLoading }
                viewModelScope.launch {
                    val weightLogged = repository.logDailyWeight(action.weight)
                    if (weightLogged) {
                        // Check whether the goal weight was achieved. If so,
                        // trigger the related event, and if not trigger weight added event.
                        val user = userData.value.user ?: return@launch
                        if (hasReachGoalWeight(action.weight, user)) {
                            val shouldSendSms = repository.userNotificationSetting
                            _state.update {
                                HomeUIState.GoalWeightReached(
                                    action.weight,
                                    shouldSendSms
                                )
                            }
                        } else {
                            _state.update { HomeUIState.WeightTracked(action.weight) }
                        }
                    } else {
                        // Error logging the weight.
                        _state.update { HomeUIState.WeightTrackingError }
                    }
                }
            }
        }
    }


    /**
     * Returns whether the user has reached a goal weight depending on the type of goal(loss/gain).
     * @param newWeight The latest logged weight.
     * @return True if the user has reached their goal weight.
     * @return False otherwise.
     */
    private fun hasReachGoalWeight(newWeight: Float, user: User): Boolean {
        val weightGoal: Float = user.goalWeight
        // In case of weight gain we need to check if current logged weight is greater or equal to
        // the goal weight and weight loss if the logged weight is less than or equal
        return if (user.getGoalType() === GoalType.WEIGHT_GAIN) {
            newWeight >= weightGoal
        } else {
            newWeight <= weightGoal
        }
    }
}