package com.shabinder.downloader.models

import com.shabinder.downloader.exceptions.YoutubeException.DownloadUnavailableException.LiveVideoException
import com.shabinder.downloader.utils.getBoolean
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getJsonArray
import com.shabinder.downloader.utils.getLong
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
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
