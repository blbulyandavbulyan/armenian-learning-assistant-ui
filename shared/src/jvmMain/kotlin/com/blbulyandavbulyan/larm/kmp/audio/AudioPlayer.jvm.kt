package com.blbulyandavbulyan.larm.kmp.audio

import java.io.ByteArrayInputStream
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip
import javax.sound.sampled.LineEvent

actual class AudioPlayer actual constructor() {
    actual fun play(audioBytes: ByteArray) {
        var clip: Clip? = null
        try {
            val audioInputStream = AudioSystem.getAudioInputStream(ByteArrayInputStream(audioBytes))
            clip = AudioSystem.getClip()
            clip.addLineListener { if (it.type == LineEvent.Type.STOP) clip?.close() }
            clip.open(audioInputStream)
            clip.start()
        } catch (e: Throwable) {
            e.printStackTrace()
            clip?.close()
        }
    }
}
