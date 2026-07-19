@file:OptIn(ExperimentalWasmJsInterop::class)

package com.blbulyandavbulyan.larm.kmp.audio

import kotlinx.browser.document
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag

import kotlinx.coroutines.await

@JsFun("(size) => new Uint8Array(size)")
private external fun createUint8Array(size: Int): Uint8Array

@JsFun("(array, index, value) => { array[index] = value; }")
private external fun setUint8Array(array: Uint8Array, index: Int, value: Byte)

@JsFun("(array) => [array]")
private external fun wrapInArray(array: Uint8Array): JsArray<JsAny?>

actual class AudioPlayer actual constructor() {
    actual suspend fun play(audioBytes: ByteArray) {
        var url: String? = null
        try {
            val uint8Array = createUint8Array(audioBytes.size)
            for (i in audioBytes.indices) {
                setUint8Array(uint8Array, i, audioBytes[i])
            }

            val jsArray = wrapInArray(uint8Array)
            val blobPropertyBag = BlobPropertyBag(type = "audio/wav")
            val blob = Blob(jsArray, blobPropertyBag)

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
