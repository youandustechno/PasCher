package com.monasoftware.pascher.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository.DarkThemeConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    val darkThemeConfig: StateFlow<DarkThemeConfig> =
        userPreferencesRepository.darkThemeConfigFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = DarkThemeConfig.FOLLOW_SYSTEM
            )

    val isExperimentalFeatureEnabled: StateFlow<Boolean> =
        userPreferencesRepository.isExperimentalFeatureEnabledFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = false
            )

    val displayName: StateFlow<String> =
        userPreferencesRepository.displayNameFlow
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ""
            )

    fun updateDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        viewModelScope.launch {
            userPreferencesRepository.setDarkThemeConfig(darkThemeConfig)
        }
    }

    fun toggleExperimentalFeature(enabled: Boolean) {
        viewModelScope.launch {
            userPreferencesRepository.setExperimentalFeatureEnabled(enabled)
        }
    }

    fun updateDisplayName(name: String) {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayName(name)
        }
    }
}
