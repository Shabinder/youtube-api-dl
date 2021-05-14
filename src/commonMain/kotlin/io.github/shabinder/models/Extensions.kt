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

@file:Suppress("SpellCheckingInspection")

package io.github.shabinder.models
sealed class Extension(val value: String) {

    object MPEG4 :Extension("mp4")
    object WEBM : Extension("webm")
    object _3GP : Extension("3gp")
    object FLV : Extension("flv")

    // audio
    object M4A : Extension("m4a")
    object WEBA : Extension("weba")

    // subtitles
    object JSON3 : Extension("json3")
    object SUBRIP : Extension("srt")
    object TRANSCRIPT_V1 : Extension("srv1")
    object TRANSCRIPT_V2 : Extension("srv2")
    object TRANSCRIPT_V3 : Extension("srv3")
    object TTML : Extension("ttml")
    object WEBVTT: Extension("vtt")

    // other
    object UNKNOWN : Extension("unknown")

    val isAudio: Boolean
        get() = this == M4A || this == WEBM
    val isVideo: Boolean
        get() = this == MPEG4 || this == WEBM || this == _3GP || this == FLV
    val isSubtitle: Boolean
        get() = this == SUBRIP || this == TRANSCRIPT_V1 || this == TRANSCRIPT_V2 || this == TRANSCRIPT_V3 || this == TTML || this == WEBVTT || this == JSON3

}

