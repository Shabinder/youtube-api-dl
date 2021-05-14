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

@file:Suppress("unused","UNCHECKED_CAST")

package io.github.shabinder.models

import io.github.shabinder.models.formats.AudioFormat
import io.github.shabinder.models.formats.AudioVideoFormat
import io.github.shabinder.models.formats.Format
import io.github.shabinder.models.formats.VideoFormat
import io.github.shabinder.models.quality.AudioQuality
import io.github.shabinder.models.quality.VideoQuality
import io.github.shabinder.models.subtitles.SubtitlesInfo

data class YoutubeVideo(
    val videoDetails: VideoDetails,
    val formats: List<Format>,
    val subtitlesInfo: List<SubtitlesInfo>,
    val clientVersion: String
) {
    fun getFormatByItag(itag: Int): Format? = formats.firstOrNull { it.itag.id == itag }

    fun getVideoWithAudioFormats(): List<AudioVideoFormat> = formats.filterIsInstance<AudioVideoFormat>()

    fun getVideoFormats(): List<VideoFormat> = formats.filterIsInstance<VideoFormat>()

    fun getVideoWithQuality(videoQuality: VideoQuality): List<VideoFormat> =
        formats.filter{
            it is VideoFormat && it.videoQuality === videoQuality
        } as List<VideoFormat>

    fun getVideoWithExtension(extension: Extension): List<VideoFormat> =
        formats.filter {
            it is VideoFormat && it.extension == extension
        } as List<VideoFormat>

    fun getAudioFormats(): List<AudioFormat> = formats.filterIsInstance<AudioFormat>()

    fun getAudioWithQuality(audioQuality: AudioQuality): List<AudioFormat> =
        formats.filter { it is AudioFormat && it.audioQuality === audioQuality } as List<AudioFormat>

    fun getAudioWithExtension(extension: Extension): List<AudioFormat> =
        formats.filter { it is AudioFormat && it.extension === extension } as List<AudioFormat>


    companion object {
        private const val BUFFER_SIZE = 4096
        private const val PART_LENGTH = 2 * 1024 * 1024
    }
}