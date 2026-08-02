package com.blbulyandavbulyan.larm.kmp.infrastructure.audio

class FakeAudioPlayer : AudioPlayer {
    var shouldFail = false
    override suspend fun play(audio: Audio) {
        if (shouldFail) {
            throw AudioPlayException(message = "Fake Audio Error")
        }
    }
}
