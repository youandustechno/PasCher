package com.monasoftware.pascher.data.remote

import com.monasoftware.pascher.domain.model.Movie
import retrofit2.http.GET

interface MovieApiService {
    @GET("movies")
    suspend fun getMovies(): List<Movie>

    companion object {
        const val BASE_URL = "https://api.pascher-movies.com/" // Mock URL
    }
}
