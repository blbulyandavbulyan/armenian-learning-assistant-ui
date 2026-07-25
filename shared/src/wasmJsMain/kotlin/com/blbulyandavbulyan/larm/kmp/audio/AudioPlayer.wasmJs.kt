@file:OptIn(ExperimentalWasmJsInterop::class)

package com.blbulyandavbulyan.larm.kmp.audio

import kotlinx.browser.document
import kotlinx.coroutines.suspendCancellableCoroutine
import org.khronos.webgl.Uint8Array
import org.w3c.dom.HTMLAudioElement
import org.w3c.dom.url.URL
import org.w3c.files.Blob
import org.w3c.files.BlobPropertyBag
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@JsFun("(size) => new Uint8Array(size)")
private external fun createUint8Array(size: Int): Uint8Array

@JsFun("(array, index, value) => { array[index] = value; }")
private external fun setUint8Array(array: Uint8Array, index: Int, value: Byte)

@JsFun("(array) => [array]")
private external fun wrapInArray(array: Uint8Array): JsArray<JsAny?>

@JsFun("(audio, onSuccess, onError) => { audio.play().then(() => onSuccess(), (e) => onError(e.toString())); }")
private external fun jsPlayAudio(audio: HTMLAudioElement, onSuccess: () -> Unit, onError: (String) -> Unit)

actual class AudioPlayer actual constructor() {
    @Suppress("TooGenericExceptionCaught")
    actual suspend fun play(audio: Audio) {
        var url: String? = null
        var audioEl: HTMLAudioElement? = null
        try {
            val uint8Array = createUint8Array(audio.data.size)
            for (i in audio.data.indices) {
                setUint8Array(uint8Array, i, audio.data[i])
            }

            val jsArray = wrapInArray(uint8Array)
            val blobPropertyBag = BlobPropertyBag(type = audio.mimeType)
            val blob = Blob(jsArray, blobPropertyBag)

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
            suspendCancellableCoroutine<Unit> { cont ->
                jsPlayAudio(audioEl, {
                    cont.resume(Unit)
                }, {
                    cont.resumeWithException(AudioPlayException(message = it))
                })
            }
        } catch (e: Throwable) {
            println("Audio setup failed: ${e.message}")
            url?.let { URL.revokeObjectURL(it) }
            audioEl?.remove()
            throw AudioPlayException(e)
        }
    }
}
