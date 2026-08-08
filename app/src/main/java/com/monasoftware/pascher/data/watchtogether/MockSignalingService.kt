package com.monasoftware.pascher.data.watchtogether

import com.monasoftware.pascher.domain.model.EventType
import com.monasoftware.pascher.domain.model.PlaybackEvent
import com.monasoftware.pascher.domain.model.PlaybackState
import com.monasoftware.pascher.domain.model.WatchSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

class MockSignalingService : SignalingService {
    private val _currentSession = MutableStateFlow<WatchSession?>(null)
    override val currentSession: Flow<WatchSession?> = _currentSession.asStateFlow()

    private val _playbackEvents = MutableSharedFlow<PlaybackEvent>()
    override val playbackEvents: Flow<PlaybackEvent> = _playbackEvents.asSharedFlow()

    override suspend fun createSession(hostName: String, movieId: String, movieTitle: String, movieUrl: String): String {
        val sessionId = (100000..999999).random().toString()
        val session = WatchSession(
            sessionId = sessionId,
            movieId = movieId,
            hostName = hostName,
            movieTitle = movieTitle,
            movieUrl = movieUrl,
            participants = listOf(hostName)
        )
        _currentSession.value = session
        return sessionId
    }

    override suspend fun joinSession(sessionId: String, participantName: String): Boolean {
        // In a real implementation, we would check if sessionId exists in the DB
        val existingSession = _currentSession.value ?: WatchSession(
            sessionId = sessionId,
            movieId = "MOCK_MOVIE_ID",
            hostName = "Remote Host",
            movieTitle = "Sync Movie",
            movieUrl = "",
            participants = listOf("Remote Host")
        )
        
        _currentSession.value = existingSession.copy(
            participants = existingSession.participants + participantName
        )
        return true
    }

    override suspend fun leaveSession() {
        _currentSession.value = null
    }

    override suspend fun removeParticipant(sessionId: String, participantName: String) {
        val existingSession = _currentSession.value ?: return
        _currentSession.value = existingSession.copy(
            participants = existingSession.participants - participantName
        )
    }

    override suspend fun sendPlaybackEvent(event: PlaybackEvent) {
        _playbackEvents.emit(event)
    }

    override suspend fun updateSessionState(positionMs: Long, isPlaying: Boolean) {
        _currentSession.value = _currentSession.value?.copy(
            positionMs = positionMs,
            state = if (isPlaying) PlaybackState.PLAYING else PlaybackState.PAUSED
        )
    }
}
