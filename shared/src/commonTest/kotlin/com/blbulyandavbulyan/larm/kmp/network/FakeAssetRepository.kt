package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.audio.AudioPlayException

class FakeAssetRepository : AssetRepository {
    var shouldFail = false
    var shouldFailWithAudioException = false
    val requestedUrls = mutableListOf<String>()

    override suspend fun getAssetBytes(url: String): ByteArray {
        requestedUrls.add(url)
        if (shouldFailWithAudioException) throw AudioPlayException("Fake Audio Error")
        if (shouldFail) throw AssetFetchException("Fake Network Error")
        return ByteArray(0)
    }
}
