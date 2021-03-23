@file:Suppress("unused", "EnumEntryName","VARIABLE_IN_ENUM")

package com.shabinder.downloader.models

import com.shabinder.downloader.models.quality.VideoQuality
import com.shabinder.downloader.models.quality.AudioQuality

enum class Itag {
    unknown,
    i5(VideoQuality.small, AudioQuality.low), i6(VideoQuality.small, AudioQuality.low), i13(
        VideoQuality.unknown,
        AudioQuality.low
    ),
    i17(VideoQuality.tiny, AudioQuality.low), i18(VideoQuality.medium, AudioQuality.low), i22(
        VideoQuality.hd720,
        AudioQuality.medium
    ),
    i34(VideoQuality.medium, AudioQuality.medium), i35(VideoQuality.large, AudioQuality.medium), i36(
        VideoQuality.tiny,
        AudioQuality.unknown
    ),
    i37(VideoQuality.hd1080, AudioQuality.medium), i38(
        VideoQuality.highres,
        AudioQuality.medium
    ),
    i43(VideoQuality.medium, AudioQuality.medium), i44(VideoQuality.large, AudioQuality.medium), i45(
        VideoQuality.hd720,
        AudioQuality.medium
    ),
    i46(VideoQuality.hd1080, AudioQuality.medium),  // 3D videos
    i82(VideoQuality.medium, AudioQuality.medium, true), i83(VideoQuality.large, AudioQuality.medium, true), i84(
        VideoQuality.hd720,
        AudioQuality.medium,
        true
    ),
    i85(VideoQuality.hd1080, AudioQuality.medium, true), i100(VideoQuality.medium, AudioQuality.medium, true), i101(
        VideoQuality.large,
        AudioQuality.medium,
        true
    ),
    i102(VideoQuality.hd720, AudioQuality.medium, true),  // Apple HTTP Live Streaming
    i91(VideoQuality.tiny, AudioQuality.low), i92(VideoQuality.small, AudioQuality.low), i93(
        VideoQuality.medium,
        AudioQuality.medium
    ),
    i94(VideoQuality.large, AudioQuality.medium), i95(VideoQuality.hd720, AudioQuality.high), i96(
        VideoQuality.hd1080,
        AudioQuality.high
    ),
    i132(VideoQuality.small, AudioQuality.low), i151(VideoQuality.tiny, AudioQuality.low),  // DASH mp4 video
    i133(VideoQuality.small), i134(VideoQuality.medium), i135(VideoQuality.large), i136(VideoQuality.hd720), i137(
        VideoQuality.hd1080
    ),
    i138(VideoQuality.hd2160), i160(VideoQuality.tiny), i212(VideoQuality.large), i264(VideoQuality.hd1440), i266(
        VideoQuality.hd2160
    ),
    i298(VideoQuality.hd720), i299(VideoQuality.hd1080),  // DASH mp4 audio
    i139(AudioQuality.low), i140(AudioQuality.medium), i141(AudioQuality.high), i256(AudioQuality.unknown), i325(
        AudioQuality.unknown
    ),
    i328(AudioQuality.unknown),  // DASH webm video
    i167(VideoQuality.medium), i168(VideoQuality.large), i169(VideoQuality.hd720), i170(VideoQuality.hd1080), i218(
        VideoQuality.large
    ),
    i219(VideoQuality.tiny), i242(VideoQuality.small), i243(VideoQuality.medium), i244(VideoQuality.large), i245(
        VideoQuality.large
    ),
    i246(VideoQuality.large), i247(VideoQuality.hd720), i248(VideoQuality.hd1080), i271(VideoQuality.hd1440), i272(
        VideoQuality.highres
    ),
    i278(VideoQuality.tiny), i302(VideoQuality.hd720), i303(VideoQuality.hd1080), i308(VideoQuality.hd1440), i313(
        VideoQuality.hd2160
    ),
    i315(VideoQuality.hd2160),  // DASH webm audio
    i171(AudioQuality.medium), i172(AudioQuality.high),  // Dash webm audio with opus inside
    i249(AudioQuality.low), i250(AudioQuality.low), i251(AudioQuality.medium),  // Dash webm hdr video
    i330(VideoQuality.tiny), i331(VideoQuality.small), i332(VideoQuality.medium), i333(VideoQuality.large), i334(
        VideoQuality.hd720
    ),
    i335(VideoQuality.hd1080), i336(VideoQuality.hd1440), i337(VideoQuality.hd2160),  // av01 video only formats
    i394(VideoQuality.tiny), i395(VideoQuality.small), i396(VideoQuality.medium), i397(VideoQuality.large), i398(
        VideoQuality.hd720
    ),
    i399(VideoQuality.hd1080), i400(VideoQuality.hd1440), i401(VideoQuality.hd2160), i402(VideoQuality.hd2880p);

    var id = 0
    private var videoQuality: VideoQuality? = null
    private var audioQuality: AudioQuality? = null
    private var isVRor3D = false

    constructor() {}
    constructor(videoQuality: VideoQuality) : this(videoQuality, AudioQuality.noAudio, false) {}
    constructor(audioQuality: AudioQuality) : this(VideoQuality.noVideo, audioQuality, false) {}
    constructor(videoQuality: VideoQuality, audioQuality: AudioQuality) : this(videoQuality, audioQuality, false) {}
    constructor(videoQuality: VideoQuality, audioQuality: AudioQuality, isVRor3D: Boolean) {
        setID(name.substring(1).toInt())
        this.videoQuality = videoQuality
        this.audioQuality = audioQuality
        this.isVRor3D = isVRor3D
    }

    open fun setID(id: Int) {
        this.id = id
    }

    fun videoQuality(): VideoQuality? {
        return videoQuality
    }

    fun audioQuality(): AudioQuality? {
        return audioQuality
    }

    val isVideo: Boolean
        get() = this !== unknown && videoQuality !== VideoQuality.noVideo
    val isAudio: Boolean
        get() = this !== unknown && audioQuality !== AudioQuality.noAudio

    override fun toString(): String {
        return id.toString()
    }
}