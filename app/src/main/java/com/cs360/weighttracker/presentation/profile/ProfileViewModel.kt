package com.cs360.weighttracker.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cs360.weighttracker.domain.IMilestoneRepository
import com.cs360.weighttracker.domain.models.User
import com.cs360.weighttracker.presentation.shared.UserData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.properties.Delegates

class ProfileViewModel(private val repository: IMilestoneRepository) : ViewModel() {

    private var _state = MutableStateFlow<ProfileUIState>(ProfileUIState.IsLoading)
    val state = _state.asStateFlow()


    val userData =
        combine(repository.getLatestLoggedWeight(), repository.getCurrentUser()) { weight, user ->
            return@combine UserData(user, weight)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), initialValue = UserData())

    var smsPreference by Delegates.notNull<Boolean>()
        private set


    fun onAction(action: ProfileActions) {
        when (action) {
            ProfileActions.Logout -> {
                _state.update { ProfileUIState.IsLoading }
                viewModelScope.launch {
                    repository.logoutUser()
                    _state.update { ProfileUIState.LogoutSuccess }
                }
            }

            is ProfileActions.UpdatePhone -> {
                _state.update { ProfileUIState.IsLoading }
                viewModelScope.launch {
                    if (repository.setPhoneNumber(action.phoneNumber)) {
                        _state.update { ProfileUIState.UpdateNumberSuccess }
                    } else {
                        _state.update { ProfileUIState.UpdateNumberError }
                    }
                }
            }

            is ProfileActions.UpdateSMSSetting -> {
                _state.update { ProfileUIState.IsLoading }
                viewModelScope.launch {
                    repository.userNotificationSetting = action.setting
                    _state.update { ProfileUIState.UpdateSMSSettingSuccess }
                }
            }
        }
    }


}