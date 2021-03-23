package com.shabinder.downloader.parser

import com.shabinder.downloader.cipher.CachedCipherFactory
import com.shabinder.downloader.cipher.Cipher
import com.shabinder.downloader.cipher.CipherFactory
import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.exceptions.YoutubeException.BadPageException
import com.shabinder.downloader.exceptions.YoutubeException.CipherException
import com.shabinder.downloader.exceptions.YoutubeException.UnknownFormatException
import com.shabinder.downloader.extractor.DefaultExtractor
import com.shabinder.downloader.extractor.Extractor
import com.shabinder.downloader.models.Itag
import com.shabinder.downloader.models.Methods
import com.shabinder.downloader.models.VideoDetails
import com.shabinder.downloader.models.formats.AudioFormat
import com.shabinder.downloader.models.formats.AudioVideoFormat
import com.shabinder.downloader.models.formats.Format
import com.shabinder.downloader.models.formats.VideoFormat
import com.shabinder.downloader.models.playlist.PlaylistDetails
import com.shabinder.downloader.models.playlist.PlaylistVideoDetails
import com.shabinder.downloader.models.subtitles.SubtitlesInfo
import com.shabinder.downloader.utils.downloadByteArray
import com.shabinder.downloader.utils.getBoolean
import com.shabinder.downloader.utils.getJsonArray
import com.shabinder.downloader.utils.getJsonObject
import com.shabinder.downloader.utils.getString
import io.ktor.http.URLParserException
import io.ktor.http.decodeURLPart
import io.ktor.utils.io.errors.IOException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlin.coroutines.cancellation.CancellationException

