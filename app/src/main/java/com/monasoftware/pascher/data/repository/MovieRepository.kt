package com.monasoftware.pascher.data.repository

import com.monasoftware.pascher.data.local.dao.MovieDao
import com.monasoftware.pascher.data.local.entity.toDomain
import com.monasoftware.pascher.data.local.entity.toEntity
import com.monasoftware.pascher.data.remote.ArchiveApiService
import com.monasoftware.pascher.domain.model.Movie
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface MovieRepository {
    fun getMovies(): Flow<List<Movie>>
    suspend fun refreshMovies()
    suspend fun getMovieById(id: String): Movie?
}

class MovieRepositoryImpl(
    private val movieDao: MovieDao,
    private val apiService: ArchiveApiService
) : MovieRepository {

    override fun getMovies(): Flow<List<Movie>> {
        return movieDao.getAllMovies().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun refreshMovies() {
        try {
            val searchResponse = apiService.searchMovies()
            val movieIds = searchResponse.response.docs.map { it.identifier }

            val movies = coroutineScope {
                movieIds.map { id ->
                    async {
                        try {
                            val metadata = apiService.getMetadata(id)
                            val videoFile = metadata.files.find { it.name.endsWith(".mp4", ignoreCase = true) }
                            
                            if (videoFile != null) {
                                Movie(
                                    id = id,
                                    title = metadata.metadata.title ?: id,
                                    description = metadata.metadata.description ?: "No description available.",
                                    thumbnailUrl = "https://archive.org/services/img/$id",
                                    videoUrl = "https://archive.org/download/$id/${videoFile.name}",
                                    genre = metadata.metadata.genre ?: "Feature Film",
                                    rating = metadata.metadata.avgRating?.toDoubleOrNull() ?: 0.0,
                                    releaseYear = metadata.metadata.date?.take(4)?.toIntOrNull() ?: 0,
                                    isPremium = false
                                )
                            } else null
                        } catch (e: Exception) {
                            null
                        }
                    }
                }.awaitAll().filterNotNull()
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

    override suspend fun getMovieById(id: String): Movie? {
        return movieDao.getMovieById(id)?.toDomain()
    }
}
