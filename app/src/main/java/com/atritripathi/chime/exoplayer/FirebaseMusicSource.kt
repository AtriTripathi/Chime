package com.atritripathi.chime.exoplayer

import com.atritripathi.chime.exoplayer.State.*

class FirebaseMusicSource {
    private val onReadyListeners = mutableListOf<(Boolean) -> Unit>()

    private var state: State = STATE_CREATED
        set(value) {
            if (value == STATE_INITIALIZED || value == STATE_ERROR) {
                synchronized(onReadyListeners) {
                    field = value
                    onReadyListeners.forEach { listener ->
                        listener(value == STATE_INITIALIZED)
                    }
                }
            } else {
                field = value   // State is either CREATED or INITIALIZING
            }
        }

    /**
     * Uses the state variable to schedule an action when the music
     * data source is correctly downloaded and finished loading.
     */
    fun whenReady(action: (Boolean) -> Unit): Boolean {
        // Music source isn't ready yet, hence add the action to onReadyListeners list.
        return if (state == STATE_CREATED || state == STATE_INITIALIZING) {
            onReadyListeners += action
            false
        } else {
            action(state == STATE_INITIALIZED) // Music source is ready, hence execute the action.
            true
        }
    }
}

/**
 * Represents all possible states
 * for the music data source.
 */
enum class State {
    STATE_CREATED,
    STATE_INITIALIZING,
    STATE_INITIALIZED,
    STATE_ERROR
}