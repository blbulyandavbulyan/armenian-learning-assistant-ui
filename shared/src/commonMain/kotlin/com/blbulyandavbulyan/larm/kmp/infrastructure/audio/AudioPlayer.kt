package com.blbulyandavbulyan.larm.kmp.infrastructure.audio

// doesn't automatically mean that it should be 'functional' interface
@Suppress("kotlin:S6517") // out of my face sonar, the fact that here is only one method,
interface AudioPlayer {
    /**
     * Plays the given audio.
     * @throws AudioPlayException if an error occurs during audio initialization or playback.
     */
    suspend fun play(audio: Audio)
}
