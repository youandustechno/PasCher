package com.monasoftware.pascher.data.repository

import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.remote.ApplicationContext
import com.monasoftware.pascher.data.remote.PayPalService
import com.monasoftware.pascher.data.remote.PayPalSubscriptionRequest
import com.monasoftware.pascher.data.remote.PayPalTokenRequest
import com.monasoftware.pascher.data.remote.SubscriberInfo
import com.monasoftware.pascher.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    fun getSubscriptionPlans(): List<SubscriptionPlan>
    val isSubscribed: Flow<Boolean>
    suspend fun subscribe(planId: String)
    suspend fun initiatePayPalSubscription(planId: String, email: String): String? // Returns approval URL
}

class SubscriptionRepositoryImpl(
    private val userPrefs: UserPreferencesRepository,
    private val payPalService: PayPalService? = null
) : SubscriptionRepository {

    override fun getSubscriptionPlans(): List<SubscriptionPlan> {
        return listOf(
            SubscriptionPlan(
                id = "basic",
                name = "Basic Plan",
                price = 9.99,
                description = "Standard definition streaming.",
                features = listOf("SD Streaming", "1 Screen"),
                paypalPlanId = "P-XXXXXXXXXXXXXXXXX" // Add your PayPal plan IDs
            ),
            SubscriptionPlan(
                id = "premium",
                name = "Premium Plan",
                price = 19.99,
                description = "Ultra HD streaming with more screens.",
                features = listOf("UHD Streaming", "4 Screens", "Offline Downloads"),
                paypalPlanId = "P-YYYYYYYYYYYYYYYYY" // Add your PayPal plan IDs
            )
        )
    }

    override val isSubscribed: Flow<Boolean> = userPrefs.isSubscribedFlow

    override suspend fun subscribe(planId: String) {
        userPrefs.updateSubscriptionStatus(true, planId)
    }

    override suspend fun initiatePayPalSubscription(planId: String, email: String): String? {
        return try {
            val plan = getSubscriptionPlans().find { it.id == planId } ?: return null

            val subscriptionRequest = PayPalSubscriptionRequest(
                plan_id = plan.paypalPlanId,
                subscriber = SubscriberInfo(email_address = email),
                application_context = ApplicationContext(
                    return_url = "https://yourapp.com/return",
                    cancel_url = "https://yourapp.com/cancel"
                )
            )

            val token = payPalService?.getAccessToken(PayPalTokenRequest())
            val authHeader = "Bearer ${token?.access_token}"

            val response = payPalService?.createSubscription(subscriptionRequest, authHeader)

            // Return the approval link
            response?.links?.find { it.rel == "approve" }?.href
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}