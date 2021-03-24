package com.shabinder.downloader.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

fun JsonObject.getString(key:String):String? = this[key]?.jsonPrimitive?.content
fun JsonObject.getLong(key:String):Long = this[key]?.jsonPrimitive?.content?.toLongOrNull() ?: 0
fun JsonObject.getInteger(key:String):Int = this[key]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
fun JsonObject.getBoolean(key:String):Boolean? = this[key]?.jsonPrimitive?.content?.toBoolean()
fun JsonObject.getFloat(key:String):Float? = this[key]?.jsonPrimitive?.content?.toFloatOrNull()
fun JsonObject.getDouble(key:String):Double? = this[key]?.jsonPrimitive?.content?.toDoubleOrNull()
fun JsonObject?.getJsonObject(key:String):JsonObject? = this?.get(key)?.jsonObject
fun JsonArray?.getJsonObject(index:Int):JsonObject? = this?.get(index)?.jsonObject
fun JsonObject?.getJsonArray(key:String):JsonArray? = this?.get(key)?.jsonArray
