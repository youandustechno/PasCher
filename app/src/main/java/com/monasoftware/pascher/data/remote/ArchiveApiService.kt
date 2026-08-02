package com.monasoftware.pascher.data.remote

import com.monasoftware.pascher.data.remote.dto.ArchiveMetadataResponse
import com.monasoftware.pascher.data.remote.dto.ArchiveSearchResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ArchiveApiService {

    @GET("advancedsearch.php")
    suspend fun searchMovies(
        @Query("q") query: String,
        @Query("fl[]") fields: List<String> = listOf("identifier", "title"),
        @Query("rows") rows: Int = 5,
        @Query("output") output: String = "json"
    ): ArchiveSearchResponse

    @GET("metadata/{identifier}")
    suspend fun getMetadata(
        @Path("identifier") identifier: String
    ): ArchiveMetadataResponse

    companion object {
        const val BASE_URL = "https://archive.org/"
    }
}
