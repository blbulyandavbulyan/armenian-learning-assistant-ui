package com.blbulyandavbulyan.larm.kmp.network

fun interface AssetRepository {
    /**
     * Fetches asset
     * @return asset content
     * @throws AssetFetchException if an error occurs during audio initialization or playback.
     */
    suspend fun getAssetBytes(url: String): ByteArray
}

class NetworkAssetRepository(private val apiClient: ApiClient) : AssetRepository {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun getAssetBytes(url: String): ByteArray {
        return try {
            apiClient.getAssetBytes(url)
        } catch (e: Throwable) {
            throw AssetFetchException(e.message ?: "Unknown asset error", Exception(e))
        }
    }
}

class AssetFetchException(message: String, cause: Exception? = null) : Exception(message, cause)
