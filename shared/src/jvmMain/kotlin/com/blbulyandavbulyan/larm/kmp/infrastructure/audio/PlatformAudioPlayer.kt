package com.blbulyandavbulyan.larm.kmp.infrastructure.audio

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual class PlatformAudioPlayer actual constructor() : AudioPlayer {
    @Suppress("TooGenericExceptionCaught", "kotlin:S6310")
    actual override suspend fun play(audio: Audio) {
        var clip: Clip? = null
        try {
            withContext(Dispatchers.IO) {
                val audioInputStream = AudioSystem.getAudioInputStream(ByteArrayInputStream(audio.data))
                clip = AudioSystem.getClip()
                clip.addLineListener { if (it.type == LineEvent.Type.STOP) clip?.close() }
                clip.open(audioInputStream)
                clip.start()
            }
        } catch (e: Exception) {
            println("Audio setup/playback failed: ${e.message}")
            clip?.close()
            throw AudioPlayException(e)
        }
    }
}
