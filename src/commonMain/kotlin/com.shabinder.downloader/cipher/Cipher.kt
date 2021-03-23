package com.shabinder.downloader.cipher

interface Cipher {
    fun getSignature(cipheredSignature: String): String
}
