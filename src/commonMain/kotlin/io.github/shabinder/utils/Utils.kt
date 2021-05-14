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

package io.github.shabinder.utils

import io.github.shabinder.exceptions.YoutubeException
import io.github.shabinder.models.NetworkResult
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.errors.*
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
    val response = try {
        client.get<ByteArray>(url,httpBuilder)
    } catch (e: ClientRequestException){
        throw YoutubeException.BadPageException(e.message ?: "Could Not Fetch $url")
    }
    catch (e: IOException){
        throw YoutubeException.NetworkException("Could Not Fetch $url")
    }
    /*
    * For Live Videos , `response.contentLength() == null`
    * */
    //val response = client.get<HttpStatement>(url,httpBuilder).execute()
    //val data = ByteArray(response.contentLength()!!.toInt())
    //response.content.readFully(data,0,data.size)
    client.close()
    return response
}