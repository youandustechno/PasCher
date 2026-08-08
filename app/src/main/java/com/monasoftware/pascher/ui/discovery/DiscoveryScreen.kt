package com.monasoftware.pascher.ui.discovery

import android.content.res.Configuration
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.monasoftware.pascher.domain.model.MovieCategory
import com.monasoftware.pascher.ui.LastRoute
import com.monasoftware.pascher.ui.components.findActivity


@OptIn(ExperimentalMaterial3AdaptiveApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    viewModel: DiscoveryViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSubscription: () -> Unit
) {
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val categorizedMovies by viewModel.categorizedMovies.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.filteredMovies.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    LastRoute.route = com.monasoftware.pascher.ui.navigation.NavKey.Discovery

    val isSearching = searchQuery.isNotBlank()
    val allMovies = remember(categorizedMovies) { categorizedMovies.values.flatten() }

    if (isLandscape) {
        Row(Modifier.fillMaxSize()) {
            MovieDiscoveryList(
                movies = if (isSearching) searchResults else allMovies,
                searchQuery = searchQuery,
                onSearchQueryChange = viewModel::onSearchQueryChange,
                onMovieClick = { movie -> onNavigateToDetail(movie.id) },
                onSubscriptionClick = onNavigateToSubscription
            )
            Box(modifier = Modifier.weight(1f)) {
                val movieId = navigator.currentDestination?.contentKey ?: allMovies.firstOrNull()?.id
                if (movieId != null) {
                    AdaptiveDetailPane(movieId = movieId)
                } else {
                    EmptyDetailPane()
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                TextField(
                    value = searchQuery,
                    onValueChange = viewModel::onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
                Spacer(modifier = Modifier.height(8.dp))
            }

            if (isSearching) {
                items(searchResults, key = { it.id }) { movie ->
                    // your existing search result row/card
                    MovieCard(movie = movie, onClick = { onNavigateToDetail(movie.id) })
                }
            } else {
                MovieCategory.values().forEach { category ->
                    val movies = categorizedMovies[category].orEmpty()
                    when {
                        movies.isNotEmpty() -> item(key = category.name) {
                            MovieCategoryRow(
                                title = category.displayTitle,
                                movies = movies,
                                onMovieClick = { onNavigateToDetail(it.id) }
                            )
                        }
                        isLoading -> item(key = "${category.name}_loading") {
                            MovieCategoryLoadingRow(title = category.displayTitle)
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun MovieCategoryRow(
    title: String,
    movies: List<Movie>,
    onMovieClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(movies, key = { it.id }) { movie ->
                MoviePosterCard(movie = movie, onClick = { onMovieClick(movie) }) // was MovieCard
            }
        }
    }
}

@Composable
fun MovieCategoryLoadingRow(title: String) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(4) {
                Box(
                    modifier = Modifier
                        .size(120.dp, 180.dp)
                        .background(Color.Gray.copy(alpha = 0.3f))
                )
            }
        }
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
                MovieCard(movie = movie) { onMovieClick(movie) }
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
            .fillMaxWidth()
            .height(200.dp)
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
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
//                Text(
//                    text = movie.genre,
//                    style = MaterialTheme.typography.bodySmall,
//                    color = MaterialTheme.colorScheme.secondary,
//                    maxLines = 2,
//                    overflow = TextOverflow.Ellipsis
//                )
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


@Composable
fun MoviePosterCard(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Log.d("MoviePosterCard", "Movie: ${movie.title} with URL: ${movie.videoUrl}")
    Column(
        modifier = modifier
            .width(120.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp, 180.dp)
                .clip(RoundedCornerShape(8.dp))
        ) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            if (movie.isPremium) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Premium",
                    tint = Color.Yellow,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = movie.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 2.dp)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(2.dp))
            Text(
                text = String.format("%.1f", movie.rating),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Composable
fun MovieListItem(
    movie: Movie,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(72.dp, 108.dp)
                .clip(RoundedCornerShape(6.dp))
        ) {
            AsyncImage(
                model = movie.thumbnailUrl,
                contentDescription = movie.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${movie.genre} • ${movie.releaseYear}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.1f", movie.rating),
                    style = MaterialTheme.typography.labelMedium
                )

                if (movie.isPremium) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Filled.WorkspacePremium,
                        contentDescription = "Premium",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Icon(
            imageVector = Icons.Filled.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}