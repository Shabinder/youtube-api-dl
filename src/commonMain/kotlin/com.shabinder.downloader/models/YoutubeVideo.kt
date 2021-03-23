package com.shabinder.downloader.models

import com.shabinder.downloader.models.formats.AudioFormat
import com.shabinder.downloader.models.formats.AudioVideoFormat
import com.shabinder.downloader.models.formats.Format
import com.shabinder.downloader.models.formats.VideoFormat
import com.shabinder.downloader.models.quality.AudioQuality
import com.shabinder.downloader.models.quality.VideoQuality

@Suppress("UNCHECKED_CAST")
data class YoutubeVideo(
    val videoDetails: VideoDetails,
    val formats: List<Format>,
    //val subtitlesInfo: List<SubtitlesInfo>,
    val clientVersion: String
) {
    fun findFormatByItag(itag: Int): Format? = formats.firstOrNull { it.itag.id == itag }

    fun videoWithAudioFormats(): List<AudioVideoFormat> = formats.filterIsInstance<AudioVideoFormat>()

    fun videoFormats(): List<VideoFormat> = formats.filterIsInstance<VideoFormat>()

    fun findVideoWithQuality(videoQuality: VideoQuality): List<VideoFormat> =
        formats.filter{
            it is VideoFormat && it.videoQuality === videoQuality
        } as List<VideoFormat>

    fun findVideoWithExtension(extension: Extension): List<VideoFormat> =
        formats.filter {
            it is VideoFormat && (it.extension?.equals(extension) == true)
        } as List<VideoFormat>

    fun audioFormats(): List<AudioFormat> = formats.filterIsInstance<AudioFormat>()

    fun findAudioWithQuality(audioQuality: AudioQuality): List<AudioFormat> =
        formats.filter { it is AudioFormat && it.audioQuality === audioQuality } as List<AudioFormat>

    fun findAudioWithExtension(extension: Extension): List<AudioFormat> =
        formats.filter { it is AudioFormat && it.extension === extension } as List<AudioFormat>


    companion object {
        private const val BUFFER_SIZE = 4096
        private const val PART_LENGTH = 2 * 1024 * 1024
    }
}