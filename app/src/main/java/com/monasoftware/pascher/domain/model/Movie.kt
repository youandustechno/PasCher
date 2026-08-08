package com.monasoftware.pascher.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Movie(
    val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val genre: String,
    val rating: Double,
    val releaseYear: Int,
    val runtime: Int = 0,
    val isPremium: Boolean = false
)
