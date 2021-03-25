package com.shabinder.downloader

import com.shabinder.downloader.exceptions.YoutubeException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class YoutubeChannelUploadsTests {

    @Test
    fun channelUploads() = runBlocking {
        val downloader = YoutubeDownloader()
        assertEquals(
            CHANNELIDPLAYLIST,
            downloader.getChannelUploads(CHANNELID).details.playlistId,
            "playlist id should be $CHANNELIDPLAYLIST"
        )
        assertEquals(
            CHANNELNAMEPLAYLIST,
            downloader.getChannelUploads(CHANNELNAME).details.playlistId,
            "playlist id should be $CHANNELNAMEPLAYLIST"
        )
        assertEquals(
            MUSICCHANNELIDPLAYLIST,
            downloader.getChannelUploads(MUSICCHANNELID).details.playlistId,
            "playlist id should be $MUSICCHANNELIDPLAYLIST"
        )
        assertEquals(
            MUSICCHANNELNAMEPLAYLIST,
            downloader.getChannelUploads(MUSICCHANNELNAME).details.playlistId,
            "playlist id should be $MUSICCHANNELNAMEPLAYLIST"
        )
    }

    @Test
    fun channelUploadsExceptions() = runBlocking {
        val downloader = YoutubeDownloader()
        assertFailsWith<YoutubeException.BadPageException>("should throw BadPageException") {
            downloader.getChannelUploads(NOTEXISTINGCHANNELID)
        }
        assertFailsWith<YoutubeException.BadPageException>("should throw BadPageException") {
            downloader.getChannelUploads(NOTEXISTINGCHANNELNAME)
        }
    }
    
    @Suppress("SpellCheckingInspection")
    companion object {
        const val CHANNELID = "UCSJ4gkVC6NrvII8umztf0Ow" //ChilledCow
        const val CHANNELNAME = "LinusTechTips" //LinusTechTips
        const val MUSICCHANNELID = "UCY2qt3dw2TQJxvBrDiYGHdQ" //Pink Floyd
        const val MUSICCHANNELNAME = "queen" //Queen
        const val NOTEXISTINGCHANNELID = "UCY2qtDdwVTQJxDBrYiYGHdQ"
        const val NOTEXISTINGCHANNELNAME = "thischanneldoesnotexist11111111111111111111111111"
        const val CHANNELIDPLAYLIST = "UUSJ4gkVC6NrvII8umztf0Ow" //ChilledCow's uploads
        const val CHANNELNAMEPLAYLIST = "UUXuqSBlHAE6Xw-yeJA0Tunw" //LinusTechTips's uploads
        const val MUSICCHANNELIDPLAYLIST = "UUY2qt3dw2TQJxvBrDiYGHdQ" //Pink Floyd's uploads
        const val MUSICCHANNELNAMEPLAYLIST = "UUiMhD4jzUqG-IgPzUmmytRQ" //Queen's uploads
    }
}