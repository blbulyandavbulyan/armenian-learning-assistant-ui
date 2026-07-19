package com.blbulyandavbulyan.larm.kmp.audio

import io.kotest.assertions.throwables.shouldThrow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AudioPlayerTest {
    @Test
    fun `play with invalid bytes should throw AudioPlayException`() = runTest {
        val player = AudioPlayer()
        val invalidBytes = byteArrayOf(1, 2, 3, 4, 5)

        shouldThrow<AudioPlayException> {
            player.play(invalidBytes)
        }
    }
}
