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

package io.github.shabinder.extractor

import io.github.shabinder.exceptions.YoutubeException
import io.github.shabinder.models.Methods
import kotlin.coroutines.cancellation.CancellationException

interface Extractor {
    fun setRequestProperty(key: String, value: String)
    fun setRetryOnFailure(retryOnFailure: Int)

    @Throws(YoutubeException::class)
    fun extractYtPlayerConfig(html: String): String

    @Throws(YoutubeException::class)
    fun extractYtInitialData(html: String): String

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun loadUrl(url: String, rawData: String? = null, method: Methods = Methods.GET): String
}