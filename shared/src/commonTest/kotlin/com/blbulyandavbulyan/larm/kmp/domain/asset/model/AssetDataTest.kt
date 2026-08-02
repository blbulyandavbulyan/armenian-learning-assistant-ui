package com.blbulyandavbulyan.larm.kmp.domain.asset.model

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.test.Test

class AssetDataTest {

    @Test
    fun equals_same_instance() {
        val asset = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")

        (asset == asset) shouldBe true
    }

    @Test
    fun equals_same_content() {
        val asset1 = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")
        val asset2 = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")

        (asset1 == asset2) shouldBe true
    }

    @Test
    fun equals_different_data() {
        val asset1 = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")
        val asset2 = AssetData(byteArrayOf(1, 2, 4), "audio/mpeg")

        (asset1 == asset2) shouldBe false
    }

    @Test
    fun equals_different_mimeType() {
        val asset1 = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")
        val asset2 = AssetData(byteArrayOf(1, 2, 3), "audio/wav")

        (asset1 == asset2) shouldBe false
    }

    @Test
    fun equals_null() {
        val asset = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")
        val other: Any? = null

        (asset == other) shouldBe false
    }

    @Test
    fun equals_different_type() {
        val asset = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")

        asset.equals("not an AssetData") shouldBe false
    }

    @Test
    fun hashCode_same_content() {
        val asset1 = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")
        val asset2 = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")

        asset1.hashCode() shouldBe asset2.hashCode()
    }

    @Test
    fun hashCode_different_data() {
        val asset1 = AssetData(byteArrayOf(1, 2, 3), "audio/mpeg")
        val asset2 = AssetData(byteArrayOf(4, 5, 6), "audio/mpeg")

        asset1.hashCode() shouldNotBe asset2.hashCode()
    }
}
