package com.shabinder.downloader.cipher

internal class SpliceFunction : CipherFunction {
    override fun apply(array: CharArray, argument: String): CharArray {
        val deleteCount = argument.toInt()
        val spliced = CharArray(array.size - deleteCount)
        // java.lang.System.arraycopy(array, 0, spliced, 0, deleteCount)
        // java.lang.System.arraycopy(array, deleteCount * 2, spliced, deleteCount, spliced.size - deleteCount)

        for(i in (0 until deleteCount)){
            spliced[i] = array[i]
        }
        for(i in (0 until spliced.size - deleteCount)){
            spliced[i + deleteCount] = array[i + (deleteCount*2)]
        }
        return spliced
    }
}