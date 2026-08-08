package com.monasoftware.pascher.ui.search

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.monasoftware.pascher.domain.model.Movie
import com.monasoftware.pascher.ui.LastRoute
import com.monasoftware.pascher.ui.components.findActivity
import com.monasoftware.pascher.ui.discovery.DiscoveryViewModel


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSubscription: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val movies by viewModel.filteredMovies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isCoWatchingEnabled by viewModel.isCoWatchingEnabled.collectAsState()
    val watchSession by viewModel.watchSession.collectAsState()
    var showJoinDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    LastRoute.route = com.monasoftware.pascher.ui.navigation.NavKey.Discovery

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.navigateToMovie.collect { movieId ->
            onNavigateToDetail(movieId)
        }
    }

    if(isLandscape) {
        Row(Modifier.fillMaxSize()) {

            MovieDiscoveryList(
                movies = movies,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onMovieClick = { movie ->
                    onNavigateToDetail(movie.id)
                },
                onSubscriptionClick = onNavigateToSubscription,
                onSettingsClick = onNavigateToSettings,
                isCoWatchingEnabled = isCoWatchingEnabled,
                onJoinSessionClick = { showJoinDialog = true },
                watchSession = watchSession,
                onLeaveSession = viewModel::leaveSession
            )
            Box(modifier = Modifier.weight(1f)) {
                val movieId = navigator.currentDestination?.contentKey?: movies.firstOrNull()?.id
                if (movieId != null) {
                    AdaptiveDetailPane(
                        movieId = movieId
                    )
                } else {
                    EmptyDetailPane()
                }
            }
        }
    }
    else {
        MovieDiscoveryList(
            movies = movies,
            searchQuery = searchQuery,
            onSearchQueryChange = viewModel::onSearchQueryChange,
            onMovieClick = { movie ->
                onNavigateToDetail(movie.id)
            },
            onSubscriptionClick = onNavigateToSubscription,
            onSettingsClick = onNavigateToSettings,
            isCoWatchingEnabled = isCoWatchingEnabled,
            onJoinSessionClick = { showJoinDialog = true },
            watchSession = watchSession,
            onLeaveSession = viewModel::leaveSession
        )
    }

    if (showJoinDialog) {
        com.monasoftware.pascher.ui.discovery.JoinSessionDialog(
            onDismiss = { showJoinDialog = false },
            onJoin = { code ->
                viewModel.joinSession(code)
                showJoinDialog = false
            }
        )
    }
}

@Suppress("UNCHECKED_CAST")
@Composable
fun AdaptiveDetailPane(
    movieId: String,
) {
    val container = (LocalContext.current.applicationContext as com.monasoftware.pascher.PasCherApplication).container
    val movieDetailsViewModel: com.monasoftware.pascher.ui.details.MovieDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = movieId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.monasoftware.pascher.ui.details.MovieDetailsViewModel(
                    movieId = movieId,
                    movieRepository = container.movieRepository,
                    signalingService = container.signalingService,
                    userPrefs = container.userPreferencesRepository,
                    savedStateHandle = androidx.lifecycle.SavedStateHandle()
                ) as T
            }
        }
    )
    com.monasoftware.pascher.ui.details.MovieDetailsContent(
        movie = movieDetailsViewModel.movie.collectAsState().value ?: return,
        exoPlayer = movieDetailsViewModel.getExoPlayer(LocalContext.current)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDiscoveryList(
    movies: List<Movie>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSubscriptionClick: () -> Unit,
    onSettingsClick: () -> Unit,
    isCoWatchingEnabled: Boolean,
    onJoinSessionClick: () -> Unit,
    watchSession: com.monasoftware.pascher.domain.model.WatchSession?,
    onLeaveSession: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp, horizontal = 5.dp),
                        placeholder = { Text("Search movies...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        shape = MaterialTheme.shapes.medium,
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        singleLine = true
                    )
                },
                actions = {
                    if (isCoWatchingEnabled) {
                        IconButton(onClick = onJoinSessionClick) {
                            Icon(Icons.Default.Group, contentDescription = "Join Session")
                        }
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = TopAppBarDefaults.windowInsets
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onSubscriptionClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Star, contentDescription = "Subscriptions")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (watchSession != null) {
                com.monasoftware.pascher.ui.components.WatchSessionBanner(
                    session = watchSession,
                    onLeave = onLeaveSession
                )
            }
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 80.dp
                ),
                modifier = Modifier.fillMaxSize()
            ) {
                items(movies) { movie ->
                    MovieCard(movie = movie) { onMovieClick(movie) }
                }
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Log.d("Movie", movie.thumbnailUrl)
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = androidx.compose.material3.CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(movie.thumbnailUrl)
                    .crossfade(true)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build(),
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = movie.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )

                Text(
                    text = ""+movie.releaseYear,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = movie.genre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun EmptyDetailPane() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
        Text("Select a movie to see details", style = MaterialTheme.typography.bodyLarge)
    }
}
