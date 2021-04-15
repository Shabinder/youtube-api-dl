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

import com.shabinder.downloader.exceptions.YoutubeException
import com.shabinder.downloader.exceptions.YoutubeException.CipherException
import com.shabinder.downloader.extractor.Extractor

class CachedCipherFactory(private val extractor: Extractor) : CipherFactory {

    private val knownInitialFunctionPatterns: ArrayList<Regex> = arrayListOf()
    private val functionsEquivalentMap: HashMap<Regex, CipherFunction> = HashMap()
    private val ciphers: HashMap<String, Cipher> = HashMap()

    override fun addInitialFunctionPattern(priority: Int, regex: String) {
        knownInitialFunctionPatterns.add(priority, Regex(regex))
    }

    override fun addFunctionEquivalent(regex: String, function: CipherFunction) {
        functionsEquivalentMap[Regex(regex)] = function
    }

    override suspend fun createCipher(jsUrl: String): Cipher {
        var cipher = ciphers[jsUrl]
        if (cipher == null) {
            val js: String = extractor.loadUrl(jsUrl)
            val transformFunctions = getTransformFunctions(js)
            val `var`: String = transformFunctions[0].`var`
            val transformObject = getTransformObject(`var`, js)
            val transformFunctionsMap = getTransformFunctionsMap(transformObject)
            cipher = DefaultCipher(transformFunctions, transformFunctionsMap)
            ciphers[jsUrl] = cipher
        }
        return cipher
    }

    fun clearCache() {
        ciphers.clear()
    }

    /**
     * Extract the list of "transform" JavaScript functions calls that
     * the ciphered signature is run through to obtain the actual signature.
     *
     *
     * Example of "transform" functions:
     * Mx.FH(a,11)
     * Mx["do"](a,3)
     * Mx.kT(a,51)
     *
     * @param js The content of the base.js file.
     * @return list of transform functions for deciphering
     * @throws YoutubeException if list of functions could not be found
     */
    @Throws(YoutubeException::class)
    private fun getTransformFunctions(js: String): List<JsFunction> {
        val name = getInitialFunctionName(js).replace("[^\$A-Za-z0-9_]".toRegex(), "")
        val pattern =
            Regex(Regex.escape(name) + "=function\\(\\w\\)\\{[a-z=\\.\\(\\\"\\)]*;(.*);(?:.+)\\}")
        val matcher = pattern.find(js)
        if (!matcher?.value.isNullOrBlank()) {
            val jsFunctions: Array<String> = matcher?.groupValues?.get(1)?.split(";")?.toTypedArray() ?: arrayOf()
            val transformFunctions: ArrayList<JsFunction> = ArrayList(jsFunctions.size)
            for (jsFunction in jsFunctions) {
                val parsedFunction = parseFunction(jsFunction)
                transformFunctions.add(parsedFunction)
            }
            return transformFunctions
        }
        throw CipherException("Transformation functions not found")
    }

    /**
     * Extract the JsFunction object from JavaScript function call string
     *
     *
     * Example:
     * given function call string as "Mx.FH(a,11)"
     * - object "var" would be "Mx"
     * - function "name" would be "FH"
     * - function "argument" would be "11"
     *
     * @param jsFunction JavaScript function call string
     * @return JsFunction object which represents JavaScript function call
     * @throws YoutubeException if could not parse JavaScript function call
     */
    @Throws(YoutubeException::class)
    private fun parseFunction(jsFunction: String): JsFunction {
        for (jsFunctionPattern in JS_FUNCTION_PATTERNS) {
            val matcher = jsFunctionPattern.find(jsFunction)
            if (matcher != null) {
                val `var`: String?
                var split: Array<String?> = jsFunction.split("\\.".toRegex()).toTypedArray() // case: Mx.FH(a,21)
                if (split.size > 1) {
                    `var` = split[0]
                } else {
                    split = jsFunction.split("\\[".toRegex()).toTypedArray() // case: Mx["do"](a,21)
                    if (split.size > 1) {
                        `var` = split[0]
                    } else {
                        continue
                    }
                }
                val name = matcher.groupValues[1]
                val argument = matcher.groupValues[2]
                if(`var` != null)
                    return JsFunction(`var`, name, argument)
            }
        }
        throw CipherException("Could not parse js function")
    }

    /**
     * Extract the name of the function responsible for deciphering the signature
     * based on list of known initial function patterns `knownInitialFunctionPatterns`
     *
     * @param js The content of the base.js file.
     * @return initial function name
     * @throws YoutubeException if none of known patterns matches
     */
    @Throws(YoutubeException::class)
    private fun getInitialFunctionName(js: String): String {
        for (pattern in knownInitialFunctionPatterns) {
            val matcher = pattern.find(js)
            if (!matcher?.value.isNullOrBlank()) {
                matcher?.groupValues?.get(1)?.let { return it }
            }
        }
        throw CipherException("Initial function name not found")
    }

