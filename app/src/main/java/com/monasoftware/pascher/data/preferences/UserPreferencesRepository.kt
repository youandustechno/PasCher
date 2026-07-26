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
