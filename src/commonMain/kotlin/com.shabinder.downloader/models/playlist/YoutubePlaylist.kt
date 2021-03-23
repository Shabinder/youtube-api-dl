package com.shabinder.downloader.models.playlist

data class YoutubePlaylist(
    val details: PlaylistDetails,
    val videos: List<PlaylistVideoDetails>
) {
    fun findVideoById(videoId: String?): PlaylistVideoDetails? = videos.firstOrNull { it.videoId.equals(videoId) }
}