package com.monasoftware.pascher.ui.details

import android.content.pm.ActivityInfo
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.media3.exoplayer.ExoPlayer
import com.monasoftware.pascher.domain.model.Movie
import com.monasoftware.pascher.domain.model.WatchSession
import com.monasoftware.pascher.ui.LastRoute
import com.monasoftware.pascher.ui.components.VideoPlayer
import com.monasoftware.pascher.ui.components.WatchSessionBanner
import com.monasoftware.pascher.ui.components.findActivity

import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsScreen(
    viewModel: MovieDetailsViewModel,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    BackHandler(onBack = {
        if (isLandscape) {
            val activity = context.findActivity()
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        } else {
            onBackClick()
        }
    })

    val movie by viewModel.movie.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val watchSession by viewModel.watchSession.collectAsState()
    val isCoWatchingEnabled by viewModel.isCoWatchingEnabled.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()
    val joinError by viewModel.joinError.collectAsState()
    var showWatchTogetherDialog by remember { mutableStateOf(false) }

    val isHost = watchSession?.hostName == currentUserName
    val isCoWatching = watchSession != null

    LastRoute.route = com.monasoftware.pascher.ui.navigation.NavKey.MovieDetail(movie?.id ?: "")

    if (joinError != null) {
        AlertDialog(
            onDismissRequest = { viewModel.clearJoinError() },
            title = { Text("Error") },
            text = { Text(joinError!!) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearJoinError() }) {
                    Text("OK")
                }
            }
        )
    }

    if (isLandscape) {
        // Full immersive mode - show only video
        Box(modifier = Modifier.fillMaxSize()) {
            VideoPlayer(
                exoPlayer = viewModel.getExoPlayer(LocalContext.current),
                modifier = Modifier.fillMaxSize(),
                useController = !isCoWatching || isHost,
                onPlayPause = viewModel::onLocalPlayPause,
                onSeek = viewModel::onLocalSeek
            )
            if (isCoWatching) {
                Surface(
                    color = if (isHost) Color.Black.copy(alpha = 0.5f) else MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
                    contentColor = Color.White,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 8.dp),
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = if (isHost) "Sharing Screen (ID: ${watchSession!!.sessionId})" else "Watching Live Screen",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
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
                    actions = {
                        if (isCoWatchingEnabled) {
                            IconButton(onClick = { showWatchTogetherDialog = true }) {
                                Icon(
                                    imageVector = if (watchSession != null) Icons.Default.Group else Icons.Default.GroupAdd,
                                    contentDescription = "Watch Together",
                                    tint = if (watchSession != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        actionIconContentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    windowInsets = TopAppBarDefaults.windowInsets
                )
            }
        ) { padding ->
            if (isLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                movie?.let { movie ->
                    Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                        if (watchSession != null) {
                            WatchSessionBanner(session = watchSession!!, onLeave = viewModel::leaveWatchTogether)
                        }
                        MovieDetailsContent(
                            movie = movie,
                            exoPlayer = viewModel.getExoPlayer(LocalContext.current),
                            useController = !isCoWatching || isHost,
                            onPlayPause = viewModel::onLocalPlayPause,
                            onSeek = viewModel::onLocalSeek,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }

    if (showWatchTogetherDialog) {
        WatchTogetherDialog(
            session = watchSession,
            currentUserName = currentUserName,
            onDismiss = { showWatchTogetherDialog = false },
            onCreateSession = {
                viewModel.startWatchTogether()
                showWatchTogetherDialog = false
            },
            onJoinSession = { code ->
                viewModel.joinWatchTogether(code)
                showWatchTogetherDialog = false
            },
            onRemoveParticipant = viewModel::removeParticipant
        )
    }
}

@Composable
fun WatchTogetherDialog(
    session: WatchSession?,
    currentUserName: String,
    onDismiss: () -> Unit,
    onCreateSession: () -> Unit,
    onJoinSession: (String) -> Unit,
    onRemoveParticipant: (String) -> Unit
) {
    var joinCode by remember { mutableStateOf("") }
    val isHost = session?.hostName == currentUserName

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Watch Together") },
        text = {
            Column {
                if (session == null) {
                    Text("Start a new session to invite friends, or join an existing one with a code. (Max 5 people)")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = joinCode,
                        onValueChange = { if (it.length <= 6) joinCode = it },
                        label = { Text("Enter 6-digit Code") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                } else {
                    Text(
                        text = if (isHost) "You are sharing your screen." else "You are watching ${session.hostName}'s screen.",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Session ID: ${session.sessionId}", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Participants (${session.participants.size}/5):")
                    session.participants.forEach { participant ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (participant == currentUserName) "$participant (You)" else participant,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (isHost && participant != currentUserName) {
                                IconButton(onClick = { onRemoveParticipant(participant) }) {
                                    Icon(
                                        imageVector = Icons.Default.PersonRemove,
                                        contentDescription = "Remove Participant",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
                    }
                    if (isHost) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Share this code with your friends!", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            if (session == null) {
                Button(
                    onClick = { onJoinSession(joinCode) },
                    enabled = joinCode.length == 6
                ) {
                    Text("Join Session")
                }
            }
        },
        dismissButton = {
            if (session == null) {
                TextButton(onClick = onCreateSession) {
                    Text("Start New Session")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Close")
                }
            }
        }
    )
}

@Composable
fun MovieDetailsContent(
    movie: Movie,
    exoPlayer: ExoPlayer,
    modifier: Modifier = Modifier,
    useController: Boolean = true,
    onPlayPause: ((Boolean) -> Unit)? = null,
    onSeek: ((Long) -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        VideoPlayer(
            exoPlayer = exoPlayer,
            modifier = Modifier.fillMaxWidth(),
            useController = useController,
            onPlayPause = onPlayPause,
            onSeek = onSeek
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${movie.genre} • ${movie.releaseYear} • ${movie.runtime} min • ⭐ ${String.format(Locale.getDefault(), "%.1f", movie.rating)}",
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
