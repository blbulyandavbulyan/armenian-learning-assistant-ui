package com.blbulyandavbulyan.larm.kmp.network

fun interface AssetRepository {
    suspend fun getAssetBytes(url: String): ByteArray
}

class NetworkAssetRepository(private val apiClient: ApiClient) : AssetRepository {
    override suspend fun getAssetBytes(url: String): ByteArray {
        return apiClient.getAssetBytes(url)
    }
}
