package com.shabinder.downloader.exceptions

sealed class YoutubeException(message: String) : Exception(message) {
    data class VideoUnavailableException(override val message: String) : YoutubeException(message)
    data class BadPageException(override val message: String) : YoutubeException(message)
    data class UnknownFormatException(override val message: String) : YoutubeException(message)
    data class CipherException(override val message: String) : YoutubeException(message)
    data class NetworkException(override val message: String) : YoutubeException(message)
    data class SubtitlesException(override val message: String) : YoutubeException(message)
    sealed class DownloadUnavailableException(override val message: String) : YoutubeException(message){
        data class LiveVideoException(override val message: String) : DownloadUnavailableException(message)
        data class RestrictedVideoException(override val message: String) : DownloadUnavailableException(message)
    }
}
