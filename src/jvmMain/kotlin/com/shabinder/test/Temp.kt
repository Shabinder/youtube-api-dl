package com.shabinder.test

import com.shabinder.downloader.YoutubeDownloader
import kotlinx.coroutines.runBlocking

fun main() {
    val downloader = YoutubeDownloader()
    runBlocking {
        println(downloader.getVideo("2lfETGiIDf8").videoDetails.toString())
    }
}