package io.github.shabinder

import io.github.shabinder.models.Extension
import io.github.shabinder.models.YoutubeVideo
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.HttpStatement
import io.ktor.client.statement.response
import io.ktor.http.HttpStatusCode
import kotlin.test.Test

class Extra {

    //@Test
    fun main() = runBlocking {
        val downloader: YoutubeDownloader = YoutubeDownloader(enableCORSProxy = false)
        val videoIDs = listOf(
            "MCTTPsUOUgI",
            "VMjQoycq3Wc",
            "GV7wFVkMv-Y",
        )

        /*videoIDs.forEach {
            downloader.fetchVideoM4aLink(it)
        }*/

        //validateLink("https://www.google.com/")
        val playlistIDs = listOf(
            "RDCLAK5uy_mUvTtdERIHEiVAHIkV3GRndrY-H4M2nnA"
        )

        playlistIDs.forEach {
            downloader.fetchPlaylistM4aDetails(it)
        }
    }

    private val client = HttpClient {
        expectSuccess = false
    }

    private suspend fun validateLink(link: String): Boolean {
        var status = HttpStatusCode.BadRequest
        client.get<HttpStatement>(link).execute { res -> status = res.status }
        return status == HttpStatusCode.OK
    }

    private suspend fun YoutubeDownloader.fetchPlaylistM4aDetails(playlistId: String) {
        val playlist = getPlaylist(playlistId)

        println("Fetching Playlist:   $playlistId")
        for (video in playlist.videos) {
            fetchVideoM4aLink(video.videoId!!)
        }
        println("\n--------------------------------------------------------------------------\n\n")
    }


    private suspend fun YoutubeDownloader.fetchVideoM4aLink(videoId: String): String? {
        var retryCount = 3
        var validM4aLink: String? = null

        println("Fetching video:   $videoId")
        while (validM4aLink.isNullOrEmpty() && retryCount > 0) {
            val video: YoutubeVideo = getVideo(videoId)
            val m4aLink = video.getAudioWithExtension(Extension.M4A).first().url!!
            println(video.getAudioWithExtension(Extension.M4A).joinToString("\n"))

            if (validateLink(m4aLink)) {
                validM4aLink = m4aLink
                println("Link is VALID")
            }

            println("\n------------------------------------------------------------------\n")
            retryCount--
        }

        return validM4aLink
    }
}