package com.cs360.weighttracker.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.models.GoalType
import com.cs360.weighttracker.domain.models.User
import com.cs360.weighttracker.presentation.shared.UserData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: IMilestoneRepository) : ViewModel() {

    private var _state = Channel<HomeUIState>()
    val state = _state.receiveAsFlow()

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

    var smsPreference = repository.getSMSSetting()
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)


    fun onAction(action: HomeActions) {

        when (action) {
            is HomeActions.DeleteWeight -> {
                viewModelScope.launch {
                    _state.send(HomeUIState.IsLoading)
                    val deleteSuccess = repository.deleteDailyWeight(action.id)
                    if (deleteSuccess) {
                        _state.send(HomeUIState.WeightDeleted)
                    } else {
                        _state.send(HomeUIState.WeightDeleteError)
                    }
                }
            }

            is HomeActions.TrackWeight -> {
                viewModelScope.launch {
                    _state.send(HomeUIState.IsLoading)
                    val weightLogged = repository.logDailyWeight(action.weight)
                    if (weightLogged) {
                        // Check whether the goal weight was achieved. If so,
                        // trigger the related event, and if not trigger weight added event.
                        val user = userData.value.user ?: return@launch
                        if (hasReachGoalWeight(action.weight, user)) {
                            val shouldSendSms = smsPreference.value
                            _state.send(
                                HomeUIState.GoalWeightReached(
                                    action.weight,
                                    shouldSendSms
                                )
                            )
                        } else {
                            _state.send(HomeUIState.WeightTracked(action.weight))
                        }
                    } else {
                        // Error logging the weight.
                        _state.send(HomeUIState.WeightTrackingError)
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