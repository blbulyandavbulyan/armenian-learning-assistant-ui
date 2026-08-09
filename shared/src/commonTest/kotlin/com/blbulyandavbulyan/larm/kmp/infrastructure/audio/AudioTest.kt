package com.blbulyandavbulyan.larm.kmp.infrastructure.audio

import io.kotest.matchers.shouldBe
import kotlin.test.Test

@Suppress("SENSELESS_COMPARISON")
class AudioTest {

    @Test
    fun equals_sameInstance_returnsTrue() {
        val audio = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        (audio == audio) shouldBe true
    }

    @Test
    fun equals_sameContentAndMimeType_returnsTrue() {
        val audio1 = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        val audio2 = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        (audio1 == audio2) shouldBe true
    }

    @Test
    fun equals_null() {
        val audio = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        val other = null
        (audio == other) shouldBe false
    }

    @Test
    fun equals_differentClass() {
        val audio = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        audio.equals("not an audio") shouldBe false
    }

    @Test
    fun equals_differentData_returnsFalse() {
        val audio1 = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        val audio2 = Audio(byteArrayOf(4, 5, 6), "audio/mp3")
        (audio1 == audio2) shouldBe false
    }

    @Test
    fun equals_differentMimeType_returnsFalse() {
        val audio1 = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        val audio2 = Audio(byteArrayOf(1, 2, 3), "audio/wav")
        (audio1 == audio2) shouldBe false
    }

    @Test
    fun hashCode_equalObjects_returnsSameHashCode() {
        val audio1 = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        val audio2 = Audio(byteArrayOf(1, 2, 3), "audio/mp3")
        audio1.hashCode() shouldBe audio2.hashCode()
    }
}
