package com.monasoftware.pascher.ui.details

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val movieId: String,
    private val movieRepository: MovieRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMovie()
    }

    fun getExoPlayer(context: Context): ExoPlayer {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context.applicationContext).build().apply {
                _movie.value?.let { movie ->
                    setMediaItem(MediaItem.fromUri(movie.videoUrl))
                    seekTo(getVideoPosition())
                    playWhenReady = true
                    prepare()
                }
            }
        }
        return exoPlayer!!
    }

    fun saveVideoPosition(position: Long) {
        savedStateHandle["videoPosition_$movieId"] = position
    }

    fun getVideoPosition(): Long {
        return savedStateHandle.get<Long>("videoPosition_$movieId") ?: 0L
    }

    private fun updatePlayer(movie: Movie) {
        exoPlayer?.let { player ->
            val currentUri = player.currentMediaItem?.localConfiguration?.uri?.toString()
            if (currentUri != movie.videoUrl) {
                val currentPos = if (player.mediaItemCount > 0) player.currentPosition else getVideoPosition()
                player.setMediaItem(MediaItem.fromUri(movie.videoUrl))
                player.seekTo(currentPos)
                player.playWhenReady = true
                player.prepare()
            }
        }
    }

    private fun loadMovie() {
        viewModelScope.launch {
            _isLoading.value = true
            val movie = movieRepository.getMovieById(movieId)
            _movie.value = movie
            _isLoading.value = false

            movie?.let { currentMovie ->
                // Update player if it exists
                updatePlayer(currentMovie)

                // Background fetch for high quality video URL
                val betterUrl = movieRepository.getMovieVideoUrl(currentMovie)
                if (betterUrl != null && betterUrl != currentMovie.videoUrl) {
                    val updatedMovie = currentMovie.copy(videoUrl = betterUrl)
                    _movie.value = updatedMovie
                    updatePlayer(updatedMovie)
                }
            }
        }
    }

    override fun onCleared() {
        exoPlayer?.let {
            saveVideoPosition(it.currentPosition)
            it.release()
        }
        exoPlayer = null
    }
}
