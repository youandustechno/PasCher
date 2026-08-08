package com.monasoftware.pascher.data.remote


import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Header

interface PayPalService {
    @POST("v1/billing/subscriptions")
    suspend fun createSubscription(
        @Body request: PayPalSubscriptionRequest,
        @Header("Authorization") authorization: String
    ): PayPalSubscriptionResponse

    @POST("v1/oauth2/token")
    suspend fun getAccessToken(
        @Body request: PayPalTokenRequest
    ): PayPalTokenResponse
}

data class PayPalSubscriptionRequest(
    val plan_id: String,
    val subscriber: SubscriberInfo,
    val application_context: ApplicationContext? = null
)

data class SubscriberInfo(
    val email_address: String,
    val name: Name? = null
)

data class Name(
    val given_name: String,
    val surname: String
)

data class ApplicationContext(
    val return_url: String,
    val cancel_url: String,
    val brand_name: String = "PasCher",
    val locale: String = "en-US",
    val landing_page: String = "LOGIN",
    val user_action: String = "SUBSCRIBE_NOW"
)

data class PayPalSubscriptionResponse(
    val id: String,
    val status: String,
    val links: List<PayPalLink>
)

data class PayPalLink(
    val rel: String,
    val href: String,
    val method: String? = "GET"
)

data class PayPalTokenRequest(
    val grant_type: String = "client_credentials"
)

data class PayPalTokenResponse(
    val scope: String,
    val access_token: String,
    val token_type: String,
    val app_id: String,
    val expires_in: Int
)