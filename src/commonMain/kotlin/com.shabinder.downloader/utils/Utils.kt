package com.shabinder.downloader.utils

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.models.NetworkResult
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.http.contentLength
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.roundToInt

private val ILLEGAL_FILENAME_CHARACTERS =
    charArrayOf('/', '\n', '\r', '\t', '\u0000', '\u000C' /* = '\f'*/ , '`', '?', '*', '\\', '<', '>', '|', '\"', ':')

fun removeIllegalChars(value: String): String {
    var fileName = value
    for (c in ILLEGAL_FILENAME_CHARACTERS) {
        fileName = fileName.replace(c, '_')
    }
    return fileName
}

suspend fun downloadFile(url: String, client: HttpClient = HttpClient{}, httpBuilder: HttpRequestBuilder.()->Unit = {}): Flow<NetworkResult> {
    return flow {
        val response = client.get<HttpStatement>(url,httpBuilder).execute()
        val data = ByteArray(response.contentLength()!!.toInt())
        var offset = 0
        do {
            val currentRead = response.content.readAvailable(data, offset, data.size)
            offset += currentRead
            val progress = (offset * 100f / data.size).roundToInt()
            emit(NetworkResult.Progress(progress))
        } while (currentRead > 0)
        if (response.status.isSuccess()) {
            emit(NetworkResult.Success(data))
        } else {
            emit(NetworkResult.Error("${response.status.value} : ${response.status.description} : $url",response))
        }
        client.close()
    }
}
suspend fun downloadByteArray(url: String, client: HttpClient = HttpClient{}, httpBuilder: HttpRequestBuilder.()->Unit = {}): ByteArray {
    val response = client.get<HttpStatement>(url,httpBuilder).execute()
    val data = ByteArray(response.contentLength()!!.toInt())
    response.content.readFully(data,0,data.size)
    client.close()
    if (response.status.isSuccess()) {
        return data
    }
    throw YoutubeException.NetworkException("${response.status.value} : ${response.status.description} : $url")
}