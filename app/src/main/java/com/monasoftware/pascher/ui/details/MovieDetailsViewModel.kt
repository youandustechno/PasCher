package com.monasoftware.pascher.ui.details

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.monasoftware.pascher.data.preferences.UserPreferencesRepository
import com.monasoftware.pascher.data.repository.MovieRepository
import com.monasoftware.pascher.data.watchtogether.SignalingService
import com.monasoftware.pascher.domain.model.EventType
import com.monasoftware.pascher.domain.model.Movie
import com.monasoftware.pascher.domain.model.PlaybackEvent
import com.monasoftware.pascher.domain.model.WatchSession
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

class MovieDetailsViewModel(
    private val movieId: String,
    private val movieRepository: MovieRepository,
    private val signalingService: SignalingService,
    private val userPrefs: UserPreferencesRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private var exoPlayer: ExoPlayer? = null
    private var isProcessingRemoteEvent = false
    private var heartbeatJob: Job? = null
    private var initialSyncDone = false

    private val _movie = MutableStateFlow<Movie?>(null)
    val movie: StateFlow<Movie?> = _movie.asStateFlow()

    private val _isLoading = MutableStateFlow(value = true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _joinError = MutableStateFlow<String?>(null)
    val joinError: StateFlow<String?> = _joinError.asStateFlow()

    private val _isStartingSession = MutableStateFlow(false)
    val isStartingSession: StateFlow<Boolean> = _isStartingSession.asStateFlow()

    val watchSession: StateFlow<WatchSession?> = signalingService.currentSession
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val isCoWatchingEnabled: StateFlow<Boolean> = userPrefs.isExperimentalFeatureEnabledFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val currentUserName: StateFlow<String> = userPrefs.displayNameFlow
        .stateIn(viewModelScope, SharingStarted.Eagerly, "")

    init {
        loadMovie()
        observePlaybackEvents()
        observeSessionForSync()
    }

    private fun observeSessionForSync() {
        viewModelScope.launch {
            watchSession.collectLatest { session ->
                Log.d("MovieDetailsVM", "WatchSession update: ${session?.sessionId ?: "NULL"}")
                if (session != null) {
                    val userName = userPrefs.displayNameFlow.first()
                    // If we are in a session but our name is gone, we've been dropped or left
                    if (!session.participants.contains(userName)) {
                        leaveWatchTogether()
                        return@collectLatest
                    }

                    if (!initialSyncDone) {
                        exoPlayer?.let { player ->
                            isProcessingRemoteEvent = true
                            player.seekTo(session.positionMs)
                            if (session.state == com.monasoftware.pascher.domain.model.PlaybackState.PLAYING) {
                                player.play()
                            } else {
                                player.pause()
                            }
                            isProcessingRemoteEvent = false
                            initialSyncDone = true
                            startHeartbeat()
                        }
                    }
                } else {
                    initialSyncDone = false
                    stopHeartbeat()
                }
            }
        }
    }

    private fun startHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = viewModelScope.launch {
            while (true) {
                delay(5.seconds)
                val session = watchSession.value
                val player = exoPlayer
                if (session != null && player != null) {
                    val userName = userPrefs.displayNameFlow.first()
                    // Only the host (first participant) updates the "global" position periodically
                    if (session.hostName == userName) {
                        signalingService.updateSessionState(player.currentPosition, player.isPlaying)
                    }
                }
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun observePlaybackEvents() {
        viewModelScope.launch {
            signalingService.playbackEvents.collectLatest { event ->
                val userName = userPrefs.displayNameFlow.first()
                if (event.senderName != userName) {
                    isProcessingRemoteEvent = true
                    exoPlayer?.let { player ->
                        when (event.type) {
                            EventType.PLAY -> player.play()
                            EventType.PAUSE -> player.pause()
                            EventType.SEEK -> player.seekTo(event.positionMs)
                        }
                    }
                    isProcessingRemoteEvent = false
                }
            }
        }
    }

    fun onLocalPlayPause(isPlaying: Boolean) {
        if (isProcessingRemoteEvent) return
        viewModelScope.launch {
            val userName = userPrefs.displayNameFlow.first()
            val session = watchSession.value
            // Only the host can send playback events
            if (session != null && session.hostName != userName) return@launch

            signalingService.sendPlaybackEvent(
                PlaybackEvent(
                    type = if (isPlaying) EventType.PLAY else EventType.PAUSE,
                    positionMs = exoPlayer?.currentPosition ?: 0L,
                    senderName = userName
                )
            )
        }
    }

    fun onLocalSeek(positionMs: Long) {
        if (isProcessingRemoteEvent) return
        viewModelScope.launch {
            val userName = userPrefs.displayNameFlow.first()
            val session = watchSession.value
            // Only the host can send seek events
            if (session != null && session.hostName != userName) return@launch

            signalingService.sendPlaybackEvent(
                PlaybackEvent(
                    type = EventType.SEEK,
                    positionMs = positionMs,
                    senderName = userName
                )
            )
        }
    }

    fun startWatchTogether() {
        viewModelScope.launch {
            _isStartingSession.value = true
            try {
                val userName = userPrefs.displayNameFlow.first()
                val movie = _movie.value ?: return@launch
                signalingService.createSession(userName, movie.id, movie.title, movie.videoUrl)
            } catch (e: Exception) {
                _joinError.value = "Failed to start co-watching session: ${e.message}"
            } finally {
                _isStartingSession.value = false
            }
        }
    }

    fun joinWatchTogether(sessionId: String) {
        viewModelScope.launch {
            val userName = userPrefs.displayNameFlow.first()
            val success = signalingService.joinSession(sessionId, userName)
            if (!success) {
                _joinError.value = "Could not join session. It might be full (max 5) or the code is invalid."
            }
        }
    }

    fun removeParticipant(name: String) {
        viewModelScope.launch {
            val session = watchSession.value ?: return@launch
            signalingService.removeParticipant(session.sessionId, name)
        }
    }

    fun clearJoinError() {
        _joinError.value = null
    }

    fun leaveWatchTogether() {
        viewModelScope.launch {
            signalingService.leaveSession()
        }
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
