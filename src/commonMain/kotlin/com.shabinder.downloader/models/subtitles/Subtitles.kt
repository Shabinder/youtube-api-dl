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

package com.shabinder.downloader.models.subtitles

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.exceptions.YoutubeException.SubtitlesException
import com.shabinder.downloader.models.Extension
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.http.URLParserException
import io.ktor.http.Url
import io.ktor.http.contentLength
import io.ktor.utils.io.errors.IOException
import kotlin.coroutines.cancellation.CancellationException
import kotlin.jvm.JvmOverloads

class Subtitles @JvmOverloads internal constructor(
    private val url: String,
    private val fromCaptions: Boolean = false
) {

    private var format: Extension? = null
    private var translationLanguage: String? = null

    fun formatTo(extension: Extension?): Subtitles {
        format = extension
        return this
    }

    fun translateTo(language: String?): Subtitles {
        // currently translation is supported only for subtitles from captions
        if (fromCaptions) {
            translationLanguage = language
        }
        return this
    }

    val downloadUrl: String
        get() {
            var downloadUrl = url
            if (format != null && format?.isSubtitle == true) {
                downloadUrl += "&fmt=" + format!!.value
            }
            if (translationLanguage != null && translationLanguage?.isNotEmpty() == true) {
                downloadUrl += "&tlang=$translationLanguage"
            }
            return downloadUrl
        }

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun download(): String {
        val url = try {
            Url(downloadUrl)
        } catch (e: URLParserException) {
            throw SubtitlesException("Failed to download subtitle: Invalid url: " + e.message)
        }
        val client = HttpClient {}
        val byteArray: ByteArray
        try {
            val result = client.get<HttpStatement>(url).execute()
            val responseCode: Int = result.status.value
            val contentLength = result.contentLength()?.toInt() ?: 0
            if (responseCode != 200) {
                throw SubtitlesException("Failed to download subtitle: HTTP $responseCode")
            }
            if (contentLength == 0) {
                throw SubtitlesException("Failed to download subtitle: Response is empty")
            }
            byteArray = ByteArray(contentLength)
            result.content.readFully(byteArray,0,contentLength)
        } catch (e: IOException) {
            throw SubtitlesException("Failed to download subtitle: " + e.message)
        } finally {
            client.close()
        }
        return byteArray.decodeToString()
    }
}
