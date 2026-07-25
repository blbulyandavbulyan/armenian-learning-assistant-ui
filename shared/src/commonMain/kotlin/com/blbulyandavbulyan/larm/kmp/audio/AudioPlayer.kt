package com.blbulyandavbulyan.larm.kmp.audio

data class Audio(
    val data: ByteArray,
    val mimeType: String
)

expect class AudioPlayer() {
    /**
     * Plays the given audio.
     * @throws AudioPlayException if an error occurs during audio initialization or playback.
     */
    suspend fun play(audio: Audio)
}
