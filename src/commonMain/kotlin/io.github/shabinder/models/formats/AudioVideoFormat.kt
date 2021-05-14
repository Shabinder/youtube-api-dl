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

package io.github.shabinder.models.formats

import io.github.shabinder.models.quality.AudioQuality
import io.github.shabinder.models.quality.VideoQuality
import io.github.shabinder.utils.getInteger
import io.github.shabinder.utils.getString
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
    override fun toString(): String {
        return videoQuality.name + " - " + "${this.averageBitrate} / $bitrate" +" - " + extension.value + " - " + url
    }
}