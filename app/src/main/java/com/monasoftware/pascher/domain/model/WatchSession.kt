package com.monasoftware.pascher.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WatchSession(
    val sessionId: String = "",
    val movieId: String = "",
    val hostName: String = "",
    val movieTitle: String = "",
    val movieUrl: String = "",
    val state: PlaybackState = PlaybackState.PAUSED,
    val positionMs: Long = 0L,
    val participants: List<String> = emptyList()
)

@Serializable
enum class PlaybackState {
    PLAYING, PAUSED, BUFFERING
}

@Serializable
data class PlaybackEvent(
    val type: EventType = EventType.PAUSE,
    val positionMs: Long = 0L,
    val senderName: String = ""
)

@Serializable
enum class EventType {
    PLAY, PAUSE, SEEK
}
