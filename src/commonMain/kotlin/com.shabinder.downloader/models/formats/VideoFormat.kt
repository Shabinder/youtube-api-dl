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
        return videoQuality.name + " - " + "${this.fps}FPS / $bitrate" +" - " + extension.value + " - " + url
    }

    init {
        if (json.containsKey("size")) {
            val split = json.getString("size")?.split("x")
            width = split?.get(0)?.toInt() ?: 0
            height = split?.get(1)?.toInt() ?: 0
        }
    }
}