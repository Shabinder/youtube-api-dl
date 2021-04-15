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
