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

package com.shabinder.downloader.models.playlist

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.models.AbstractVideoDetails
import com.shabinder.downloader.utils.getBoolean
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getJsonArray
import com.shabinder.downloader.utils.getJsonObject
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

data class PlaylistVideoDetails(
    val json: JsonObject,
    var index:Int? = null,
    var isPlayable:Boolean? = null
) : AbstractVideoDetails(json) {

    constructor(jsonObject: JsonObject):this(json = jsonObject){
        author = json
            .getJsonObject("shortBylineText")
            ?.getJsonArray("runs")
            ?.getJsonObject(0)
            ?.getString("text")

        title = json.getJsonObject("title")?.let {
            it.getString("simpleText") ?: it.getJsonArray("runs")?.getJsonObject(0)?.getString("text")
        }
        if (thumbnails.isNotEmpty()) {
            // Otherwise, contains "/hqdefault.jpg?"
            isLive = thumbnails[0].contains("/hqdefault_live.jpg?")
        }
        isPlayable = json.getBoolean("isPlayable") ?: false

        index = json.getJsonObject("index")?.getInteger("simpleText") ?: -1
    }

    @Throws(YoutubeException.DownloadUnavailableException::class)
    override fun checkDownload() {
        if (isPlayable == false) {
            throw YoutubeException.DownloadUnavailableException.RestrictedVideoException("Can not download $title")
        } else if (isLive || lengthSeconds == 0) {
            throw YoutubeException.DownloadUnavailableException.LiveVideoException("Can not download live stream")
        }
    }
}
