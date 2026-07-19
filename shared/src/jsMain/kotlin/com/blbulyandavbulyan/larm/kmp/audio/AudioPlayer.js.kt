package com.blbulyandavbulyan.larm.kmp.audio

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual class AudioPlayer actual constructor() {
    actual suspend fun play(audioBytes: ByteArray) {
        var url: String? = null
        try {
            val blob = Blob(arrayOf(audioBytes), BlobPropertyBag(type = "audio/wav"))
            url = URL.createObjectURL(blob)
            val audio = document.createElement("audio") as HTMLAudioElement
            audio.src = url
            audio.addEventListener("ended") {
                url.let { URL.revokeObjectURL(it) }
            }
            audio.addEventListener("error") {
                println("Audio playback error event")
                url.let { URL.revokeObjectURL(it) }
            }
            audio.play().await()
        } catch (e: Throwable) {
            println("Audio setup failed: ${e.message}")
            url?.let { URL.revokeObjectURL(it) }
            throw AudioPlayException(e.message ?: "Unknown audio error", e)
        }
    }
}
