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
        return audioQuality.name + " - " + "$averageBitrate / $bitrate" +" - " + extension.value + " - " + url
    }
}
