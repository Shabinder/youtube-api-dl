package com.shabinder.downloader.models.playlist

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.models.AbstractVideoDetails
import com.shabinder.downloader.utils.getBoolean
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

class PlaylistVideoDetails(json: JsonObject) : AbstractVideoDetails(json) {
    var index = json["index"]?.jsonObject?.getInteger("simpleText") ?: -1
        private set

    var isPlayable = false
        private set

    init {
        author = json["shortBylineText"]?.jsonObject?.get("runs")?.jsonArray?.get(0)?.jsonObject?.getString("text")
        title = json["title"]?.jsonObject?.let {
            it.getString("simpleText") ?: it["runs"]?.jsonArray?.get(0)?.jsonObject?.getString("text")
        }
        if (thumbnails.isNotEmpty()) {
            // Otherwise, contains "/hqdefault.jpg?"
            isLive = thumbnails[0].contains("/hqdefault_live.jpg?")
        }
        isPlayable = json.getBoolean("isPlayable") ?: false
    }

    @Throws(YoutubeException.DownloadUnavailableException::class)
    override fun checkDownload() {
        if (!isPlayable) {
            throw YoutubeException.DownloadUnavailableException.RestrictedVideoException("Can not download $title")
        } else if (isLive || lengthSeconds == 0) {
            throw YoutubeException.DownloadUnavailableException.LiveVideoException("Can not download live stream")
        }
    }
}
