package com.blbulyandavbulyan.larm.kmp.audio

expect class AudioPlayer() {
    /**
     * Plays the given audio bytes.
     * @throws AudioPlayException if an error occurs during audio initialization or playback.
     */
    suspend fun play(audioBytes: ByteArray)
}
