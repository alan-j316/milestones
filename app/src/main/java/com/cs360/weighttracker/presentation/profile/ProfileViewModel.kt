package com.cs360.weighttracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.presentation.shared.UserData
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val repository: IMilestoneRepository) : ViewModel() {
    private var _state = Channel<ProfileUIState>()
    val state = _state.receiveAsFlow()

    val userData =
        combine(repository.getLatestLoggedWeight(), repository.getCurrentUser()) { weight, user ->
            return@combine UserData(user, weight)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = UserData())

    var smsPreference = repository.getSMSSetting()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)


    fun onAction(action: ProfileActions) {
        when (action) {
            ProfileActions.Logout -> {
                viewModelScope.launch {
                    _state.send(ProfileUIState.IsLoading)
                    repository.logoutUser()
                    _state.send(ProfileUIState.LogoutSuccess)
                }
            }

            is ProfileActions.UpdatePhone -> {
                viewModelScope.launch {
                    _state.send(ProfileUIState.IsLoading)
                    if (repository.setPhoneNumber(action.phoneNumber)) {
                        _state.send(ProfileUIState.UpdateNumberSuccess)
                    } else {
                        _state.send(ProfileUIState.UpdateNumberError)
                    }
                }
            }

            is ProfileActions.UpdateSMSSetting -> {
                viewModelScope.launch {
                    _state.send(ProfileUIState.IsLoading)
                    repository.setSMSSetting(action.setting)
                    _state.send(ProfileUIState.UpdateSMSSettingSuccess)
                }
            }
        }
    }


}