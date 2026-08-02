package com.monasoftware.pascher.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TraktMovieDto(
    val title: String,
    val year: Int?,
    val ids: TraktIdsDto,
    val tagline: String? = null,
    val overview: String? = null,
    val released: String? = null,
    val runtime: Int? = null,
    val trailer: String? = null,
    val rating: Double? = null,
    val genres: List<String>? = null,
    val images: TraktImagesDto? = null
)

@JsonClass(generateAdapter = true)
data class TraktIdsDto(
    val trakt: Int,
    val slug: String?,
    val imdb: String?,
    val tmdb: Int?
)

@JsonClass(generateAdapter = true)
data class TraktImagesDto(
    val logo: List<String> = emptyList(),
    val thumb: List<String> = emptyList(),
    val banner: List<String> = emptyList(),
    val fanart: List<String> = emptyList(),
    val poster: List<String> = emptyList(),
    val clearart: List<String> = emptyList()
)
@JsonClass(generateAdapter = true)
data class TraktImageSizeDto(
    val full: String? = null,   // largest
    val medium: String? = null, // medium size
    val thumb: String? = null   // thumbnail
)

@JsonClass(generateAdapter = true)
data class TraktTrendingDto(
    val watchers: Int,
    val movie: TraktMovieDto
)

@JsonClass(generateAdapter = true)
data class TraktAnticipatedDto(
    @param:Json(name = "list_count") val listCount: Int,
    val movie: TraktMovieDto
)

@JsonClass(generateAdapter = true)
data class TraktSearchDto(
    val type: String,
    val score: Double,
    val movie: TraktMovieDto
)
