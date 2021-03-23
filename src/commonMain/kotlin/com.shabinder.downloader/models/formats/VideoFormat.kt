package com.shabinder.downloader.models.formats

import com.shabinder.downloader.models.quality.VideoQuality
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject

class VideoFormat(json: JsonObject, isAdaptive: Boolean) : Format(json, isAdaptive) {
    val fps: Int = json.getInteger("fps")
    val qualityLabel: String? = json.getString("qualityLabel")
    var width: Int = json.getInteger("width")
    var height: Int = json.getInteger("height")
    val videoQuality = json.getString("quality")?.let { VideoQuality.valueOf(it) }
        ?: itag.videoQuality()
        ?: VideoQuality.unknown

    override fun type(): String {
        return VIDEO
    }

    override fun toString(): String {
        return videoQuality.name + " - " + "${this.fps}FPS / $bitrate" +" - " + extension?.value + " - " + url
    }

    init {
        if (json.containsKey("size")) {
            val split = json.getString("size")?.split("x")
            width = split?.get(0)?.toInt() ?: 0
            height = split?.get(1)?.toInt() ?: 0
        }
    }
}