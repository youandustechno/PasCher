package com.monasoftware.pascher

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository.DarkThemeConfig
import com.monasoftware.pascher.ui.navigation.NavApp
import com.monasoftware.pascher.ui.theme.PasCherTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as PasCherApplication).container

        setContent {
            val mainViewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(container.userPreferencesRepository) as T
                    }
                }
            )

            val darkThemeConfig by mainViewModel.darkThemeConfig.collectAsStateWithLifecycle()

            val darkTheme = when (darkThemeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }

            PasCherTheme(darkTheme = darkTheme) {
                NavApp()
            }
        }
    }
}
