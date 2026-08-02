package com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.asset

import com.blbulyandavbulyan.larm.kmp.domain.asset.model.AssetData
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.AssetFetchException
import com.blbulyandavbulyan.larm.kmp.domain.asset.repository.AssetRepository
import com.blbulyandavbulyan.larm.kmp.infrastructure.appbackend.client.ApiClient

class BackendAssetRepository(private val apiClient: ApiClient) : AssetRepository {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun getAsset(url: String): AssetData {
        return try {
            apiClient.getAsset(url)
        } catch (e: Throwable) {
            throw AssetFetchException(e)
        }
    }
}
