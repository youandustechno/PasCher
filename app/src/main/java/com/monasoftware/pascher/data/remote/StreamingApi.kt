package com.monasoftware.pascher.data.remote

import com.monasoftware.pascher.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface StreamingApi {

    @GET("shows/{id}")
    suspend fun getShow(
        @Path("id") id: String,
        @Query("country") country: String = "us"
    ): StreamingShowDto

    @GET("shows/{id}")
    suspend fun getMovie(
        @Path("id") id: String,
        @Query("country") country: String = "us"
    ): StreamingShowDto

    @GET("shows/search/title")
    suspend fun searchByTitle(
        @Query("country") country: String = "us",
        @Query("title") title: String,
        @Query("show_type") showType: String = "movie"
    ): SearchResult

    @GET("shows/search/filters")
    suspend fun searchShows(
        @Query("country") country: String = "us",
        @Query("show_type") showType: String = "movie",
        @Query("genres") genres: String? = null,
        @Query("order_by") orderBy: String? = "popularity",
        @Query("cursor") cursor: String? = null
    ): SearchResult

    @GET("changes")
    suspend fun getChanges(
        @Query("country") country: String = "us",
        @Query("change_type") changeType: String = "new", // "new", "updated", "upcoming"
        @Query("item_type") itemType: String = "show",
        @Query("show_type") showType: String = "movie"
    ): ChangesResult

    @GET("genres")
    suspend fun getGenres(): List<Genre>

    companion object {
        const val STREAMING_BASE_URL = "https://api.movieofthenight.com/v4/"
        const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/w500"
    }
}
