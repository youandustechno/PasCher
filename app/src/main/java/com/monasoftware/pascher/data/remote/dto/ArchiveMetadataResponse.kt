package com.monasoftware.pascher.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArchiveMetadataResponse(
    val metadata: ArchiveMetadata,
    val files: List<ArchiveFile>
)

@JsonClass(generateAdapter = true)
data class ArchiveMetadata(
    val title: String? = null,
    val description: String? = null,
    val genre: String? = null,
    val date: String? = null,
    val avg_rating: String? = null
)

@JsonClass(generateAdapter = true)
data class ArchiveFile(
    val name: String,
    val format: String? = null
)
