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