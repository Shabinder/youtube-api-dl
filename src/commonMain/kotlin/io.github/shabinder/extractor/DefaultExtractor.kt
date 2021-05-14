/*
 *  Copyright (c)  2021  Shabinder Singh
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

@file:Suppress("RegExpRedundantEscape")

package io.github.shabinder.extractor

import io.github.shabinder.exceptions.YoutubeException
import io.github.shabinder.exceptions.YoutubeException.BadPageException
import io.github.shabinder.models.Methods
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.errors.*
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
                    if(key.equals("Content-Type",true)){
                        /*
                        * io.ktor.http.UnsafeHeaderException:
                        *  Header(s) [Content-Type] are controlled by the engine and cannot be set explicitly
                        * */
                        //contentType(ContentType.Application.Json)
                    }else{
                        header(key,value)
                    }
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