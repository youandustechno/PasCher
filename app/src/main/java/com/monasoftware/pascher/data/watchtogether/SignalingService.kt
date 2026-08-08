package com.monasoftware.pascher.data.watchtogether

import com.monasoftware.pascher.domain.model.PlaybackEvent
import com.monasoftware.pascher.domain.model.WatchSession
import kotlinx.coroutines.flow.Flow

interface SignalingService {
    val currentSession: Flow<WatchSession?>
    val playbackEvents: Flow<PlaybackEvent>

    suspend fun createSession(hostName: String, movieId: String, movieTitle: String, movieUrl: String): String
    suspend fun joinSession(sessionId: String, participantName: String): Boolean
    suspend fun leaveSession()
    suspend fun removeParticipant(sessionId: String, participantName: String)
    suspend fun sendPlaybackEvent(event: PlaybackEvent)
    suspend fun updateSessionState(positionMs: Long, isPlaying: Boolean)
}
