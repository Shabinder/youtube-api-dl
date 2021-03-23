package com.shabinder.downloader.cipher

interface CipherFunction {
    fun apply(array: CharArray, argument: String): CharArray
}