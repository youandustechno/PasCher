package com.monasoftware.pascher.ui.details

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MovieDetailsViewModel(
    private val movieId: String,
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        loadMovie()
    }

    private fun loadMovie() {
        viewModelScope.launch {
            _isLoading.value = true
            _movie.value = movieRepository.getMovieById(movieId)
            _isLoading.value = false
        }
    }
}
