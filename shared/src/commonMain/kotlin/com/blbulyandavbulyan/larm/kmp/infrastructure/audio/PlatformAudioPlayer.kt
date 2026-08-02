package com.blbulyandavbulyan.larm.kmp.infrastructure.audio

expect class PlatformAudioPlayer() : AudioPlayer {
    override suspend fun play(audio: Audio)
}
