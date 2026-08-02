package com.monasoftware.pascher.data.repository

import android.util.Log
import com.monasoftware.pascher.data.local.dao.MovieDao
import com.monasoftware.pascher.data.local.entity.toDomain
import com.monasoftware.pascher.data.local.entity.toEntity
import com.monasoftware.pascher.data.remote.ArchiveApiService
import com.monasoftware.pascher.data.remote.TraktApiService
import com.monasoftware.pascher.data.remote.dto.TraktMovieDto
import com.monasoftware.pascher.domain.model.Movie
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.net.URLEncoder

interface MovieRepository {
    fun getMovies(): Flow<List<Movie>>
    suspend fun refreshMovies()
    suspend fun getMovieById(id: String): Movie?
    suspend fun getPopularMovies(): List<Movie>
    suspend fun getAnticipatedMovies(): List<Movie>
    suspend fun searchMovies(query: String): List<Movie>
    suspend fun getRecommendations(token: String): List<Movie>
}

class MovieRepositoryImpl(
    private val movieDao: MovieDao,
    private val traktApiService: TraktApiService,
    private val archiveApiService: ArchiveApiService
) : MovieRepository {

    override fun getMovies(): Flow<List<Movie>> {
        return movieDao.getAllMovies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshMovies() {
        try {
            val trendingMovies = traktApiService.getTrendingMovies()
            val movies = coroutineScope {
                trendingMovies.map { trendingDto ->
                    async {
                        trendingDto.movie.toMovie()
                    }
                }.awaitAll()
            }

            if (movies.isNotEmpty()) {
                movieDao.clearAll()
                movieDao.insertMovies(movies.map { it.toEntity() })
            } else {
                insertFallbackData()
            }
        } catch (e: Exception) {
            insertFallbackData()
            e.printStackTrace()
        }
    }

    private suspend fun findVideoUrlForMovie(title: String): String? {
        return try {
            val searchResponse = archiveApiService.searchMovies(query = "title:\"$title\" AND mediatype:movies")
            val identifier = searchResponse.response.docs.firstOrNull()?.identifier ?: return null
            val metadata = archiveApiService.getMetadata(identifier)
            val videoFile = metadata.files.find { it.name.endsWith(".mp4", ignoreCase = true) }
            if (videoFile != null) {
                "https://archive.org/download/$identifier/${videoFile.name}"
            } else null
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getMovieById(id: String): Movie? {
        // First try local
        val local = movieDao.getMovieById(id)
        if (local != null) return local.toDomain()
        
        // Then remote
        return try {
            traktApiService.getMovieDetails(id).toMovie()
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun getPopularMovies(): List<Movie> {
        return try {
            coroutineScope {
                traktApiService.getPopularMovies().map { dto ->
                    async { dto.toMovie() }
                }.awaitAll()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getAnticipatedMovies(): List<Movie> {
        return try {
            coroutineScope {
                traktApiService.getAnticipatedMovies().map { dto ->
                    async { dto.movie.toMovie() }
                }.awaitAll()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun searchMovies(query: String): List<Movie> {
        return try {
            coroutineScope {
                traktApiService.searchMovies(query).map { dto ->
                    async { dto.movie.toMovie() }
                }.awaitAll()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    override suspend fun getRecommendations(token: String): List<Movie> {
        return try {
            coroutineScope {
                traktApiService.getRecommendations(bearerToken = "Bearer $token").map { dto ->
                    async { dto.toMovie() }
                }.awaitAll()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun TraktMovieDto.toMovie(): Movie {
        val videoUrl = findVideoUrlForMovie(title)
        val thumbnailUrl = images?.poster?.firstOrNull()?.let { "https://$it" }

        Log.d("MovieRepository", "Thumbnail for '$title' -> $thumbnailUrl")

        return Movie(
            id = ids.trakt.toString(),
            title = title,
            description = overview ?: "No description available.",
            thumbnailUrl = thumbnailUrl
                ?: "https://via.placeholder.com/300x450.png?text=No+Image",
            videoUrl = videoUrl ?: trailer ?: "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            genre = genres?.firstOrNull() ?: "Movie",
            rating = rating ?: 0.0,
            releaseYear = year ?: 0,
            isPremium = false
        )
    }
    private suspend fun insertFallbackData() {
        val fallbackMovies = listOf(
            Movie(
                id = "1",
                title = "Interstellar",
                description = "When a mysterious wormhole opens, a group of astronauts must venture beyond the stars to find a new home for humanity.",
                thumbnailUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=2094&auto=format&fit=crop",
                videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                genre = "Sci-Fi",
                rating = 8.7,
                releaseYear = 2014,
                isPremium = true
            ),
            Movie(
                id = "2",
                title = "The Dark Knight",
                description = "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice.",
                thumbnailUrl = "https://images.unsplash.com/photo-1478720568477-152d9b164e26?q=80&w=2070&auto=format&fit=crop",
                videoUrl = "https://storage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                genre = "Action",
                rating = 9.0,
                releaseYear = 2008,
                isPremium = false
            )
        )
        movieDao.insertMovies(fallbackMovies.map { it.toEntity() })
    }
}
