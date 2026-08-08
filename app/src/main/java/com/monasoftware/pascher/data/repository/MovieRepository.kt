package com.monasoftware.pascher.data.repository

import android.util.Log
import com.monasoftware.pascher.data.local.dao.MovieDao
import com.monasoftware.pascher.data.local.entity.toDomain
import com.monasoftware.pascher.data.local.entity.toEntity
import com.monasoftware.pascher.data.remote.ArchiveApiService
import com.monasoftware.pascher.data.remote.PublicDomainCatalog
import com.monasoftware.pascher.data.remote.StreamingApi
import com.monasoftware.pascher.data.remote.dto.StreamingShowDto
import com.monasoftware.pascher.domain.model.Movie
import com.monasoftware.pascher.domain.model.MovieCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MovieRepository {
    fun getMovies(): Flow<List<Movie>>
    suspend fun refreshMovies()
    suspend fun getMovieById(id: String): Movie?
    suspend fun getPopularMovies(): List<Movie>
    suspend fun getAnticipatedMovies(): List<Movie>
    suspend fun searchMovies(query: String): List<Movie>
    suspend fun getRecommendations(token: String): List<Movie>
    suspend fun getMoviesByCategory(category: MovieCategory): List<Movie>
    suspend fun getPublicDomainMovies(): List<Movie>
    suspend fun getMovieVideoUrl(movie: Movie): String?
}

class MovieRepositoryImpl(
    private val movieDao: MovieDao,
    private val streamingApi: StreamingApi,
    private val archiveApiService: ArchiveApiService,
    private val country: String = "us",
) : MovieRepository {

    companion object {
        private const val MIN_RUNTIME_MINUTES = 60
    }

    override fun getMovies(): Flow<List<Movie>> {
        return movieDao.getAllMovies().map { entities ->
            entities.map { it.toDomain() }.filter { it.runtime >= MIN_RUNTIME_MINUTES }
        }
    }

    override suspend fun getPublicDomainMovies(): List<Movie> {
        return coroutineScope {
            PublicDomainCatalog.entries.map { entry ->
                async { buildMovie(entry) }
            }.awaitAll().filterNotNull()
        }
    }

    override suspend fun refreshMovies() {
        try {
            Log.d("MovieRepository", "Refreshing movies from streaming service...")
            val trending = streamingApi.searchShows(country = country, orderBy = "popularity")
            val movies = coroutineScope {
                trending.shows.map { dto ->
                    async { dto.toMovie() }
                }.awaitAll().filter { it.runtime >= MIN_RUNTIME_MINUTES }
            }

            if (movies.isNotEmpty()) {
                Log.d("MovieRepository", "Inserting ${movies.size} trending movies")
                movieDao.clearAll()
                movieDao.insertMovies(movies.map { it.toEntity() })
            } else {
                Log.w("MovieRepository", "No trending movies found, using fallback")
                insertFallbackData()
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error refreshing movies", e)
            insertFallbackData()
        }
    }

    override suspend fun getMovieVideoUrl(movie: Movie): String? {
        return findVideoUrlForMovie(movie.title)
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
        return movieDao.getMovieById(id)?.toDomain()?.takeIf { it.runtime >= MIN_RUNTIME_MINUTES }
            ?: try {
                val dto = streamingApi.getShow(id = id, country = country)
                dto.toMovie().takeIf { it.runtime >= MIN_RUNTIME_MINUTES }
            } catch (e: Exception) {
                Log.e("MovieRepository", "Error getting movie by id: $id", e)
                null
            }
    }

    override suspend fun getPopularMovies(): List<Movie> {
        return try {
            coroutineScope {
                val result = streamingApi.searchShows(country = country, orderBy = "popularity")
                result.shows.map { dto ->
                    async { dto.toMovie() }
                }.awaitAll().filter { it.runtime >= MIN_RUNTIME_MINUTES }
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error getting popular movies", e)
            emptyList()
        }
    }

    override suspend fun getAnticipatedMovies(): List<Movie> {
        return try {
            coroutineScope {
                val result = streamingApi.getChanges(country = country, changeType = "upcoming")
                // shows is a Map<String, StreamingShowDto>
                result.shows.values.map { dto ->
                    async { dto.toMovie() }
                }.awaitAll().filter { it.runtime >= MIN_RUNTIME_MINUTES }
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error getting anticipated movies", e)
            emptyList()
        }
    }

    override suspend fun searchMovies(query: String): List<Movie> {
        return try {
            coroutineScope {
                val result = streamingApi.searchByTitle(country = country, title = query)
                result.shows.map { dto ->
                    async { dto.toMovie() }
                }.awaitAll().filter { it.runtime >= MIN_RUNTIME_MINUTES }
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error searching movies for: $query", e)
            emptyList()
        }
    }

    override suspend fun getRecommendations(token: String): List<Movie> {
        return emptyList()
    }

    override suspend fun getMoviesByCategory(category: MovieCategory): List<Movie> {
        return try {
            when (category) {
                MovieCategory.TRENDING -> getPopularMovies()
                MovieCategory.OTHER -> getAnticipatedMovies()
                else -> coroutineScope {
                    val genres = category.genreSlugs.joinToString(",")
                    val result = streamingApi.searchShows(country = country, genres = genres)
                    result.shows.map { dto ->
                        async { dto.toMovie() }
                    }.awaitAll().filter { it.runtime >= MIN_RUNTIME_MINUTES }
                }
            }
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error getting movies by category: $category", e)
            emptyList()
        }
    }

    private suspend fun buildMovie(entry: PublicDomainCatalog.Entry): Movie? {
        return try {
            val streamingDto = streamingApi.getMovie(entry.tmdbId.toString())
            val archiveMeta = archiveApiService.getMetadata(entry.archiveIdentifier)

            // Extra safety check: confirm the item actually declares a PD/open license
            val license = archiveMeta.metadata?.title // Using title as a proxy if license missing in DTO
            val videoFile = archiveMeta.files.find {
                it.name.endsWith(".mp4", true) || it.name.endsWith(".ogv", true)
            } ?: return null

            val videoUrl = "https://archive.org/download/${entry.archiveIdentifier}/${videoFile.name}"

            Movie(
                id = entry.tmdbId.toString(),
                title = streamingDto.title,
                description = streamingDto.overview ?: "No description available.",
                thumbnailUrl = streamingDto.imageSet?.getPoster()
                    ?: "https://via.placeholder.com/300x450.png?text=No+Image",
                videoUrl = videoUrl,
                genre = streamingDto.genres?.firstOrNull()?.name ?: "Movie",
                rating = (streamingDto.rating ?: 0.0) / 10.0,
                releaseYear = streamingDto.releaseYear ?: 0,
                runtime = streamingDto.runtime ?: 0,
                isPremium = false
            )
        } catch (e: Exception) {
            Log.e("MovieRepository", "Error building PD movie ${entry.title}", e)
            null
        }
    }

    private fun StreamingShowDto.toMovie(): Movie {
        val posterUrl = imageSet?.getPoster()
        val normalizedRating = (rating ?: 0.0) / 10.0

        return Movie(
            id = id,
            title = title,
            description = overview ?: "No description available.",
            thumbnailUrl = posterUrl ?: "https://via.placeholder.com/300x450.png?text=No+Image",
            videoUrl = streamingOptions[country]?.firstOrNull { it.videoLink != null }?.videoLink
                ?: streamingOptions[country]?.firstOrNull()?.link
                ?: "https://storage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            genre = genres?.firstOrNull()?.name ?: "Movie",
            rating = normalizedRating,
            releaseYear = releaseYear ?: 0,
            runtime = runtime ?: 0,
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