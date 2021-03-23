package com.shabinder.downloader.cipher

import com.shabinder.downloader.exceptions.YoutubeException
import kotlin.coroutines.cancellation.CancellationException

interface CipherFactory {

    @Throws(YoutubeException::class, CancellationException::class)
    suspend fun createCipher(jsUrl: String): Cipher

    fun addInitialFunctionPattern(priority: Int, regex: String)

    fun addFunctionEquivalent(regex: String, function: CipherFunction)
}