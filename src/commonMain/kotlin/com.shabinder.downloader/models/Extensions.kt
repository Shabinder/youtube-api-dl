@file:Suppress("SpellCheckingInspection")

package com.shabinder.downloader.models
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

