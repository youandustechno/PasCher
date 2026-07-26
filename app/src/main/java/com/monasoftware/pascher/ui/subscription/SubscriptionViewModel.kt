package com.monasoftware.pascher.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.repository.SubscriptionRepository
import com.monasoftware.pascher.domain.model.SubscriptionPlan
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SubscriptionViewModel(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _plans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    val plans: StateFlow<List<SubscriptionPlan>> = _plans.asStateFlow()

    private val _isSubscribed = MutableStateFlow(false)
    val isSubscribed: StateFlow<Boolean> = _isSubscribed.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _paymentSuccess = MutableStateFlow(false)
    val paymentSuccess: StateFlow<Boolean> = _paymentSuccess.asStateFlow()

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
            // Simulate network delay for payment
            delay(2000)
            subscriptionRepository.subscribe(planId)
            _isProcessing.value = false
            _paymentSuccess.value = true
        }
    }

    fun resetPaymentStatus() {
        _paymentSuccess.value = false
    }
}
