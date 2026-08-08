package com.monasoftware.pascher.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val IS_SUBSCRIBED = booleanPreferencesKey("is_subscribed")
        val SELECTED_PLAN_ID = stringPreferencesKey("selected_plan_id")
        val DARK_THEME_CONFIG = stringPreferencesKey("dark_theme_config")
        val EXPERIMENTAL_FEATURE_ENABLED = booleanPreferencesKey("experimental_feature_enabled")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
    }

    enum class DarkThemeConfig {
        FOLLOW_SYSTEM, LIGHT, DARK
    }

    val darkThemeConfigFlow: Flow<DarkThemeConfig> = context.dataStore.data
        .map { preferences ->
            val configName = preferences[PreferencesKeys.DARK_THEME_CONFIG] ?: DarkThemeConfig.FOLLOW_SYSTEM.name
            DarkThemeConfig.valueOf(configName)
        }

    suspend fun setDarkThemeConfig(darkThemeConfig: DarkThemeConfig) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME_CONFIG] = darkThemeConfig.name
        }
    }

    val isExperimentalFeatureEnabledFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.EXPERIMENTAL_FEATURE_ENABLED] ?: false
        }

    suspend fun setExperimentalFeatureEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EXPERIMENTAL_FEATURE_ENABLED] = enabled
        }
    }

    val displayNameFlow: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.DISPLAY_NAME] ?: "User_${(1000..9999).random()}"
        }

    suspend fun setDisplayName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DISPLAY_NAME] = name
        }
    }

    val isSubscribedFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.IS_SUBSCRIBED] ?: false
        }

    suspend fun updateSubscriptionStatus(isSubscribed: Boolean, planId: String?) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_SUBSCRIBED] = isSubscribed
            if (planId != null) {
                preferences[PreferencesKeys.SELECTED_PLAN_ID] = planId
            } else {
                preferences.remove(PreferencesKeys.SELECTED_PLAN_ID)
            }
        }
    }
}