    /**
     * Extract the function definitions – "transform object" referenced in the
     * list of transform functions.
     *
     *
     * Example of "transform object":
     * var Mx={
     * FH:function(a){a.reverse()},
     * "do":function(a,b){var c=a[0];a[0]=a[b%a.length];a[b%a.length]=c},
     * xK:function(a,b){a.splice(0,b)}
     * };
     *
     * @param var The obfuscated variable name that stores functions definitions
     * for deciphering the signature.
     * @param js  The content of the base.js file.
     * @return array of functions definitions for deciphering
     * @throws YoutubeException if "transform object" not found
     */
    @Throws(YoutubeException::class)
    private fun getTransformObject(`var`: String, js: String): Array<String> {
        var temp = `var`
        temp = temp.replace("[^\$A-Za-z0-9_]".toRegex(), "")
        temp = Regex.escape(temp)
        /* Using [\s\S] instead of `.` and `RegexOptions.DOT_ALL`
        *  As DOT_ALL OPTION ISN'T AVAILABLE on JS Platform
        * */
        val pattern =
            Regex("var $temp=\\{([\\s\\S]*?)\\};")
        val matcher = pattern.find(js)
        val res = matcher?.groupValues?.get(1)?.replace("\n".toRegex(), " ")?.split(", ")?.toTypedArray()
        return res ?: throw CipherException("Transform object not found")
    }

    /**
     * Create a map of obfuscated JavaScript function names to the Java equivalents
     *
     * @param transformObject The list of function definitions for deciphering
     * @return map of JS functions to Java equivalents
     * @throws YoutubeException if map function not found
     */
    @Throws(YoutubeException::class)
    private fun getTransformFunctionsMap(transformObject: Array<String>): Map<String, CipherFunction> {
        val mapper: MutableMap<String, CipherFunction> = HashMap()
        for (obj in transformObject) {
            @Suppress("CHANGING_ARGUMENTS_EXECUTION_ORDER_FOR_NAMED_VARARGS")
            val split = obj.split(delimiters = charArrayOf(':'),ignoreCase = true,limit = 2).toTypedArray()
            val name = split[0]
            val jsFunction = split[1]
            val function = mapFunction(jsFunction)
            mapper[name] = function
        }
        return mapper
    }

    /**
     * For a given JavaScript transform function definition, find the Java equivalent
     *
     * @param jsFunction JavaScript function definition
     * @return Java equivalent for JavaScript transform function
     * @throws YoutubeException if map function not found
     */
    @Throws(YoutubeException::class)
    private fun mapFunction(jsFunction: String): CipherFunction {
        for ((key, value) in functionsEquivalentMap) {
            val matcher = key.find(jsFunction)
            if (!matcher?.value.isNullOrBlank()) {
                return value
            }
        }
        throw CipherException("Map function not found")
    }

    companion object {
        private val INITIAL_FUNCTION_PATTERNS = arrayOf(
            "\\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*([a-zA-Z0-9$]+)\\(",
            "\\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*encodeURIComponent\\s*\\(\\s*([a-zA-Z0-9$]+)\\(",
            "(?:\\b|[^a-zA-Z0-9$])([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)",
            "([a-zA-Z0-9$]+)\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)",
            "([\"'])signature\\1\\s*,\\s*([a-zA-Z0-9$]+)\\(",
            "\\.sig\\|\\|([a-zA-Z0-9$]+)\\(",
            "yt\\.akamaized\\.net/\\)\\s*\\|\\|\\s*.*?\\s*[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*(?:encodeURIComponent\\s*\\()?\\s*()$",
            "\\b[cs]\\s*&&\\s*[adf]\\.set\\([^,]+\\s*,\\s*([a-zA-Z0-9$]+)\\(",
            "\\b[a-zA-Z0-9]+\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*([a-zA-Z0-9$]+)\\(",
            "\\bc\\s*&&\\s*a\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*([a-zA-Z0-9$]+)\\(",
            "\\bc\\s*&&\\s*[a-zA-Z0-9]+\\.set\\([^,]+\\s*,\\s*\\([^)]*\\)\\s*\\(\\s*([a-zA-Z0-9$]+)\\("
        )
        private const val FUNCTION_REVERSE_PATTERN = "\\{\\w\\.reverse\\(\\)\\}"
        private const val FUNCTION_SPLICE_PATTERN = "\\{\\w\\.splice\\(0,\\w\\)\\}"
        private const val FUNCTION_SWAP1_PATTERN =
            "\\{var\\s\\w=\\w\\[0];\\w\\[0]=\\w\\[\\w%\\w.length];\\w\\[\\w]=\\w\\}"
        private const val FUNCTION_SWAP2_PATTERN =
            "\\{var\\s\\w=\\w\\[0];\\w\\[0]=\\w\\[\\w%\\w.length];\\w\\[\\w%\\w.length]=\\w\\}"
        private const val FUNCTION_SWAP3_PATTERN =
            "function\\(\\w+,\\w+\\)\\{var\\s\\w=\\w\\[0];\\w\\[0]=\\w\\[\\w%\\w.length];\\w\\[\\w%\\w.length]=\\w\\}"
        private val JS_FUNCTION_PATTERNS: Array<Regex> = arrayOf(
            Regex("\\w+\\.(\\w+)\\(\\w,(\\d+)\\)"),
            Regex("\\w+\\[(\\\"\\w+\\\")\\]\\(\\w,(\\d+)\\)")
        )
    }

    init {
        for (pattern in INITIAL_FUNCTION_PATTERNS) {
            addInitialFunctionPattern(knownInitialFunctionPatterns.size, pattern)
        }
        addFunctionEquivalent(FUNCTION_REVERSE_PATTERN, ReverseFunction())
        addFunctionEquivalent(FUNCTION_SPLICE_PATTERN, SpliceFunction())
        addFunctionEquivalent(FUNCTION_SWAP1_PATTERN, SwapFunctionV1())
        val swapFunctionV2 = SwapFunctionV2()
        addFunctionEquivalent(FUNCTION_SWAP2_PATTERN, swapFunctionV2)
        addFunctionEquivalent(FUNCTION_SWAP3_PATTERN, swapFunctionV2)
    }
}