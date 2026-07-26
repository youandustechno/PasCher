package com.monasoftware.pascher.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.domain.model.Movie
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _movies = MutableStateFlow<List<Movie>>(emptyList())
    private val _filteredMovies = MutableStateFlow<List<Movie>>(emptyList())
    val filteredMovies: StateFlow<List<Movie>> = _filteredMovies.asStateFlow()

    private val _selectedMovieId = MutableStateFlow<String?>(null)
    val selectedMovieId: StateFlow<String?> = _selectedMovieId.asStateFlow()

    init {
        movieRepository.getMovies()
            .onEach { _movies.value = it }
            .launchIn(viewModelScope)

        combine(_movies, _searchQuery) { movies, query ->
            if (query.isBlank()) {
                movies
            } else {
                movies.filter { 
                    it.title.contains(query, ignoreCase = true) || 
                    it.genre.contains(query, ignoreCase = true) 
                }
            }
        }.onEach { _filteredMovies.value = it }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            movieRepository.refreshMovies()
        }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onMovieSelected(movieId: String?) {
        _selectedMovieId.value = movieId
    }
}
