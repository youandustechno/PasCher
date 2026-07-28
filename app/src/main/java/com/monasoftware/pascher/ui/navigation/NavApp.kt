package com.monasoftware.pascher.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import com.monasoftware.pascher.PasCherApplication
import com.monasoftware.pascher.ui.LastRoute
import com.monasoftware.pascher.ui.discovery.DiscoveryScreen
import com.monasoftware.pascher.ui.discovery.DiscoveryViewModel
import com.monasoftware.pascher.ui.details.MovieDetailsScreen
import com.monasoftware.pascher.ui.details.MovieDetailsViewModel
import com.monasoftware.pascher.ui.subscription.SubscriptionScreen
import com.monasoftware.pascher.ui.subscription.SubscriptionViewModel

@Suppress("UNCHECKED_CAST")
@Composable
fun NavApp() {
    val backStack = remember { mutableStateListOf(LastRoute.route ?: NavKey.Discovery) }
    val container = (LocalContext.current.applicationContext as PasCherApplication).container

    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),
        modifier = Modifier.fillMaxSize()
    ) { key ->
        when (key) {
            is NavKey.Discovery -> {
                NavEntry<NavKey>(key) {
                    val discoveryViewModel: DiscoveryViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return DiscoveryViewModel(container.movieRepository) as T
                            }
                        }
                    )
                    DiscoveryScreen(
                        viewModel = discoveryViewModel,
                        onNavigateToDetail = { movieId ->
                            backStack.add(NavKey.MovieDetail(movieId))
                        },
                        onNavigateToSubscription = {
                            backStack.add(NavKey.Subscription)
                        }
                    )
                }
            }
            is NavKey.MovieDetail -> {
                NavEntry<NavKey>(key) {
                    val movieDetailsViewModel: MovieDetailsViewModel = viewModel(
                        key = key.movieId,
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return MovieDetailsViewModel(
                                    movieId = key.movieId,
                                    movieRepository = container.movieRepository,
                                    savedStateHandle = androidx.lifecycle.SavedStateHandle()
                                ) as T
                            }
                        }
                    )
                    MovieDetailsScreen(
                        viewModel = movieDetailsViewModel,
                        onBackClick = {
                            backStack.clear()
                            backStack.add(NavKey.Discovery)
                        }
                    )
                }
            }
            is NavKey.Subscription -> {
                NavEntry<NavKey>(key) {
                    val subscriptionViewModel: SubscriptionViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                return SubscriptionViewModel(
                                    subscriptionRepository = container.subscriptionRepository
                                ) as T
                            }
                        }
                    )
                    SubscriptionScreen(
                        viewModel = subscriptionViewModel,
                        onBackClick = { backStack.removeLastOrNull() }
                    )
                }
            }
        }
    }
}
