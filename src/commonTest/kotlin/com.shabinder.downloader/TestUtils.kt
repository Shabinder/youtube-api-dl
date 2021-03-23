@file:Suppress("PropertyName", "SpellCheckingInspection")

package com.shabinder.downloader

import io.ktor.client.HttpClient
import io.ktor.client.request.head
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.errors.IOException

internal object TestUtils {
    const val ME_AT_THE_ZOO_ID = "jNQXAC9IVRw" // me at the zoo
    const val N3WPORT_ID = "DFdOcVpRhWI" // N3WPORT - Alive (feat. Neoni) [NCS Release]
    const val LIVE_ID = "5qap5aO4i9A"
    const val WAS_LIVE_ID = "boSGRDYm92E"

    suspend fun isReachable(url: String): Boolean {
        val client = HttpClient {}
        return try {
            val resp = client.head<HttpStatement>(url).execute()
            val responseCode: Int = resp.status.value
            return (responseCode in 200..399)
        } catch (exception: IOException) {
            false
        } finally {
            client.close()
        }
    }
}