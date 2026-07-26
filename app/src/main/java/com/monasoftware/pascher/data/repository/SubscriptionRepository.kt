package com.monasoftware.pascher.data.repository

import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getSubscriptionPlans(): List<SubscriptionPlan>
    val isSubscribed: Flow<Boolean>
    suspend fun subscribe(planId: String)
}

class SubscriptionRepositoryImpl(
    private val userPrefs: UserPreferencesRepository
) : SubscriptionRepository {

    override fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                id = "basic",
                name = "Basic Plan",
                price = 9.99,
                description = "Standard definition streaming.",
                features = listOf("SD Streaming", "1 Screen")
            ),
            SubscriptionPlan(
                id = "premium",
                name = "Premium Plan",
                price = 19.99,
                description = "Ultra HD streaming with more screens.",
                features = listOf("UHD Streaming", "4 Screens", "Offline Downloads")
            )
        )
    }

    override val isSubscribed: Flow<Boolean> = userPrefs.isSubscribedFlow

    override suspend fun subscribe(planId: String) {
        userPrefs.updateSubscriptionStatus(true, planId)
    }
}
