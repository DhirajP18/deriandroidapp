package com.example.data.api

import org.json.JSONArray
import org.json.JSONObject

object ApiJsonParser {
    fun parseObject(raw: String?): JSONObject? {
        if (raw.isNullOrBlank()) return null
        return try {
            val root = JSONObject(raw)
            unwrapObject(root)
        } catch (_: Exception) {
            null
        }
    }

    fun parseArray(raw: String?): JSONArray {
        if (raw.isNullOrBlank()) return JSONArray()
        return try {
            val root = JSONObject(raw)
            val payload = root.opt("Data")
                ?: root.opt("data")
                ?: root.opt("Result")
                ?: root.opt("result")
            when (payload) {
                is JSONArray -> payload
                is String -> runCatching { JSONArray(payload) }.getOrDefault(JSONArray())
                else -> JSONArray()
            }
        } catch (_: Exception) {
            try {
                JSONArray(raw)
            } catch (_: Exception) {
                JSONArray()
            }
        }
    }

    fun readString(obj: JSONObject?, vararg keys: String): String? {
        if (obj == null) return null
        for (key in keys) {
            val value = obj.opt(key)
            if (value is String && value.isNotBlank()) return value
        }
        return null
    }

    fun readInt(obj: JSONObject?, vararg keys: String): Int? {
        if (obj == null) return null
        for (key in keys) {
            val value = obj.opt(key)
            when (value) {
                is Int -> return value
                is Number -> return value.toInt()
                is String -> value.toIntOrNull()?.let { return it }
            }
        }
        return null
    }

    fun readDouble(obj: JSONObject?, vararg keys: String): Double? {
        if (obj == null) return null
        for (key in keys) {
            val value = obj.opt(key)
            when (value) {
                is Double -> return value
                is Number -> return value.toDouble()
                is String -> value.toDoubleOrNull()?.let { return it }
            }
        }
        return null
    }

    fun readBoolean(obj: JSONObject?, vararg keys: String): Boolean? {
        if (obj == null) return null
        for (key in keys) {
            val value = obj.opt(key)
            when (value) {
                is Boolean -> return value
                is String -> if (value.equals("true", ignoreCase = true)) return true
                is Number -> return value.toInt() != 0
            }
        }
        return null
    }

    fun readNestedObject(obj: JSONObject?, vararg keys: String): JSONObject? {
        if (obj == null) return null
        for (key in keys) {
            val value = obj.opt(key)
            if (value is JSONObject) return value
            if (value is String) {
                runCatching { JSONObject(value) }.getOrNull()?.let { return it }
            }
        }
        return null
    }

    fun readArray(obj: JSONObject?, vararg keys: String): JSONArray {
        if (obj == null) return JSONArray()
        for (key in keys) {
            val value = obj.opt(key)
            when (value) {
                is JSONArray -> return value
                is String -> {
                    val parsed = runCatching { JSONArray(value) }.getOrNull()
                    if (parsed != null) return parsed
                }
            }
        }
        return JSONArray()
    }

    private fun unwrapObject(obj: JSONObject): JSONObject {
        val nested = readNestedObject(obj, "Data", "data", "Result", "result")
        return nested ?: obj
    }
}
