package com.blbulyandavbulyan.larm.kmp.audio

import kotlinx.browser.document
import kotlinx.coroutines.await
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual class AudioPlayer actual constructor() {
    @Suppress("TooGenericExceptionCaught")
    actual suspend fun play(audioBytes: ByteArray) {
        var url: String? = null
        var audio: HTMLAudioElement? = null
        try {
            val blob = Blob(arrayOf(audioBytes), BlobPropertyBag(type = "audio/wav"))
            url = URL.createObjectURL(blob)
            audio = document.createElement("audio") as HTMLAudioElement
            audio.src = url
            document.body?.append(audio)
            audio.addEventListener("ended") {
                url.let { URL.revokeObjectURL(it) }
                audio.remove()
            }
            audio.addEventListener("error") {
                println("Audio playback error event")
                url.let { URL.revokeObjectURL(it) }
                audio.remove()
            }
            audio.play().await()
        } catch (e: Throwable) {
            println("Audio setup failed: ${e.message}")
            url?.let { URL.revokeObjectURL(it) }
            audio?.remove()
            throw AudioPlayException(e)
        }
    }
}
