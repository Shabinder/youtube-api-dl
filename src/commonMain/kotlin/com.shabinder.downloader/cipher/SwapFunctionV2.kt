package com.shabinder.downloader.cipher

internal class SwapFunctionV2 : CipherFunction {
    override fun apply(array: CharArray, argument: String): CharArray {
        val position = argument.toInt()
        val c = array[0]
        array[0] = array[position % array.size]
        array[position % array.size] = c
        return array
    }
}