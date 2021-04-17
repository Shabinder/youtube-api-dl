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

import com.shabinder.downloader.TestUtils.N3WPORT_ID
import com.shabinder.downloader.models.Extension
import com.shabinder.downloader.models.YoutubeVideo
import com.shabinder.downloader.models.subtitles.SubtitlesInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class YoutubeSubtitlesExtractorTests {

    private val downloader: YoutubeDownloader = YoutubeDownloader()

    //@Test
    fun subtitlesInfo_ExtractFromCaptions_Success() = runBlocking {
        val video: YoutubeVideo = downloader.getVideo(N3WPORT_ID)
        val subtitlesInfos: List<SubtitlesInfo> = video.subtitlesInfo
        assertFalse(subtitlesInfos.isEmpty(), "subtitles info should not be empty")
    }

    //@Test
    fun subtitlesInfo_ExtractSubtitles_Success() = runBlocking {
        val subtitlesInfos: List<SubtitlesInfo> = downloader.getVideoSubtitles(N3WPORT_ID)
        assertFalse(subtitlesInfos.isEmpty(), "subtitles info should not be empty")
    }

    //@Test
    fun downloadUrlSuccess() = runBlocking {
        val subtitlesInfos: List<SubtitlesInfo> = downloader.getVideoSubtitles(N3WPORT_ID)
        for (info in subtitlesInfos) {
            var downloadUrl: String = info.subtitles.downloadUrl
            assertEquals(info.url, downloadUrl, "download url should be equals to info url")
            downloadUrl = info.subtitles
                .formatTo(Extension.JSON3)
                .downloadUrl
            assertTrue(
                downloadUrl.contains("&fmt=" + Extension.JSON3.value),
                "download url should contains format query param"
            )
        }
    }
}