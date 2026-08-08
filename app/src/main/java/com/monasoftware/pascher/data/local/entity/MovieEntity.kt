package com.monasoftware.pascher.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.monasoftware.pascher.domain.model.Movie

@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val thumbnailUrl: String,
    val videoUrl: String,
    val genre: String,
    val rating: Double,
    val releaseYear: Int,
    val runtime: Int,
    val isPremium: Boolean
)

fun MovieEntity.toDomain() = Movie(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    videoUrl = videoUrl,
    genre = genre,
    rating = rating,
    releaseYear = releaseYear,
    runtime = runtime,
    isPremium = isPremium
)

fun Movie.toEntity() = MovieEntity(
    id = id,
    title = title,
    description = description,
    thumbnailUrl = thumbnailUrl,
    videoUrl = videoUrl,
    genre = genre,
    rating = rating,
    releaseYear = releaseYear,
    runtime = runtime,
    isPremium = isPremium
)
