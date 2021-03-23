@file:Suppress("SpellCheckingInspection")

package com.shabinder.downloader.models.formats

import com.shabinder.downloader.models.Extension
import com.shabinder.downloader.models.Itag
import com.shabinder.downloader.utils.getInteger
import com.shabinder.downloader.utils.getLong
import com.shabinder.downloader.utils.getString
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

abstract class Format protected constructor(json: JsonObject, val isAdaptive: Boolean) {
    val itag: Itag
    val url: String?
    val mimeType: String?
    var extension: Extension? = null
    val bitrate: Int
    val contentLength: Long
    val lastModified: Long
    val approxDurationMs: Long

    abstract fun type(): String?

    companion object {
        const val AUDIO = "audio"
        const val VIDEO = "video"
        const val AUDIO_VIDEO = "audio/video"
    }

    init {
        var itag: Itag
        try {
            itag = Itag.valueOf("i" + json["itag"]?.jsonPrimitive)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            itag = Itag.unknown
            itag.setID(json["itag"]?.jsonPrimitive?.content?.toInt() ?: 0)
        }
        this.itag = itag
        url = json.getString("url")?.replace("\\u0026", "&")
        mimeType = json.getString("mimeType")
        bitrate = json.getInteger("bitrate") ?: 0
        contentLength = json.getLong("contentLength") ?: 0
        lastModified = json.getLong("lastModified") ?: 0
        approxDurationMs = json.getLong("approxDurationMs") ?: 0
        extension = if (mimeType == null || mimeType.isEmpty()) {
            Extension.UNKNOWN
        } else if (mimeType.contains(Extension.MPEG4.value)) {
            if (this is AudioFormat) Extension.M4A else Extension.MPEG4
        } else if (mimeType.contains(Extension.WEBM.value)) {
            if (this is AudioFormat) Extension.WEBA else Extension.WEBM
        } else if (mimeType.contains(Extension.FLV.value)) {
            Extension.FLV
        } else if (mimeType.contains(Extension._3GP.value)) {
            Extension._3GP
        } else {
            Extension.UNKNOWN
        }
    }
}
