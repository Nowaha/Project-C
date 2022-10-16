package xyz.nowaha.chengetawildlife.extensions


import kotlinx.serialization.json.*

fun JsonObject.getString(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

fun JsonObject.getBoolean(key: String): Boolean? {
    return this[key]?.jsonPrimitive?.booleanOrNull
}