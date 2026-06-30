package com.play.urlscheduler.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.play.urlscheduler.domain.model.PrivilegeMode
import com.play.urlscheduler.domain.repository.StateRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val stateRepository: StateRepository
) : ViewModel() {

    val privilegeMode: StateFlow<PrivilegeMode> = stateRepository.privilegeMode
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PrivilegeMode.STANDARD
        )

    fun setPrivilegeMode(mode: PrivilegeMode) {
        viewModelScope.launch {
            stateRepository.setPrivilegeMode(mode)
        }
    }
}
