package com.shabinder.downloader.cipher

class DefaultCipher(
    private val functions: List<JsFunction>,
    private val functionsMap: Map<String, CipherFunction>
) : Cipher {
    override fun getSignature(cipheredSignature: String): String {
        var signature = cipheredSignature.toCharArray()
        for (jsFunction in functions) {
            signature = functionsMap[jsFunction.name]!!.apply(signature, jsFunction.argument)
        }
        return signature.concatToString()
    }
}
