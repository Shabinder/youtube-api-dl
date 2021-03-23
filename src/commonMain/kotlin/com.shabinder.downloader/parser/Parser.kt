package com.shabinder.downloader.parser

import com.shabinder.downloader.cipher.CipherFactory
import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.extractor.Extractor
import com.shabinder.downloader.models.VideoDetails
import com.shabinder.downloader.models.formats.Format
import com.shabinder.downloader.models.playlist.PlaylistDetails
import com.shabinder.downloader.models.playlist.PlaylistVideoDetails
import com.shabinder.downloader.models.subtitles.SubtitlesInfo
import kotlinx.serialization.json.JsonObject
import kotlin.coroutines.cancellation.CancellationException

interface Parser {
    val extractor: Extractor
    val cipherFactory: CipherFactory

    /* Video */
    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getPlayerConfig(htmlUrl: String): JsonObject
    fun getClientVersion(json: JsonObject): String
    fun getVideoDetails(json: JsonObject): VideoDetails?

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getJsUrl(json: JsonObject): String

    fun getSubtitlesInfoFromCaptions(json: JsonObject): List<SubtitlesInfo>

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getSubtitlesInfo(videoId: String): List<SubtitlesInfo>

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun parseFormats(json: JsonObject): List<Format>

    /* Playlist */
    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getInitialData(htmlUrl: String): JsonObject
    fun getPlaylistDetails(playlistId: String, initialData: JsonObject): PlaylistDetails

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getPlaylistVideos(initialData: JsonObject, videoCount: Int): List<PlaylistVideoDetails>

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getChannelUploadsPlaylistId(channelId: String): String
}
