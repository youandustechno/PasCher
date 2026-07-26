package com.monasoftware.pascher.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArchiveMetadataResponse(
    @Json(name = "metadata") val metadata: ArchiveMetadata,
    @Json(name = "files") val files: List<ArchiveFile>
)

@JsonClass(generateAdapter = true)
data class ArchiveMetadata(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "title") val title: String? = null,
    @Json(name = "description") val description: String? = null,
    @Json(name = "date") val date: String? = null,
    @Json(name = "genre") val genre: String? = null,
    @Json(name = "avg_rating") val avgRating: String? = null
)

@JsonClass(generateAdapter = true)
data class ArchiveFile(
    @Json(name = "name") val name: String,
    @Json(name = "format") val format: String? = null
)
