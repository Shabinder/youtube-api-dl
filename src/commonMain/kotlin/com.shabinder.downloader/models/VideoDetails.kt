package com.shabinder.downloader.models

import com.shabinder.downloader.exceptions.YoutubeException.DownloadUnavailableException.LiveVideoException
import com.shabinder.downloader.utils.getBoolean
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getLong
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonPrimitive

class VideoDetails(json: JsonObject, liveHLSUrl: String?) : AbstractVideoDetails(json) {
    override fun toString(): String {
        return "${this.title}---${this.lengthSeconds}----${this.author}---${this.shortDescription}"
    }

    var keywords: List<String> = json["keywords"]?.jsonArray?.map { it.jsonPrimitive.content } ?: listOf()
    var shortDescription: String? = json.getString("shortDescription")
    var viewCount: Long = json.getLong("viewCount")
    var averageRating: Int = json.getInteger("averageRating")
    var liveUrl: String? = liveHLSUrl
    var isLiveContent = json.getBoolean("isLiveContent") ?: false
        private set

    init {
        title = json.getString("title")
        author = json.getString("author")
        isLive = json.getBoolean("isLive") ?: false
    }

    override fun checkDownload() {
        if (isLive || isLiveContent && lengthSeconds == 0) throw LiveVideoException(
            "Can not download live stream"
        )
    }
}
