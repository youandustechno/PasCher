package com.monasoftware.pascher.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.data.watchtogether.SignalingService
import com.monasoftware.pascher.domain.model.Movie
import com.monasoftware.pascher.domain.model.MovieCategory
import com.monasoftware.pascher.domain.model.WatchSession
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DiscoveryViewModel(
    private val movieRepository: MovieRepository,
    private val signalingService: SignalingService,
    private val userPrefs: UserPreferencesRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _categorizedMovies = MutableStateFlow<Map<MovieCategory, List<Movie>>>(emptyMap())
    val categorizedMovies: StateFlow<Map<MovieCategory, List<Movie>>> = _categorizedMovies.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _filteredMovies = MutableStateFlow<List<Movie>>(emptyList())
    val filteredMovies: StateFlow<List<Movie>> = _filteredMovies.asStateFlow()

    val isCoWatchingEnabled: StateFlow<Boolean> = userPrefs.isExperimentalFeatureEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val watchSession: StateFlow<WatchSession?> = signalingService.currentSession
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _navigateToMovie = MutableSharedFlow<String>()
    val navigateToMovie: SharedFlow<String> = _navigateToMovie.asSharedFlow()

    init {
        loadCategories()

        _searchQuery
            .onEach { query -> _filteredMovies.value = runSearch(query) }
            .launchIn(viewModelScope)
    }

    fun joinSession(code: String) {
        viewModelScope.launch {
            val userName = userPrefs.displayNameFlow.first()
            val success = signalingService.joinSession(code, userName)
            if (success) {
                val session = signalingService.currentSession.first()
                session?.movieId?.let { movieId ->
                    _navigateToMovie.emit(movieId)
                }
            }
        }
    }

    fun leaveSession() {
        viewModelScope.launch {
            signalingService.leaveSession()
        }
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