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