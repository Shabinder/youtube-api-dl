package com.shabinder.downloader

import com.shabinder.downloader.TestUtils.LIVE_ID
import com.shabinder.downloader.TestUtils.WAS_LIVE_ID
import com.shabinder.downloader.TestUtils.isReachable
import com.shabinder.downloader.parser.DefaultParser
import kotlinx.serialization.json.JsonObject
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class YoutubeLiveStreamTests {

    @Test
    fun liveStreamHLS_Success() = runBlocking {
        val htmlUrl = "https://www.youtube.com/watch?v=$LIVE_ID"
        val parser = DefaultParser()
        val ytPlayerConfig: JsonObject = parser.getPlayerConfig(htmlUrl)
        val details = parser.getVideoDetails(ytPlayerConfig)
        assertNotNull(details, "there should be a video details")
        assertEquals(LIVE_ID, details.videoId, "videoId should be $LIVE_ID")
        assertTrue(details.isLive, "this should be a live video")
        assertNotNull(details.liveUrl, "there should be a live video url")
        assertTrue(isReachable(details.liveUrl!!), "url should be reachable")
    }
    
    @Test
    fun wasLiveFormats_Success() = runBlocking {
        val htmlUrl = "https://www.youtube.com/watch?v=$WAS_LIVE_ID"
        val parser = DefaultParser()
        val ytPlayerConfig: JsonObject = parser.getPlayerConfig(htmlUrl)
        val details = parser.getVideoDetails(ytPlayerConfig)
        assertNotNull(details, "there should be a video details")
        assertEquals(WAS_LIVE_ID, details.videoId, "videoId should be $WAS_LIVE_ID")
        assertTrue(details.isLiveContent == true, "videoId was live ")
        assertFalse(details.thumbnails.isEmpty(), "thumbnails should be not empty")
        assertNotEquals(0, details.lengthSeconds, "length should not be 0")
        assertNotEquals(0, details.viewCount, "viewCount should not be 0")
    }
}