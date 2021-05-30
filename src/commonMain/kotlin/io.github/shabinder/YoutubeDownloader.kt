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

package io.github.shabinder

import io.github.shabinder.cipher.CipherFunction
import io.github.shabinder.exceptions.YoutubeException
import io.github.shabinder.models.VideoDetails
import io.github.shabinder.models.YoutubeVideo
import io.github.shabinder.models.formats.Format
import io.github.shabinder.models.playlist.PlaylistDetails
import io.github.shabinder.models.playlist.PlaylistVideoDetails
import io.github.shabinder.models.playlist.YoutubePlaylist
import io.github.shabinder.models.subtitles.SubtitlesInfo
import io.github.shabinder.parser.DefaultParser
import io.github.shabinder.parser.Parser
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlin.coroutines.cancellation.CancellationException

class YoutubeDownloader(
    private val parser: Parser = DefaultParser(),
    enableCORSProxy:Boolean = true,
    CORSProxyAddress: String = "https://kind-grasshopper-73.telebit.io/cors/"
) {
    var isCORSEnabled: Boolean = enableCORSProxy
    var addressCORS: String = CORSProxyAddress

    private val proxyCORS get() = if(activePlatform is TargetPlatforms.Js && isCORSEnabled) addressCORS else ""

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getVideo(videoId: String): YoutubeVideo {
        val htmlUrl = "${proxyCORS}https://www.youtube.com/watch?v=$videoId"
        val ytPlayerConfig: MutableMap<String,JsonElement> = parser.getPlayerConfig(htmlUrl).toMutableMap()
        ytPlayerConfig["yt-downloader-videoId"] = JsonPrimitive(videoId)
        val ytConfigJson = JsonObject(ytPlayerConfig)
        val videoDetails: VideoDetails? = parser.getVideoDetails(ytConfigJson)
        val formats: List<Format> = parser.parseFormats(ytConfigJson)
        val subtitlesInfo: List<SubtitlesInfo> = parser.getSubtitlesInfoFromCaptions(ytConfigJson)
        val clientVersion: String = parser.getClientVersion(ytConfigJson)
        return videoDetails?.let { YoutubeVideo(it, formats, subtitlesInfo, clientVersion) }
            ?: throw YoutubeException.VideoUnavailableException("Video Details Couldn't Be Fetched")
    }

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getPlaylist(playlistId: String): YoutubePlaylist {
        val htmlUrl = "${proxyCORS}https://www.youtube.com/playlist?list=$playlistId"
        val ytInitialData: JsonObject = parser.getInitialData(htmlUrl)
        if (!ytInitialData.containsKey("metadata")) {
            throw YoutubeException.BadPageException("Invalid initial data json")
        }
        val playlistDetails: PlaylistDetails = parser.getPlaylistDetails(playlistId, ytInitialData)
        val videos: List<PlaylistVideoDetails> = parser.getPlaylistVideos(ytInitialData, playlistDetails.videoCount)
        return YoutubePlaylist(playlistDetails, videos)
    }

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getChannelUploads(channelId: String): YoutubePlaylist {
        val playlistId: String = parser.getChannelUploadsPlaylistId(channelId)
        return getPlaylist(playlistId)
    }

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun getVideoSubtitles(videoId: String): List<SubtitlesInfo> {
        return parser.getSubtitlesInfo(videoId)
    }

    fun setParserRequestProperty(key: String, value: String) {
        parser.extractor.setRequestProperty(key, value)
    }

    fun setParserRetryOnFailure(retryOnFailure: Int) {
        parser.extractor.setRetryOnFailure(retryOnFailure)
    }

    fun addCipherFunctionPattern(priority: Int, regex: String) {
        parser.cipherFactory.addInitialFunctionPattern(priority, regex)
    }

    fun addCipherFunctionEquivalent(regex: String, function: CipherFunction) {
        parser.cipherFactory.addFunctionEquivalent(regex, function)
    }
}
