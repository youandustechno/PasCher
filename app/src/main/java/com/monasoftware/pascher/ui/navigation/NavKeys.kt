package com.monasoftware.pascher.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
sealed interface NavKey {
    @Serializable
    data object Discovery : NavKey

    @Serializable
    data class MovieDetail(val movieId: String) : NavKey

    @Serializable
    data object Subscription : NavKey

    @Serializable
    data object Settings : NavKey
}
