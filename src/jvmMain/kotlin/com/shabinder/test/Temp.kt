package com.shabinder.test

import com.shabinder.downloader.YoutubeDownloader
import kotlinx.coroutines.runBlocking

private fun main() {
    val downloader = YoutubeDownloader()
    runBlocking {
        val video = downloader.getVideo("mGLocpX09mU")
        println(video.getAudioFormats().joinToString("\n"))
    }
}