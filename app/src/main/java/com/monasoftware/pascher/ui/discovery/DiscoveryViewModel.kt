package com.monasoftware.pascher.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.domain.model.Movie
import com.monasoftware.pascher.domain.model.MovieCategory
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val movieRepository: MovieRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categorizedMovies = MutableStateFlow<Map<MovieCategory, List<Movie>>>(emptyMap())
    val categorizedMovies: StateFlow<Map<MovieCategory, List<Movie>>> = _categorizedMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filteredMovies = MutableStateFlow<List<Movie>>(emptyList())
    val filteredMovies: StateFlow<List<Movie>> = _filteredMovies.asStateFlow()

    init {
        loadCategories()

        _searchQuery
            .onEach { query -> _filteredMovies.value = runSearch(query) }
            .launchIn(viewModelScope)
    }

    private fun loadCategories() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                coroutineScope {
                    MovieCategory.entries.forEach { category ->
                        launch {
                            val movies = movieRepository.getMoviesByCategory(category)
                            _categorizedMovies.value = _categorizedMovies.value + (category to movies)
                        }
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun runSearch(query: String): List<Movie> {
        if (query.isBlank()) return emptyList()
        val localMatches = _categorizedMovies.value.values
            .flatten()
            .distinctBy { it.id }
            .filter {
                it.title.contains(query, ignoreCase = true) ||
                        it.genre.contains(query, ignoreCase = true)
            }
        return localMatches.ifEmpty { movieRepository.searchMovies(query) }
    }

    fun onSearchQueryChange(newQuery: String) {
        _searchQuery.value = newQuery
    }

    fun onMovieSelected(movieId: String?) {
        _selectedMovieId.value = movieId
    }

    private val _selectedMovieId = MutableStateFlow<String?>(null)
    val selectedMovieId: StateFlow<String?> = _selectedMovieId.asStateFlow()
}