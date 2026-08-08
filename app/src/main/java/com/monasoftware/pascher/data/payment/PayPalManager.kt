package com.monasoftware.pascher.data.payment


import android.app.Application
import android.content.Context
import com.paypal.checkout.PayPalCheckout
import com.paypal.checkout.config.CheckoutConfig
import com.paypal.checkout.config.Environment
import com.paypal.checkout.config.SettingsConfig
import com.paypal.checkout.createorder.CurrencyCode
import com.paypal.checkout.createorder.OrderIntent
import com.paypal.checkout.order.Amount
import com.paypal.checkout.order.OrderRequest
import com.paypal.checkout.order.PurchaseUnit

class PayPalManager(
    private val context: Context,
    clientId: String,
    isProduction: Boolean = false
) {
    init {
        val config = CheckoutConfig(
            application = context.applicationContext as Application,
            clientId = clientId,
            environment = if (isProduction) Environment.LIVE else Environment.SANDBOX,
            returnUrl = "com.monasoftware.pascher://paypalpay",
            settingsConfig = SettingsConfig(
                loggingEnabled = !isProduction
            )
        )
        PayPalCheckout.setConfig(config)
    }

    fun createSubscriptionRequest(
        planId: String,
        amount: String,
        currencyCode: String = "USD"
    ): OrderRequest {
        return OrderRequest(
            intent = OrderIntent.CAPTURE,
            purchaseUnitList = listOf(
                PurchaseUnit(
                    amount = Amount(
                        currencyCode = CurrencyCode.valueOf(currencyCode),
                        value = amount
                    ),
                    referenceId = planId
                )
            )
        )
    }
}