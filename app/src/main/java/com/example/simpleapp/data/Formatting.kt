package com.example.simpleapp.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private val RU_LOCALE: Locale = Locale.forLanguageTag("ru-RU")

private val DATE_TIME_FORMAT = SimpleDateFormat("dd.MM.yyyy HH:mm", RU_LOCALE)

fun formatDateTime(millis: Long): String = DATE_TIME_FORMAT.format(Date(millis))

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", RU_LOCALE).format(Date(millis))

fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes Б"
    val units = listOf("КБ", "МБ", "ГБ", "ТБ")
    var value = bytes.toDouble() / 1024
    var unitIndex = 0
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    val pattern = if (value < 10) "%.1f %s" else "%.0f %s"
    return String.format(RU_LOCALE, pattern, value, units[unitIndex])
}

fun formatResolution(width: Int, height: Int): String? =
    if (width > 0 && height > 0) "$width × $height" else null

fun formatImageFormat(mimeType: String): String? {
    val subtype = mimeType.substringAfter('/', "").uppercase(RU_LOCALE)
    return subtype.ifEmpty { null }
}

fun formatExposure(seconds: Double?): String? {
    if (seconds == null || seconds <= 0.0) return null
    return if (seconds < 1.0) {
        "1/${(1.0 / seconds).toInt()}"
    } else {
        String.format(RU_LOCALE, "%.1f с", seconds)
    }
}

fun formatAperture(fNumber: Double?): String? =
    fNumber?.takeIf { it > 0.0 }?.let { String.format(RU_LOCALE, "f/%.2f", it) }

fun formatIso(iso: Int?): String? = iso?.takeIf { it > 0 }?.let { "ISO $it" }

fun formatFocalLength(millimeters: Float?): String? =
    millimeters?.takeIf { it > 0f }?.let { String.format(RU_LOCALE, "%.0f мм", it) }

fun formatRelativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now - millis)
    return when {
        minutes < 1 -> "только что"
        minutes < 60 -> "$minutes мин назад"
        minutes < 60 * 24 -> "${minutes / 60} ч назад"
        minutes < 60 * 24 * 30 -> "${minutes / (60 * 24)} дн. назад"
        else -> formatDate(millis)
    }
}