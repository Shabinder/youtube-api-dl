package com.shabinder.downloader.models.playlist

data class PlaylistDetails(
    val playlistId: String,
    val title: String,
    val author: String?,
    val videoCount: Int,
    val viewCount: Int
)