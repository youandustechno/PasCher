package com.monasoftware.pascher.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val price: Double,
    val description: String,
    val features: List<String>
)

@Serializable
data class UserSubscription(
    val planId: String,
    val isActive: Boolean,
    val expiryDate: Long? = null
)
