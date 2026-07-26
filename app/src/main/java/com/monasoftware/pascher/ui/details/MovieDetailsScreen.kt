package com.monasoftware.pascher.ui.details

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.statusBarsPadding
import com.monasoftware.pascher.domain.model.Movie
import com.monasoftware.pascher.ui.components.VideoPlayer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onBackClick: () -> Unit
) {
    val movie by viewModel.movie.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var isImmersiveFullscreen by remember { mutableStateOf(false) }

    if (isImmersiveFullscreen) {
        // Full immersive mode - show only video
        movie?.let { movie ->
            VideoPlayer(
                videoUrl = movie.videoUrl,
                modifier = Modifier.fillMaxSize(),
                onImmersiveFullscreenChanged = { immersive ->
                    isImmersiveFullscreen = immersive
                }
            )
        }
    } else {
        // Normal mode
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(movie?.title ?: "Movie Details") },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    )
                )
            }
        ) { padding ->
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                movie?.let { movie ->
                    MovieDetailsContent(
                        movie = movie,
                        modifier = Modifier.padding(padding),
                        onImmersiveFullscreenChanged = { immersive ->
                            isImmersiveFullscreen = immersive
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun MovieDetailsContent(
    movie: Movie,
    modifier: Modifier = Modifier,
    onImmersiveFullscreenChanged: (Boolean) -> Unit = {}
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        VideoPlayer(
            videoUrl = movie.videoUrl,
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding(),
            onImmersiveFullscreenChanged = onImmersiveFullscreenChanged
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${movie.genre} • ${movie.releaseYear} • ⭐ ${movie.rating}",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.secondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Synopsis",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = movie.description,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}