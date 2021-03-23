package com.shabinder.downloader.models

import io.ktor.client.statement.HttpResponse


sealed class NetworkResult{
    data class Progress(val progress:Int):NetworkResult()
    data class Error(val errorMsg: String, val response: HttpResponse): NetworkResult()
    data class Success(val array: ByteArray):NetworkResult() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as Success

            if (!array.contentEquals(other.array)) return false

            return true
        }

        override fun hashCode(): Int {
            return array.contentHashCode()
        }
    }
}
