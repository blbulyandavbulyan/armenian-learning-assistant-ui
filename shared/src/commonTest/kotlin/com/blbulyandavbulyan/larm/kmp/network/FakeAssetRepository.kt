package com.blbulyandavbulyan.larm.kmp.network

class FakeAssetRepository : AssetRepository {
    var shouldFail = false
    val requestedUrls = mutableListOf<String>()

    override suspend fun getAssetBytes(url: String): ByteArray {
        requestedUrls.add(url)
        if (shouldFail) throw Exception("Fake Network Error")
        return ByteArray(0)
    }
}
