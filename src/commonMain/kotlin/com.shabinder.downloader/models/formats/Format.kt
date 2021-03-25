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

    val itag: Itag = try {
        Itag.valueOf("i" + json["itag"]?.jsonPrimitive)
    } catch (e: IllegalArgumentException) {
        e.printStackTrace()
        Itag.unknown.apply {
            setID(json["itag"]?.jsonPrimitive?.content?.toInt() ?: 0)
        }
    }

    val url: String? = json.getString("url")?.replace("\\u0026", "&")
    val mimeType: String? = json.getString("mimeType")
    val bitrate: Int = json.getInteger("bitrate")
    val contentLength: Long = json.getLong("contentLength")
    val lastModified: Long = json.getLong("lastModified")
    val approxDurationMs: Long = json.getLong("approxDurationMs")
    var extension: Extension = when {
        mimeType == null || mimeType.isEmpty() -> {
            Extension.UNKNOWN
        }
        mimeType.contains(Extension.MPEG4.value) -> {
            if (this is AudioFormat) Extension.M4A else Extension.MPEG4
        }
        mimeType.contains(Extension.WEBM.value) -> {
            if (this is AudioFormat) Extension.WEBA else Extension.WEBM
        }
        mimeType.contains(Extension.FLV.value) -> {
            Extension.FLV
        }
        mimeType.contains(Extension._3GP.value) -> {
            Extension._3GP
        }
        else -> {
            Extension.UNKNOWN
        }
    }

    abstract fun type(): String?

    companion object {
        const val AUDIO = "audio"
        const val VIDEO = "video"
        const val AUDIO_VIDEO = "audio/video"
    }
}
