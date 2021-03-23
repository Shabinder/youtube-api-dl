package com.shabinder.downloader.models

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.utils.getInteger
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
    protected var title: String? = null
    protected var author: String? = null
    var isLive = false
        protected set

    @Throws(YoutubeException.DownloadUnavailableException::class)
    protected abstract fun checkDownload()

    init {
        val jsonThumbnails: JsonArray? = json["thumbnail"]?.jsonObject?.get("thumbnails")?.jsonArray
        thumbnails = mutableListOf()
        for (i in 0 until (jsonThumbnails?.size ?: 0)) {
            val jsonObject = jsonThumbnails?.get(i)?.jsonObject
            jsonObject?.getString("url")?.let { thumbnails.add(it) }
        }
    }
}