class DefaultParser(
    override val extractor: Extractor = DefaultExtractor(),
    override val cipherFactory: CipherFactory = CachedCipherFactory(extractor)
) : Parser {

    override suspend fun getPlayerConfig(htmlUrl: String): JsonObject {
        val html: String = extractor.loadUrl(htmlUrl)
        val ytPlayerConfig: String = extractor.extractYtPlayerConfig(html)
        return try {
            val config: JsonObject = Json.decodeFromString(ytPlayerConfig)
            if (config.containsKey("args")) {
                config
            } else {
                buildJsonObject {
                    put("args", buildJsonObject {
                        put("player_response", config)
                    })
                }
            }
        } catch (e: Exception) {
            throw BadPageException("Could not parse player config json")
        }
    }


    override fun getClientVersion(json: JsonObject): String {
        return getClientVersionFromContext(
            json.getJsonObject("args")
                .getJsonObject("player_response").getJsonObject("responseContext")
        )
    }

    override suspend fun getJsUrl(json: JsonObject): String {
        var js: String? = null
        if (json.containsKey("assets")) {
            js = json.getJsonObject("assets")!!.getString("js")
        } else {
            // if assets not found - download embed webpage and search there
            val videoId = json.getString("yt-downloader-videoId")
            val html: String = extractor.loadUrl("https://www.youtube.com/embed/$videoId")
            var matcher = assetsJsRegex.find(html)
            if (!matcher?.value.isNullOrBlank()) {
                js = matcher?.groupValues?.get(1)?.replace("\\", "")
            } else {
                matcher = embJsRegex.find(html)
                if (!matcher?.value.isNullOrBlank()) {
                    js = matcher?.groupValues?.get(1)?.replace("\\", "")
                }
            }
        }
        if (js == null) {
            throw BadPageException("Could not extract js url: assets not found")
        }
        return "https://youtube.com$js"
    }

    override fun getVideoDetails(json: JsonObject): VideoDetails? {
        val args = json.getJsonObject("args")!!
        val playerResponse = args.getJsonObject("player_response")
        if (playerResponse?.containsKey("videoDetails") == true) {
            val videoDetails = playerResponse.getJsonObject("videoDetails")
            var liveHLSUrl: String? = null
            if (videoDetails?.getBoolean("isLive") == true) {
                if (playerResponse.containsKey("streamingData")) {
                    liveHLSUrl = playerResponse.getJsonObject("streamingData")?.getString("hlsManifestUrl")
                }
            }
            return videoDetails?.let { VideoDetails(it, liveHLSUrl) }
        }
        return null
    }

    override fun getSubtitlesInfoFromCaptions(json: JsonObject): List<SubtitlesInfo> {
        val args = json.getJsonObject("args")
        val playerResponse = args?.getJsonObject("player_response")

        if (playerResponse?.containsKey("captions") == false) {
            return emptyList()
        }

        val captions = playerResponse.getJsonObject("captions")
        val playerCaptionsTrackListRenderer = captions.getJsonObject("playerCaptionsTracklistRenderer")
        if (playerCaptionsTrackListRenderer == null || playerCaptionsTrackListRenderer.isEmpty()) {
            return emptyList()
        }

        val captionsArray = playerCaptionsTrackListRenderer.getJsonArray("captionTracks")
        if (captionsArray == null || captionsArray.isEmpty()) {
            return emptyList()
        }

        val subtitlesInfo: MutableList<SubtitlesInfo> = mutableListOf()
        for (i in 0 until captionsArray.size) {
            val subtitleInfo = captionsArray.getJsonObject(i)
            val language = subtitleInfo?.getString("languageCode")
            val url = subtitleInfo?.getString("baseUrl")
            val vssId = subtitleInfo?.getString("vssId")
            if (language != null && url != null && vssId != null) {
                val isAutoGenerated = vssId.startsWith("a.")
                subtitlesInfo.add(SubtitlesInfo(url, language, isAutoGenerated, true))
            }
        }
        return subtitlesInfo
    }

    override suspend fun getSubtitlesInfo(videoId: String): List<SubtitlesInfo> {
        val xmlUrl = "https://video.google.com/timedtext?hl=en&type=list&v=$videoId"
        val subtitlesXml: String = extractor.loadUrl(xmlUrl)
        var matcher: MatchResult? = subtitleLangCodeRegex.find(subtitlesXml) ?: return emptyList()
        val subtitlesInfo: MutableList<SubtitlesInfo> = mutableListOf()
        while (matcher != null) {
            val language = matcher.groupValues[1]
            val url = "https://www.youtube.com/api/timedtext?lang=$language&v=$videoId"
            subtitlesInfo.add(SubtitlesInfo(url, language, false))
            matcher = matcher.next()
        }
        return subtitlesInfo
    }

    override suspend fun parseFormats(json: JsonObject): List<Format> {
        val args = json.getJsonObject("args")!!
        val playerResponse: JsonObject = args.getJsonObject("player_response")!!
        if (!playerResponse.containsKey("streamingData")) {
            throw BadPageException("Streaming data not found")
        }
        val streamingData = playerResponse.getJsonObject("streamingData")!!
        val jsonFormats = mutableListOf<JsonElement>()
        if (streamingData.containsKey("formats")) {
            streamingData.getJsonArray("formats")?.let { jsonFormats.addAll(it) }
        }
        val jsonAdaptiveFormats = mutableListOf<JsonElement>()
        if (streamingData.containsKey("adaptiveFormats")) {
            streamingData.getJsonArray("adaptiveFormats")?.let { jsonAdaptiveFormats.addAll(it) }
        }
        val jsUrl = getJsUrl(json)
        val formats: MutableList<Format> = mutableListOf()
        populateFormats(formats, JsonArray(jsonFormats), jsUrl, false)
        populateFormats(formats, JsonArray(jsonAdaptiveFormats), jsUrl, true)
        return formats
    }

    override suspend fun getInitialData(htmlUrl: String): JsonObject {
        val html: String = extractor.loadUrl(htmlUrl)
        val ytInitialData: String = extractor.extractYtInitialData(html)
        return try {
            Json.decodeFromString(ytInitialData)
        } catch (e: Exception) {
            e.printStackTrace()
            throw BadPageException("Could not parse initial data json")
        }
    }

    override fun getPlaylistDetails(playlistId: String, initialData: JsonObject): PlaylistDetails {
        try{
            val title: String = initialData.getJsonObject("metadata")
                .getJsonObject("playlistMetadataRenderer")
                ?.getString("title")!!
            val sideBarItems: JsonArray =
                initialData.getJsonObject("sidebar").getJsonObject("playlistSidebarRenderer").getJsonArray("items")!!
            var author: String? = null
            try {
                // try to retrieve author, some playlists may have no author
                author = sideBarItems.getJsonObject(1)
                    .getJsonObject("playlistSidebarSecondaryInfoRenderer")
                    .getJsonObject("videoOwner")
                    .getJsonObject("videoOwnerRenderer")
                    .getJsonObject("title")
                    .getJsonArray("runs")
                    .getJsonObject(0)
                    ?.getString("text")
            } catch (ignored: Exception) {
            }
            val stats: JsonArray? = sideBarItems.getJsonObject(0)
                .getJsonObject("playlistSidebarPrimaryInfoRenderer")
                .getJsonArray("stats")
            val videoCount = extractNumber(stats.getJsonObject(0).getJsonArray("runs").getJsonObject(0)?.getString("text")!!)
            val viewCount = extractNumber(stats.getJsonObject(1)?.getString("simpleText")!!)
            return PlaylistDetails(playlistId, title, author, videoCount, viewCount)
        }catch (e:NullPointerException){
            e.printStackTrace()
            throw BadPageException("Bad Playlist Details")
        }
    }

    override suspend fun getPlaylistVideos(initialData: JsonObject, videoCount: Int): List<PlaylistVideoDetails> {
        val content: JsonObject = try {
            initialData.getJsonObject("contents")
                .getJsonObject("twoColumnBrowseResultsRenderer")
                .getJsonArray("tabs").getJsonObject(0)
                .getJsonObject("tabRenderer")
                .getJsonObject("content")
                .getJsonObject("sectionListRenderer")
                .getJsonArray("contents").getJsonObject(0)
                .getJsonObject("itemSectionRenderer")
                .getJsonArray("contents").getJsonObject(0)
                .getJsonObject("playlistVideoListRenderer")!!
        } catch (e: NullPointerException) {
            throw BadPageException("Playlist initial data not found")
        }
        val videos: MutableList<PlaylistVideoDetails> = mutableListOf()
        populatePlaylist(content, videos,
            initialData["responseContext"]?.jsonObject?.let { getClientVersionFromContext(it) })
        return videos
    }

    override suspend fun getChannelUploadsPlaylistId(channelId: String): String {
        val channelLink = try {
            if (channelId.length == 24 && channelId.startsWith("UC")) {
                "https://www.youtube.com/channel/$channelId/videos?view=57"
            } else {
                "https://www.youtube.com/c/$channelId/videos?view=57"
            }
        } catch (e: URLParserException) {
            throw BadPageException("Upload Playlist not found")
        }
        try {
            val result = downloadByteArray(channelLink).decodeToString()
            result.split("list=").forEach { string ->
                if (string.startsWith("UU")) {
                    return string.substring(0, 24)
                }
            }
        } catch (e: IOException) {
            throw BadPageException("Could not load url: $channelLink, exception: ${e.message}")
        }
        throw BadPageException("Upload Playlist not found")
    }

    @Throws(CipherException::class, CancellationException::class)
    private suspend fun populateFormats(
        formats: MutableList<Format>,
        jsonFormats: JsonArray,
        jsUrl: String,
        isAdaptive: Boolean
    ) {
        for (i in 0 until jsonFormats.size) {
            val json: JsonObject = jsonFormats[i].jsonObject
            if ("FORMAT_STREAM_TYPE_OTF" == json.getString("type")) continue  // unsupported otf formats which cause 404 not found
            try {
                val format: Format = parseFormat(json, jsUrl, isAdaptive)
                formats.add(format)
            } catch (e: CipherException) {
                throw e
            } catch (e: YoutubeException) {
                println("Error parsing format: " + e.message)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    @Throws(YoutubeException::class, CancellationException::class)
    private suspend fun parseFormat(jsonOb: JsonObject, jsUrl: String, isAdaptive: Boolean): Format {
        val json : MutableMap<String,JsonElement> = mutableMapOf()
        jsonOb.entries.map { it.key to it.value }.toMap(json)
        if (jsonOb.containsKey("signatureCipher")) {
            val jsonCipher = mutableMapOf<String,JsonElement>()
            val cipherData: List<String> = json["signatureCipher"]
                ?.jsonPrimitive?.content?.replace("\\u0026", "&")
                ?.split("&") ?: listOf()

            for (s in cipherData) {
                val keyValue = s.split("=").toTypedArray()
                jsonCipher[keyValue[0]] = JsonPrimitive(keyValue[1])
            }
            if (!jsonCipher.containsKey("url")) {
                throw BadPageException("Could not found url in cipher data")
            }
            var urlWithSig: String? = jsonCipher["url"]!!.jsonPrimitive.content.encodeToByteArray().decodeToString()
            try {
                urlWithSig = urlWithSig!!.decodeURLPart()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            if (urlWithSig?.contains("signature") == true
                || !jsonCipher.containsKey("s") && (urlWithSig?.contains("&sig=") == true || urlWithSig?.contains("&lsig=") == true)
            ) {
                // do nothing, this is pre-signed videos with signature
            } else {
                var s = jsonCipher["s"]?.jsonPrimitive?.content?.encodeToByteArray()?.decodeToString()
                try {
                    s = s!!.decodeURLPart()
                    val cipher: Cipher = cipherFactory.createCipher(jsUrl)
                    val signature: String = cipher.getSignature(s)
                    val decipheredUrl = "$urlWithSig&sig=$signature"
                    json["url"] = JsonPrimitive(decipheredUrl)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        var itag: Itag
        try {
            itag = Itag.valueOf(("i" + json["itag"]?.jsonPrimitive?.content?.toInt()))
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            itag = Itag.unknown
            itag.id = json["itag"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
        }
        val hasVideo = itag.isVideo || json.containsKey("size") || json.containsKey("width")
        val hasAudio = itag.isAudio || json.containsKey("audioQuality")

        return when {
            hasVideo && hasAudio -> AudioVideoFormat(JsonObject(json), isAdaptive)
            hasVideo -> VideoFormat(JsonObject(json), isAdaptive)
            hasAudio -> AudioFormat(JsonObject(json), isAdaptive)
            else -> throw UnknownFormatException("unknown format with itag " + itag.id)
        }

    }

    @Throws(YoutubeException::class, CancellationException::class)
    private suspend fun populatePlaylist(
        content: JsonObject,
        videos: MutableList<PlaylistVideoDetails>,
        clientVersion: String?
    ) {
        val contents: JsonArray = when {
            content.containsKey("contents") -> { // parse first items (up to 100)
                content["contents"]!!.jsonArray
            }
            content.containsKey("continuationItems") -> { // parse continuationItems
                content["continuationItems"]!!.jsonArray
            }
            content.containsKey("continuations") -> { // load continuation
                val nextContinuationData =
                    content["continuations"]!!.jsonArray[0].jsonObject["nextContinuationData"]?.jsonObject
                val continuation = nextContinuationData?.getString("continuation")
                val ctp = nextContinuationData?.getString("clickTrackingParams")
                if(continuation != null && ctp != null && clientVersion != null)
                    loadPlaylistContinuation(continuation, ctp, videos, clientVersion)
                return
            }
            else -> { // noting found
                return
            }
        }
        for (i in 0 until contents.size) {
            val contentsItem: JsonObject = contents[i].jsonObject
            if (contentsItem.containsKey("playlistVideoRenderer")) {
                videos.add(PlaylistVideoDetails(contentsItem["playlistVideoRenderer"]!!.jsonObject))
            } else {
                if (contentsItem.containsKey("continuationItemRenderer")) {
                    val continuationEndpoint = contentsItem["continuationItemRenderer"]!!
                        .jsonObject["continuationEndpoint"]
                        ?.jsonObject
                    val continuation =
                        continuationEndpoint?.get("continuationCommand")?.jsonObject?.getString("token")
                    val ctp = continuationEndpoint?.getString("clickTrackingParams")
                    if(continuation != null && ctp != null && clientVersion != null)
                        loadPlaylistContinuation(continuation, ctp, videos, clientVersion)
                }
            }
        }
    }

    private suspend fun loadPlaylistContinuation(
        continuation: String,
        ctp: String,
        videos: MutableList<PlaylistVideoDetails>,
        clientVersion: String
    ) {
        val content: JsonObject?
        val url = "https://www.youtube.com/youtubei/v1/browse?key=AIzaSyAO_FJ2SlqU8Q4STEHLGCilw_Y9_11qcW8"
        extractor.setRequestProperty("X-YouTube-Client-Name", "1")
        extractor.setRequestProperty("X-YouTube-Client-Version", clientVersion)
        extractor.setRequestProperty("Content-Type", "application/json")
        val data: JsonObject = buildJsonObject {
            put("context", buildJsonObject {
                put("client", buildJsonObject {
                    put("clientName","WEB")
                    put("clientVersion","2.20201021.03.00")
                })
            })
            put("continuation",continuation)
            put("clickTracking", buildJsonObject { put("clickTrackingParams", ctp) })
        }
        val html: String = extractor.loadUrl(url, data.toString(), Methods.POST)

//        data['clickTracking'] = {
//                'clickTrackingParams':continuation['itct']
//            }
        try {
            val response: JsonObject = Json.decodeFromString(html)
            content = if (response.containsKey("continuationContents")) {
                response["continuationContents"]?.jsonObject?.get("playlistVideoListContinuation")?.jsonObject
            } else {
                response["onResponseReceivedActions"]
                    ?.jsonArray?.get(0)
                    ?.jsonObject?.get("appendContinuationItemsAction")?.jsonObject
            }
            content?.let { populatePlaylist(it, videos, clientVersion) }
        } catch (e: YoutubeException) {
            throw e
        } catch (e: Exception) {
            throw BadPageException("Could not parse playlist continuation json")
        }
    }

    private fun getClientVersionFromContext(context: JsonObject?): String {
        val trackingParams: JsonArray = context?.get("serviceTrackingParams")?.jsonArray ?: return "2.20200720.00.02"
        for (ti in 0 until trackingParams.size) {
            val params = trackingParams[ti].jsonObject["params"]?.jsonArray
            for (pi in 0 until (params?.size ?: 1)) {
                if (params?.get(pi)?.jsonObject?.getString("key").equals("cver")) {
                    return params?.get(pi)?.jsonObject?.getString("value") ?: "2.20200720.00.02"
                }
            }
        }
        return "2.20200720.00.02"
    }

    companion object {
        private val assetsJsRegex = Regex("\"assets\":.+?\"js\":\\s*\"([^\"]+)\"")
        private val subtitleLangCodeRegex = Regex("lang_code=\"(.{2,3})\"")
        private val embJsRegex = Regex("\"jsUrl\":\\s*\"([^\"]+)\"")
        private val textNumberRegex = Regex("[0-9]+[0-9, ']*")
        
        private fun extractNumber(text: String): Int {
            val matcher = textNumberRegex.find(text)
            return matcher?.groupValues?.get(0)?.replace("[, ']".toRegex(), "")?.toInt() ?: 0
        }
    }
}