package com.monasoftware.pascher.data.remote

import com.monasoftware.pascher.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface TraktApiService {

    @GET("movies/trending")
    suspend fun getTrendingMovies(
        @Query("extended") extended: String = "full",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): List<TraktTrendingDto>

    @GET("movies/popular")
    suspend fun getPopularMovies(
        @Query("extended") extended: String = "full",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): List<TraktMovieDto>

    @GET("movies/anticipated")
    suspend fun getAnticipatedMovies(
        @Query("extended") extended: String = "full",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): List<TraktAnticipatedDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("extended") extended: String = "full",
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
    ): List<TraktSearchDto>

    @GET("recommendations/movies")
    suspend fun getRecommendations(
        @Query("extended") extended: String = "full",
        @Header("Authorization") bearerToken: String
    ): List<TraktMovieDto>

    @GET("movies/{id}")
    suspend fun getMovieDetails(
        @Path("id") id: String,
        @Query("extended") extended: String = "full"
    ): TraktMovieDto

    companion object {
        const val BASE_URL = "https://api.trakt.tv/"
        const val API_VERSION = "2"
        const val CLIENT_ID = "CI1OCWfHCTMEpx2x-jvzFazKyvCHG1SHt-nnbvON1GY"
    }
}
