@file:Suppress("RegExpRedundantEscape")

package com.shabinder.downloader.extractor

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.exceptions.YoutubeException.BadPageException
import com.shabinder.downloader.models.Methods
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.utils.io.errors.IOException
import kotlin.coroutines.cancellation.CancellationException

class DefaultExtractor : Extractor {
    private val requestProperties: MutableMap<String, String> = HashMap()
    private var retryOnFailure = DEFAULT_RETRY_ON_FAILURE

    override fun setRequestProperty(key: String, value: String) {
        requestProperties[key] = value
    }

    override fun setRetryOnFailure(retryOnFailure: Int) {
        require(retryOnFailure >= 0) { "retry count should be > 0" }
        this.retryOnFailure = retryOnFailure
    }

    @Throws(YoutubeException::class)
    override fun extractYtPlayerConfig(html: String): String {
        for (pattern in YT_PLAYER_CONFIG_PATTERNS) {
            val match = pattern.find(html)
            if (!match?.value.isNullOrBlank()) {
                match?.groupValues?.get(1)?.let {
                    return it
                }
            }
        }
        throw BadPageException("Could not parse web page")
    }

    @Throws(YoutubeException::class)
    override fun extractYtInitialData(html: String): String {
        for (pattern in YT_INITIAL_DATA_PATTERNS) {
            val match = pattern.find(html)
            if (!match?.value.isNullOrBlank()) {
                match?.groupValues?.get(1)?.let{
                    return it
                }
            }
        }
        throw BadPageException("Could not parse web page")
    }

    @Throws(YoutubeException::class, CancellationException::class)
    override suspend fun loadUrl(url: String, rawData: String?, method: Methods): String {
        val client = HttpClient {  }
        var retryCount = retryOnFailure
        var errorMsg = ""
        val headers:HttpRequestBuilder.() -> Unit = {
            headers {
                for((key,value) in requestProperties){
                    header(key,value)
                }
            }
        }
        while (retryCount-- >= 0) {
            try {
                val response = when(method){
                    Methods.GET -> {
                        client.get<String>(url,headers)
                    }
                    Methods.POST -> {
                        client.post<String>(url){
                            headers()
                            if (rawData != null) {
                                body = rawData
                            }
                        }
                    }
                }
                client.close()
                return response
            } catch (e: IOException) {
                client.close()
                errorMsg = "Could not load url: $url, exception: ${e.message}"
            }
        }
        throw YoutubeException.VideoUnavailableException(errorMsg)
    }

    companion object {
        private val YT_PLAYER_CONFIG_PATTERNS = listOf(
            Regex(";ytplayer\\.config = (\\{.*?\\})\\;ytplayer"),
            Regex(";ytplayer\\.config = (\\{.*?\\})\\;"),
            Regex("ytInitialPlayerResponse\\s*=\\s*(\\{.+?\\})\\;var meta")
        )
        private val YT_INITIAL_DATA_PATTERNS = listOf(
            Regex("window\\[\"ytInitialData\"\\] = (\\{.*?\\});"),
            Regex("ytInitialData = (\\{.*?\\});")
        )
        private const val DEFAULT_USER_AGENT =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36"
        private const val DEFAULT_ACCEPT_LANG = "en-US,en;"
        private const val DEFAULT_RETRY_ON_FAILURE = 3
    }

    init {
        setRequestProperty("User-Agent", DEFAULT_USER_AGENT)
        setRequestProperty("Accept-language", DEFAULT_ACCEPT_LANG)
    }
}