package com.blbulyandavbulyan.larm.kmp.infrastructure.audio

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual class PlatformAudioPlayer actual constructor() : AudioPlayer {
    @Suppress("TooGenericExceptionCaught")
    actual override suspend fun play(audio: Audio) {
        var url: String? = null
        var audioEl: HTMLAudioElement? = null
        try {
            val blob = Blob(arrayOf(audio.data), BlobPropertyBag(type = audio.mimeType))
            url = URL.createObjectURL(blob)
            audioEl = document.createElement("audio") as HTMLAudioElement
            audioEl.src = url
            document.body?.append(audioEl)
            audioEl.addEventListener("ended") {
                url.let { URL.revokeObjectURL(it) }
                audioEl.remove()
            }
            audioEl.addEventListener("error") {
                println("Audio playback error event")
                url.let { URL.revokeObjectURL(it) }
                audioEl.remove()
            }
            audioEl.play().await()
        } catch (e: Throwable) {
            println("Audio setup failed: ${e.message}")
            url?.let { URL.revokeObjectURL(it) }
            audioEl?.remove()
            throw AudioPlayException(e)
        }
    }
}
