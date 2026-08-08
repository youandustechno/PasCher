package com.monasoftware.pascher.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.repository.SubscriptionRepository
import com.monasoftware.pascher.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository,
    private val userEmail: String = "" // Pass user email
) : ViewModel() {

    private val _plans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val plans: StateFlow<List<SubscriptionPlan>> = _plans.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess.asStateFlow()

    private val _paypalApprovalUrl = MutableStateFlow<String?>(null)
    val paypalApprovalUrl: StateFlow<String?> = _paypalApprovalUrl.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        _plans.value = subscriptionRepository.getSubscriptionPlans()
        viewModelScope.launch {
            subscriptionRepository.isSubscribed.collect {
                _isSubscribed.value = it
            }
        }
    }

    fun processPayment(planId: String) {
        viewModelScope.launch {
            _isProcessing.value = true
            _errorMessage.value = null
            try {
                // Try PayPal first if available
                val approvalUrl = subscriptionRepository.initiatePayPalSubscription(planId, userEmail)
                if (approvalUrl != null) {
                    _paypalApprovalUrl.value = approvalUrl
                } else {
                    // Fallback to local processing
                    subscriptionRepository.subscribe(planId)
                    _paymentSuccess.value = true
                }
            } catch (e: Exception) {
                _errorMessage.value = "Payment failed: ${e.message}"
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun resetPaymentStatus() {
        _paymentSuccess.value = false
        _paypalApprovalUrl.value = null
        _errorMessage.value = null
    }
}