package com.shabinder.downloader.cipher

internal class SwapFunctionV1 : CipherFunction {
    override fun apply(array: CharArray, argument: String): CharArray {
        val position = argument.toInt()
        val c = array[0]
        array[0] = array[position % array.size]
        array[position] = c
        return array
    }
}