package com.monasoftware.pascher.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArchiveSearchResponse(
    @Json(name = "response") val response: SearchResult
)

@JsonClass(generateAdapter = true)
data class SearchResult(
    @Json(name = "docs") val docs: List<SearchDoc>
)

@JsonClass(generateAdapter = true)
data class SearchDoc(
    @Json(name = "identifier") val identifier: String,
    @Json(name = "title") val title: String? = null
)
