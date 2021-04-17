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

    //@Test
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
    
    //@Test
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