package com.shabinder.downloader.models.formats

import com.shabinder.downloader.models.quality.AudioQuality
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject

class AudioFormat(json: JsonObject, isAdaptive: Boolean) : Format(json, isAdaptive) {
    val averageBitrate: Int = json.getInteger("averageBitrate")
    val audioSampleRate: Int = json.getInteger("audioSampleRate")

    val audioQuality: AudioQuality =
        try {
            val split = json.getString("audioQuality")?.split("_")
            split?.get(split.size - 1)?.toLowerCase()?.let { AudioQuality.valueOf(it) }
        }catch(e: Exception) { itag.audioQuality() }
            ?: AudioQuality.unknown

    override fun type(): String {
        return AUDIO
    }
    override fun toString(): String {
        return audioQuality.name + " - " + "$averageBitrate / $bitrate" +" - " + extension?.value + " - " + url
    }
}
