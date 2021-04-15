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
