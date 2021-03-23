package com.shabinder.downloader.utils

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

inline fun JsonObject.getString(key:String):String? = this[key]?.jsonPrimitive?.content
inline fun JsonObject.getLong(key:String):Long = this[key]?.jsonPrimitive?.content?.toLongOrNull() ?: 0
inline fun JsonObject.getInteger(key:String):Int = this[key]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
inline fun JsonObject.getBoolean(key:String):Boolean? = this[key]?.jsonPrimitive?.content?.toBoolean()
inline fun JsonObject.getFloat(key:String):Float? = this[key]?.jsonPrimitive?.content?.toFloatOrNull()
inline fun JsonObject.getDouble(key:String):Double? = this[key]?.jsonPrimitive?.content?.toDoubleOrNull()
inline fun JsonObject?.getJsonObject(key:String):JsonObject? = this?.get(key)?.jsonObject
inline fun JsonArray?.getJsonObject(index:Int):JsonObject? = this?.get(index)?.jsonObject
inline fun JsonObject?.getJsonArray(key:String):JsonArray? = this?.get(key)?.jsonArray
