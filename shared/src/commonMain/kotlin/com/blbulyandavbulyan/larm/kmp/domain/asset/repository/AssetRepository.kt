package com.blbulyandavbulyan.larm.kmp.domain.asset.repository

import com.blbulyandavbulyan.larm.kmp.domain.asset.model.AssetData

fun interface AssetRepository {
    /**
     * Fetches asset
     * @return asset content
     * @throws AssetFetchException if an error occurs during audio initialization or playback.
     */
    suspend fun getAsset(url: String): AssetData
}

open class AssetFetchException(cause: Throwable? = null, message: String? = cause?.message) : Exception(message, cause)
