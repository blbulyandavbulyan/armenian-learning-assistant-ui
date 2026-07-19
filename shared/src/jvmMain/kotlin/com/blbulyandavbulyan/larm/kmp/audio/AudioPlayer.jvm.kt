package com.blbulyandavbulyan.larm.kmp.audio

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual class AudioPlayer actual constructor() {
    @Suppress("TooGenericExceptionCaught")
    actual suspend fun play(audioBytes: ByteArray) {
        var clip: Clip? = null
        try {
            // TODO "Possibly blocking call in non-blocking context could lead to thread starvation" - warning from idea, standard idea fix
            // `wrap withContext` produces sonar warning
            val audioInputStream = AudioSystem.getAudioInputStream(ByteArrayInputStream(audioBytes))
            clip = AudioSystem.getClip()
            clip.addLineListener { if (it.type == LineEvent.Type.STOP) clip?.close() }
            clip.open(audioInputStream)
            clip.start()
        } catch (e: Exception) {
            println("Audio setup/playback failed: ${e.message}")
            clip?.close()
            throw AudioPlayException(e.message ?: "Unknown audio error", e)
        }
    }
}
