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

package com.shabinder.downloader.models

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getJsonArray
import com.shabinder.downloader.utils.getJsonObject
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

abstract class AbstractVideoDetails(json: JsonObject) {
    var videoId: String? = json.getString("videoId")
    var lengthSeconds = json.getInteger("lengthSeconds")
    var thumbnails: MutableList<String>

    // Subclass specific extraction
    var title: String? = null
    var author: String? = null
    var isLive = false
        protected set

    @Throws(YoutubeException.DownloadUnavailableException::class)
    protected abstract fun checkDownload()

    init {
        val jsonThumbnails: JsonArray? = json.getJsonObject("thumbnail")?.getJsonArray("thumbnails")
        thumbnails = mutableListOf()
        for (i in 0 until (jsonThumbnails?.size ?: 0)) {
            val jsonObject = jsonThumbnails?.getJsonObject(i)
            jsonObject?.getString("url")?.let { thumbnails.add(it) }
        }
    }
}