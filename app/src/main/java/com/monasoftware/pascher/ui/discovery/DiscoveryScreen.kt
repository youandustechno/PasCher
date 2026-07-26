package com.monasoftware.pascher.ui.discovery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.monasoftware.pascher.domain.model.Movie
import kotlinx.coroutines.launch

// In DiscoveryScreen function, modify the ListDetailPaneScaffold:

@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSubscription: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val movies by viewModel.filteredMovies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val scope = rememberCoroutineScope()
    var isImmersiveFullscreen by remember { mutableStateOf(false) }

    if (isImmersiveFullscreen) {
        // Hide list pane entirely - show only the video, full screen
        val movieId = navigator.currentDestination?.contentKey as? String
        if (movieId != null) {
            AdaptiveDetailPane(
                movieId = movieId,
                onImmersiveFullscreenChanged = { immersive -> isImmersiveFullscreen = immersive }
            )
        }
    } else {
        ListDetailPaneScaffold(
            directive = navigator.scaffoldDirective,
            value = navigator.scaffoldValue,
            listPane = {
                MovieDiscoveryList(
                    movies = movies,
                    searchQuery = searchQuery,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onMovieClick = { movie ->
                        if (navigator.scaffoldDirective.maxHorizontalPartitions > 1) {
                            scope.launch {
                                navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, movie.id)
                            }
                        } else {
                            onNavigateToDetail(movie.id)
                        }
                    },
                    onSubscriptionClick = onNavigateToSubscription
                )
            },
            detailPane = {
                val movieId = navigator.currentDestination?.contentKey as? String
                if (movieId != null) {
                    AdaptiveDetailPane(
                        movieId = movieId,
                        onImmersiveFullscreenChanged = { immersive -> isImmersiveFullscreen = immersive }
                    )
                } else {
                    EmptyDetailPane()
                }
            }
        )
    }
}

@Composable
fun AdaptiveDetailPane(
    movieId: String,
    onImmersiveFullscreenChanged: (Boolean) -> Unit = {}
) {
    val container = (LocalContext.current.applicationContext as com.monasoftware.pascher.PasCherApplication).container
    val movieDetailsViewModel: com.monasoftware.pascher.ui.details.MovieDetailsViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
        key = movieId,
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return com.monasoftware.pascher.ui.details.MovieDetailsViewModel(
                    movieId = movieId,
                    movieRepository = container.movieRepository
                ) as T
            }
        }
    )
    com.monasoftware.pascher.ui.details.MovieDetailsContent(
        movie = movieDetailsViewModel.movie.collectAsState().value ?: return,
        onImmersiveFullscreenChanged = onImmersiveFullscreenChanged
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDiscoveryList(
    movies: List<Movie>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onSubscriptionClick: () -> Unit
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
                            .padding(vertical = 8.dp),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
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
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            contentPadding = PaddingValues(
                start = 16.dp,
                top = padding.calculateTopPadding() + 16.dp,
                end = 16.dp,
                bottom = padding.calculateBottomPadding() + 80.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            items(movies) { movie ->
                MovieCard(movie = movie, onClick = { onMovieClick(movie) })
            }
        }
    }
}

@Composable
fun MovieCard(movie: Movie, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable(onClick = onClick)
    ) {
        Column {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(2f / 3f),
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
                    text = movie.genre,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
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
