package com.blbulyandavbulyan.larm.kmp.network

fun interface AssetRepository {
    /**
     * Fetches asset
     * @return asset content
     * @throws AssetFetchException if an error occurs during audio initialization or playback.
     */
    suspend fun getAsset(url: String): AssetData
}

class NetworkAssetRepository(private val apiClient: ApiClient) : AssetRepository {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun getAsset(url: String): AssetData {
        return try {
            apiClient.getAsset(url)
        } catch (e: Throwable) {
            throw AssetFetchException(e)
        }
    }
}

open class AssetFetchException(cause: Throwable? = null, message: String? = cause?.message) : Exception(message, cause)
class AssetHasNoContentTypeException : Exception("Asset response is missing Content-Type header")
