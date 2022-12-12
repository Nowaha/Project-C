package xyz.nowaha.chengetawildlife.util.extensions


import kotlinx.serialization.json.*

fun JsonObject.getString(key: String): String? {
    return this[key]?.jsonPrimitive?.contentOrNull
}

fun JsonObject.getString(key: String, default: String): String {
    return this.getString(key) ?: default
}

fun JsonObject.getBoolean(key: String): Boolean? {
    return this[key]?.jsonPrimitive?.booleanOrNull
}

fun JsonObject.getBoolean(key: String, default: Boolean): Boolean {
    return this.getBoolean(key) ?: default
}

fun JsonObject.getInt(key: String): Int? {
    return this[key]?.jsonPrimitive?.intOrNull
}

fun JsonObject.getInt(key: String, default: Int): Int {
    return this.getInt(key) ?: default
}

fun JsonObject.getLong(key: String): Long? {
    return this[key]?.jsonPrimitive?.longOrNull
}

fun JsonObject.getLong(key: String, default: Long): Long {
    return this.getLong(key) ?: default
}

fun JsonObject.getFloat(key: String): Float? {
    return this[key]?.jsonPrimitive?.floatOrNull
}

fun JsonObject.getFloat(key: String, default: Float): Float {
    return this.getFloat(key) ?: default
}

fun JsonObject.getJsonArray(key: String) : JsonArray {
    return this[key]?.jsonArray ?: JsonArray(listOf())
}

fun JsonObject.getJsonObject(key: String) : JsonObject {
    return this[key]?.jsonObject ?: JsonObject(mapOf())
}