package com.shabinder.downloader

import com.shabinder.downloader.TestUtils.ME_AT_THE_ZOO_ID
import com.shabinder.downloader.TestUtils.isReachable
import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.models.Extension
import com.shabinder.downloader.models.VideoDetails
import com.shabinder.downloader.models.YoutubeVideo
import com.shabinder.downloader.models.formats.AudioVideoFormat
import com.shabinder.downloader.models.formats.Format
import com.shabinder.downloader.models.quality.AudioQuality
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

internal class YoutubeVideoExtractorTests {

    private val downloader: YoutubeDownloader = YoutubeDownloader()
    private val scope = CoroutineScope(Dispatchers.Default)

    @Test
    fun videoWithoutSignatureSuccess() {
            scope.launch {
                val video: YoutubeVideo = downloader.getVideo(ME_AT_THE_ZOO_ID)
                val details: VideoDetails = video.videoDetails
                assertEquals(ME_AT_THE_ZOO_ID, details.videoId, "videoId should be $ME_AT_THE_ZOO_ID")
                val title = "Me at the zoo"
                assertEquals(title, details.title, "title should be $title")
                assertFalse(details.thumbnails.isEmpty(), "thumbnails should be not empty")
                assertNotEquals(0, details.lengthSeconds, "length should not be 0")
                assertNotEquals(0, details.viewCount, "viewCount should not be 0")
                val formats: List<Format> = video.formats
                assertFalse(formats.isEmpty(), "formats should not be empty")
                val itag = 18
                val format = video.getFormatByItag(itag)
                assertNotNull(format, "findFormatByItag should return not null format")
                assertTrue(
                    format is AudioVideoFormat,
                    "format with itag $itag should be instance of AudioVideoFormat"
                )
                assertEquals(itag, format.itag.id, "itag should be $itag")
                val expectedWidth = 320
                val width: Int = format.width
                assertNotNull(width, "width should not be null")
                assertEquals(
                    expectedWidth,
                    width,
                    "format with itag $itag should have width $expectedWidth"
                )
                val expectedHeight = 240
                val height: Int = format.height
                assertNotNull(height, "height should not be null")
                assertEquals(
                    expectedHeight,
                    height,
                    "format with itag $itag should have height $expectedHeight"
                )
                val expectedAudioQuality: AudioQuality = AudioQuality.low
                assertEquals(
                    expectedAudioQuality,
                    format.audioQuality,
                    "audioQuality should be " + expectedAudioQuality.name
                )
                val expectedMimeType = "video/mp4"
                assertTrue(format.mimeType?.contains(expectedMimeType) == true, "mimetype should be $expectedMimeType")
                val expectedExtension: Extension = Extension.MPEG4
                assertEquals(
                    expectedExtension,
                    format.extension,
                    "extension should be " + expectedExtension.value
                )
                val expectedLabel = "240p"
                assertEquals(
                    expectedLabel,
                    format.qualityLabel,
                    "qualityLable should be $expectedLabel"
                )
                assertNotNull(format.url, "url should not be null")
                assertTrue(isReachable(format.url!!), "url should be reachable")
            }
        }

    @Test
    fun videoUnavailableThrowsException() {
        scope.launch {
            val unavailableVideoId = "12345678901"
            assertFailsWith<YoutubeException.BadPageException>("getVideo should throw exception for unavailable video") {
                downloader.getVideo(unavailableVideoId)
            }
        }
    }

    @Test
    fun addInitialFunctionPattern_Success() {
        scope.launch {
            downloader.addCipherFunctionPattern(
                0,
                "([a-zA-Z0-9$]+)\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)"
            )
            val videoId = "SmM0653YvXU"
            assertFailsWith<YoutubeException.CipherException>("getVideo should throw CipherException if initial function patterns has wrong priority") {
                downloader.getVideo(videoId)
            }

            downloader.addCipherFunctionPattern(
                0,
                "(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)"
            )
            assertNotNull(
                downloader.getVideo(videoId),
                "getVideo should not throw exception if initial function patterns has correct priority"
            )
        }
    }
}