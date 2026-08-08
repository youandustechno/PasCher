package com.monasoftware.pascher.data.remote.dto

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArchiveSearchResponse(
    val response: ArchiveResponseData
)

@JsonClass(generateAdapter = true)
data class ArchiveResponseData(
    val docs: List<ArchiveDoc>
)

@JsonClass(generateAdapter = true)
data class ArchiveDoc(
    val identifier: String,
    val title: String? = null
)

@JsonClass(generateAdapter = true)
data class ArchiveMetadataResponse(
    val metadata: ArchiveMetadata,
    val files: List<ArchiveFile>
)

@JsonClass(generateAdapter = true)
data class ArchiveMetadata(
    val title: String? = null,
    val description: String? = null
)

@JsonClass(generateAdapter = true)
data class ArchiveFile(
    val name: String
)
