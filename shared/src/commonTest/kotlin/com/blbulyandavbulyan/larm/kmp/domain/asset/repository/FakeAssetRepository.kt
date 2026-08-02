package com.blbulyandavbulyan.larm.kmp.domain.asset.repository

import com.blbulyandavbulyan.larm.kmp.domain.asset.model.AssetData

class FakeAssetRepository : AssetRepository {
    var shouldFail = false
    val requestedUrls = mutableListOf<String>()

    override suspend fun getAsset(url: String): AssetData {
        requestedUrls.add(url)
        if (shouldFail) throw AssetFetchException(message = "Fake Network Error")
        return AssetData(ByteArray(0), "audio/wav")
    }
}
