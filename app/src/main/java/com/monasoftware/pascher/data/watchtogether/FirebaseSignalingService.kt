package com.monasoftware.pascher.data.watchtogether

import android.util.Log
import android.widget.Toast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.monasoftware.pascher.domain.model.PlaybackEvent
import com.monasoftware.pascher.domain.model.PlaybackState
import com.monasoftware.pascher.domain.model.WatchSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await

class FirebaseSignalingService(private val context: android.content.Context) : SignalingService {

    private val TAG = "FirebaseSignaling"
    private val database = FirebaseDatabase.getInstance().reference.child("sessions")

    init {
        Log.d(TAG, "Initialized FirebaseSignalingService with DB: ${database.toString()}")
    }

    private val _currentSession = MutableStateFlow<WatchSession?>(null)
    override val currentSession: Flow<WatchSession?> = _currentSession.asStateFlow()

    private val _playbackEvents = MutableSharedFlow<PlaybackEvent>()
    override val playbackEvents: Flow<PlaybackEvent> = _playbackEvents.asSharedFlow()

    private var sessionListener: ValueEventListener? = null
    private var eventsListener: ValueEventListener? = null
    private var currentSessionId: String? = null

    override suspend fun createSession(hostName: String, movieId: String, movieTitle: String, movieUrl: String): String {
        Log.d(TAG, "Creating session for movie: $movieTitle")
        val sessionId = (100000..999999).random().toString()
        val session = WatchSession(
            sessionId = sessionId,
            movieId = movieId,
            hostName = hostName,
            movieTitle = movieTitle,
            movieUrl = movieUrl,
            participants = listOf(hostName)
        )

        try {
            database.child(sessionId).setValue(session).await()
            Log.d(TAG, "Session $sessionId successfully created in Firebase")
            currentSessionId = sessionId
            _currentSession.value = session
            
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                Toast.makeText(context, "Watch Together session started: $sessionId", Toast.LENGTH_LONG).show()
            }
            
            Log.d(TAG, "Local _currentSession updated: ${session.sessionId}")
            observeSession(sessionId)
            return sessionId
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create session in Firebase", e)
            throw e
        }
    }

    override suspend fun joinSession(sessionId: String, participantName: String): Boolean {
        Log.d(TAG, "Attempting to join session: $sessionId")
        try {
            val snapshot = database.child(sessionId).get().await()
            if (!snapshot.exists()) {
                Log.w(TAG, "Join failed: Session $sessionId does not exist")
                return false
            }

            val session = snapshot.getValue(WatchSession::class.java) ?: return false
            val participants = session.participants.toMutableList()

            if (participants.size >= 5 && !participants.contains(participantName)) {
                Log.w(TAG, "Join failed: Session $sessionId is full")
                return false // Session full
            }

            if (!participants.contains(participantName)) {
                participants.add(participantName)
                database.child(sessionId).child("participants").setValue(participants).await()
            }

            Log.d(TAG, "Successfully joined session: $sessionId")
            currentSessionId = sessionId
            _currentSession.value = session.copy(participants = participants)
            observeSession(sessionId)
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error joining session $sessionId", e)
            return false
        }
    }

    override suspend fun leaveSession() {
        val sessionId = currentSessionId ?: return

        sessionListener?.let { l -> database.child(sessionId).removeEventListener(l) }
        eventsListener?.let { l -> database.child(sessionId).child("lastEvent").removeEventListener(l) }

        sessionListener = null
        eventsListener = null
        currentSessionId = null
        _currentSession.value = null
    }

    override suspend fun removeParticipant(sessionId: String, participantName: String) {
        val snapshot = database.child(sessionId).get().await()
        val session = snapshot.getValue(WatchSession::class.java) ?: return
        val participants = session.participants.toMutableList()
        if (participants.remove(participantName)) {
            database.child(sessionId).child("participants").setValue(participants).await()
        }
    }

    override suspend fun sendPlaybackEvent(event: PlaybackEvent) {
        val sessionId = currentSessionId ?: return
        database.child(sessionId).child("lastEvent").setValue(event).await()
    }

    override suspend fun updateSessionState(positionMs: Long, isPlaying: Boolean) {
        val sessionId = currentSessionId ?: return
        val updates = HashMap<String, Any>()
        updates["positionMs"] = positionMs
        updates["state"] = if (isPlaying) PlaybackState.PLAYING.name else PlaybackState.PAUSED.name
        database.child(sessionId).updateChildren(updates).await()
    }

    private fun observeSession(sessionId: String) {
        // Remove old listeners if switching sessions
        if (currentSessionId != sessionId) {
            sessionListener?.let { l -> currentSessionId?.let { id -> database.child(id).removeEventListener(l) } }
            eventsListener?.let { l -> currentSessionId?.let { id -> database.child(id).child("lastEvent").removeEventListener(l) } }
        }

        val sListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "onDataChange: Session data received for $sessionId. raw: ${snapshot.value}")
                try {
                    val session = snapshot.getValue(WatchSession::class.java)
                    if (session == null) {
                        Log.w(TAG, "onDataChange: Received NULL session for $sessionId")
                    } else {
                        Log.d(TAG, "onDataChange: Session updated - ID: ${session.sessionId}, participants: ${session.participants.size}")
                        _currentSession.value = session
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "onDataChange: Deserialization failed for $sessionId", e)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: Session listener failed", error.toException())
            }
        }
        sessionListener = sListener
        database.child(sessionId).addValueEventListener(sListener)

        val eListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val event = snapshot.getValue(PlaybackEvent::class.java)
                if (event != null) {
                    _playbackEvents.tryEmit(event)
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        }
        eventsListener = eListener
        database.child(sessionId).child("lastEvent").addValueEventListener(eListener)
    }
}