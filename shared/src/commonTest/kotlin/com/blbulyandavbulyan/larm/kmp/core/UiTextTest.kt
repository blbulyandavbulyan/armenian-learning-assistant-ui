package com.blbulyandavbulyan.larm.kmp.core

import armenianlearningassistant_kmp.shared.generated.resources.Res
import armenianlearningassistant_kmp.shared.generated.resources.error_unknown
import io.kotest.matchers.shouldBe
import kotlin.test.Test

class UiTextTest {

    @Test
    fun `from with string creates DynamicString`() {
        val result = UiText.from("Custom Message")
        result shouldBe UiText.DynamicString("Custom Message")
    }

    @Test
    fun `from with StringResource creates Resource`() {
        val result = UiText.from(Res.string.error_unknown)
        result shouldBe UiText.Resource(Res.string.error_unknown)
    }

    @Test
    fun `from with null string falls back to fallback resource`() {
        val result = UiText.from(null, Res.string.error_unknown)
        result shouldBe UiText.Resource(Res.string.error_unknown)
    }

    @Test
    fun `from with non-null string ignores fallback resource`() {
        val result = UiText.from("Actual Error", Res.string.error_unknown)
        result shouldBe UiText.DynamicString("Actual Error")
    }
}
