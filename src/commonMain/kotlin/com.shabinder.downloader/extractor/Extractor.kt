package com.shabinder.downloader.extractor

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.models.Methods
import kotlin.coroutines.cancellation.CancellationException

interface Extractor {
    fun setRequestProperty(key: String, value: String)
    fun setRetryOnFailure(retryOnFailure: Int)

    @Throws(YoutubeException::class)
    fun extractYtPlayerConfig(html: String): String

    @Throws(YoutubeException::class)
    fun extractYtInitialData(html: String): String

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun loadUrl(url: String, rawData: String? = null, method: Methods = Methods.GET): String
}