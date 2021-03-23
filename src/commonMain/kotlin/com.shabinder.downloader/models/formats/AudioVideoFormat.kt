package com.shabinder.downloader.models.formats

import com.shabinder.downloader.models.quality.AudioQuality
import com.shabinder.downloader.models.quality.VideoQuality
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject

class AudioVideoFormat(json: JsonObject, isAdaptive: Boolean) : Format(json, isAdaptive) {
    val averageBitrate: Int = json.getInteger("averageBitrate")
    val audioSampleRate: Int = json.getInteger("audioSampleRate")
    val qualityLabel: String? = json.getString("qualityLabel")
    val width: Int = json.getInteger("width")
    val height: Int = json.getInteger("height")

    val audioQuality: AudioQuality =
        try {
            val split = json.getString("audioQuality")?.split("_")
            val quality = split?.get(split.size - 1)?.toLowerCase()
            quality?.let { AudioQuality.valueOf(it) } ?: itag.audioQuality()
        } catch (ignore: Exception) { null }
            ?: AudioQuality.unknown

    val videoQuality: VideoQuality =
        try {
            json.getString("quality")?.let { VideoQuality.valueOf(it) } ?: itag.videoQuality()
        } catch (ignore: Exception) { null }
            ?: VideoQuality.unknown

    override fun type(): String {
        return AUDIO_VIDEO
    }
}