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

    @Test
    fun subtitlesInfo_ExtractFromCaptions_Success() = runBlocking {
        val video: YoutubeVideo = downloader.getVideo(N3WPORT_ID)
        val subtitlesInfos: List<SubtitlesInfo> = video.subtitlesInfo
        assertFalse(subtitlesInfos.isEmpty(), "subtitles info should not be empty")
    }

    @Test
    fun subtitlesInfo_ExtractSubtitles_Success() = runBlocking {
        val subtitlesInfos: List<SubtitlesInfo> = downloader.getVideoSubtitles(N3WPORT_ID)
        assertFalse(subtitlesInfos.isEmpty(), "subtitles info should not be empty")
    }

    @Test
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