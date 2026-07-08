package com.blbulyandavbulyan.larm.kmp.audio

import kotlinx.browser.document
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

actual class AudioPlayer actual constructor() {
    actual fun play(audioBytes: ByteArray) {
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
            audio.play().catch { e ->
                println("Audio play promise rejected: $e")
                url.let { URL.revokeObjectURL(it) }
            }
        } catch (e: Throwable) {
            e.printStackTrace()
            url?.let { URL.revokeObjectURL(it) }
        }
    }
}
