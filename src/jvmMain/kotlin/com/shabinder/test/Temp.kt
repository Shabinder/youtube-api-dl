package com.shabinder.test

import com.shabinder.downloader.YoutubeDownloader
import kotlinx.coroutines.runBlocking

fun main() {
    val downloader = YoutubeDownloader()
    runBlocking {
        val video = downloader.getVideo("mGLocpX09mU")
        println(video.videoWithAudioFormats().joinToString("\n"))
    }
}