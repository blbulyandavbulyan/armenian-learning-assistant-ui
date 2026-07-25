package com.blbulyandavbulyan.larm.kmp.network

import com.blbulyandavbulyan.larm.kmp.audio.AudioPlayException

class FakeAssetRepository : AssetRepository {
    var shouldFailWithAssetFetchException = false
    var shouldFailWithAudioException = false
    val requestedUrls = mutableListOf<String>()

    override suspend fun getAsset(url: String): AssetData {
        requestedUrls.add(url)
        if (shouldFailWithAudioException) throw AudioPlayException(message = "Fake Audio Error")
        if (shouldFailWithAssetFetchException) throw AssetFetchException(message = "Fake Network Error")
        return AssetData(ByteArray(0), "audio/wav")
    }
}
