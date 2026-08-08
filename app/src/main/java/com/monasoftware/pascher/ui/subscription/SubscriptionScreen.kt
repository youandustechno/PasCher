package com.monasoftware.pascher.ui.subscription

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.monasoftware.pascher.domain.model.SubscriptionPlan


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onBackClick: () -> Unit
) {
    val plans by viewModel.plans.collectAsState()
    val isSubscribed by viewModel.isSubscribed.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val paymentSuccess by viewModel.paymentSuccess.collectAsState()

    LaunchedEffect(paymentSuccess) {
        if (paymentSuccess) {
            // Potentially show a snackbar or navigate back
            // viewModel.resetPaymentStatus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscriptions") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isSubscribed && !paymentSuccess) {
                AlreadySubscribedContent(onBackClick)
            } else if (paymentSuccess) {
                PaymentSuccessContent(onBackClick)
            } else {
                SubscriptionList(
                    plans = plans,
                    isProcessing = isProcessing,
                    onPayClick = { viewModel.processPayment(it.id) }
                )
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
fun SubscriptionList(
    plans: List<SubscriptionPlan>,
    isProcessing: Boolean,
    onPayClick: (SubscriptionPlan) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Choose Your Plan",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "Unlock premium movies and ad-free experience.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }
        items(plans) { plan ->
            PlanCard(plan = plan, isProcessing = isProcessing, onPayClick = onPayClick)
        }
    }
}

@Composable
fun PlanCard(
    plan: SubscriptionPlan,
    isProcessing: Boolean,
    onPayClick: (SubscriptionPlan) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (plan.id == "premium") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = plan.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                if (plan.id == "premium") {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Popular",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "$${plan.price}/month",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.ExtraBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = plan.description, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(16.dp))
            
            plan.features.forEach { feature ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF4CAF50)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = feature, style = MaterialTheme.typography.bodySmall)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))

            PayPalPaymentButton(
                plan = plan,
                isProcessing = isProcessing,
                onPayClick = onPayClick
            )
        }
    }
}

@Composable
fun AlreadySubscribedContent(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Star,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "You're all set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "You are currently a premium member.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBackClick) {
            Text("Go Back")
        }
    }
}

@Composable
fun PaymentSuccessContent(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = Color(0xFF4CAF50)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Payment Successful!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Your subscription is now active.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onBackClick) {
            Text("Start Watching")
        }
    }
}

@Composable
fun PayPalPaymentButton(
    plan: SubscriptionPlan,
    isProcessing: Boolean,
    onPayClick: (SubscriptionPlan) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = { onPayClick(plan) },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Subscribe with PayPal")
        }
    }
}
