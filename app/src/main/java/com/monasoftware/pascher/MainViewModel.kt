package com.monasoftware.pascher

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository.DarkThemeConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(
    userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val darkThemeConfig: StateFlow<DarkThemeConfig> =
        userPreferencesRepository.darkThemeConfigFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DarkThemeConfig.FOLLOW_SYSTEM
            )
}
