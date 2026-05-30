package com.example.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private val isoParser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val compactFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.US)
    private val readableFormatter = SimpleDateFormat("dd MMM yyyy", Locale.US)

    fun getTodayFormatted(): String {
        return readableFormatter.format(Date())
    }

    fun getTodayIsoString(): String {
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return df.format(Date())
    }

    fun toIso8601String(year: Int, month: Int, day: Int): String {
        val cal = Calendar.getInstance().apply {
            set(year, month, day, 12, 0, 0)
        }
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return df.format(cal.time)
    }

    fun formatCompactDate(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return "N/A"
        return try {
            // Remove fractional seconds if exist
            val cleanString = isoString.substringBefore(".")
            val parsedDate = isoParser.parse(cleanString)
            if (parsedDate != null) compactFormatter.format(parsedDate) else "N/A"
        } catch (e: Exception) {
            isoString.substringBefore("T")
        }
    }

    fun formatReadableDate(isoString: String?): String {
        if (isoString.isNullOrEmpty()) return "N/A"
        return try {
            val cleanString = isoString.substringBefore(".")
            val parsedDate = isoParser.parse(cleanString)
            if (parsedDate != null) readableFormatter.format(parsedDate) else "N/A"
        } catch (e: Exception) {
            isoString.substringBefore("T")
        }
    }

    fun getDaysAgoIso(days: Int): String {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, -days)
        }
        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.000'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        return df.format(cal.time)
    }

    fun getWeeksAgoIso(weeks: Int): String {
        return getDaysAgoIso(weeks * 7)
    }
}
