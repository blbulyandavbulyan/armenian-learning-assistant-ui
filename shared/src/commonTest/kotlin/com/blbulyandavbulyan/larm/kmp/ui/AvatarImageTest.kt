package com.blbulyandavbulyan.larm.kmp.ui

import com.blbulyandavbulyan.larm.kmp.ui.common.extractInitials
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class AvatarImageTest {

    @Test
    fun `extractInitials handles multiple words`() {
        extractInitials("David Bul") shouldBe "DB"
        extractInitials("John Robert Doe") shouldBe "JR"
    }

    @Test
    fun `extractInitials handles single word`() {
        extractInitials("David") shouldBe "D"
        extractInitials("alice") shouldBe "A"
    }

    @Test
    fun `extractInitials handles null and empty`() {
        extractInitials(null) shouldBe "U"
        extractInitials("") shouldBe "U"
        extractInitials("   ") shouldBe "U"
    }
}
