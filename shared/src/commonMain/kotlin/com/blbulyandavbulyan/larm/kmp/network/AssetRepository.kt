package com.blbulyandavbulyan.larm.kmp.network

fun interface AssetRepository {
    suspend fun getAssetBytes(url: String): ByteArray
}

class NetworkAssetRepository(private val apiClient: ApiClient) : AssetRepository {
    override suspend fun getAssetBytes(url: String): ByteArray {
        return try {
            apiClient.getAssetBytes(url)
        } catch (e: Exception) {
            throw AudioFetchException(e.message ?: "Unknown audio error", e)
        }
    }
}

class AudioFetchException(message: String, cause: Exception? = null) : Exception(message, cause)
