package com.shabinder.downloader.cipher

internal class ReverseFunction : CipherFunction {
    override fun apply(array: CharArray, argument: String): CharArray {
        val sb = StringBuilder().append(array)
        return sb.reverse().toString().toCharArray()
    }
}