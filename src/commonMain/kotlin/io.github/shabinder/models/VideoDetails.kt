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

package io.github.shabinder.models

import io.github.shabinder.exceptions.YoutubeException.DownloadUnavailableException.LiveVideoException
import io.github.shabinder.utils.getBoolean
import io.github.shabinder.utils.getInteger
import io.github.shabinder.utils.getJsonArray
import io.github.shabinder.utils.getLong
import io.github.shabinder.utils.getString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

data class VideoDetails(
    val jsonObject: JsonObject,
    var keywords: List<String> = emptyList(),
    var shortDescription: String? = null,
    var viewCount: Long? = null,
    var averageRating: Int? = null,
    var liveUrl: String? = null,
    var isLiveContent: Boolean? = null,
) : AbstractVideoDetails(jsonObject) {

    constructor(json: JsonObject, liveHLSUrl: String?):this(json){
        title = json.getString("title")
        author = json.getString("author")
        isLive = json.getBoolean("isLive") ?: false
        keywords = json.getJsonArray("keywords")?.map { it.jsonPrimitive.content } ?: listOf()
        shortDescription = json.getString("shortDescription")
        viewCount = json.getLong("viewCount")
        averageRating = json.getInteger("averageRating")
        liveUrl = liveHLSUrl
        isLiveContent = json.getBoolean("isLiveContent") ?: false
    }

    override fun checkDownload() {
        if (isLive || isLiveContent == true && lengthSeconds == 0) throw LiveVideoException(
            "Can not download live stream"
        )
    }
}
