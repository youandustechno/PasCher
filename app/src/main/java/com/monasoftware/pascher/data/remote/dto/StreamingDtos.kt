package com.monasoftware.pascher.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StreamingShowDto(
    val itemType: String? = null,
    val showType: String? = null,
    val id: String,
    val imdbId: String? = null,
    val tmdbId: String? = null,
    val title: String,
    val overview: String? = null,
    val releaseYear: Int? = null,
    val genres: List<GenreRef>? = null,
    val imageSet: ImageSet? = null,
    val streamingOptions: Map<String, List<StreamingOptionDto>> = emptyMap(),
    val rating: Double? = null,
    val runtime: Int? = null
)

@JsonClass(generateAdapter = true)
data class GenreRef(
    val id: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class ImageSet(
    val verticalPoster: Map<String, String>? = null,
    val horizontalPoster: Map<String, String>? = null,
    val verticalBackdrop: Map<String, String>? = null,
    val horizontalBackdrop: Map<String, String>? = null
) {
    fun getPoster(size: String = "w480"): String? {
        return verticalPoster?.get(size) ?: verticalPoster?.values?.firstOrNull()
    }
}

@JsonClass(generateAdapter = true)
data class StreamingOptionDto(
    val service: ServiceRef? = null,
    val type: String? = null,
    val link: String? = null,
    val videoLink: String? = null,
    val quality: String? = null
)

@JsonClass(generateAdapter = true)
data class ServiceRef(
    val id: String,
    val name: String
)

@JsonClass(generateAdapter = true)
data class SearchResult(
    val shows: List<StreamingShowDto> = emptyList(),
    val hasMore: Boolean = false,
    val nextCursor: String? = null
)

@JsonClass(generateAdapter = true)
data class ChangesResult(
    val shows: Map<String, StreamingShowDto> = emptyMap(),
    val hasMore: Boolean = false,
    val nextCursor: String? = null
)

@JsonClass(generateAdapter = true)
data class Genre(
    val id: String,
    val name: String
)
