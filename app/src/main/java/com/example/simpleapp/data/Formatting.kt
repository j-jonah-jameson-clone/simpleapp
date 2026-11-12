package com.example.simpleapp.data

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

private fun isEnglish(): Boolean = Locale.getDefault().language == "en"

fun formatDateTime(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(Date(millis))

fun formatDate(millis: Long): String =
    SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(Date(millis))

fun formatFileSize(bytes: Long): String {
    val units = if (isEnglish()) {
        listOf("B", "KB", "MB", "GB", "TB")
    } else {
        listOf("Б", "КБ", "МБ", "ГБ", "ТБ")
    }
    if (bytes < 1024) return "$bytes ${units.first()}"

    var value = bytes.toDouble() / 1024
    var unitIndex = 1
    while (value >= 1024 && unitIndex < units.lastIndex) {
        value /= 1024
        unitIndex++
    }
    val pattern = if (value < 10) "%.1f %s" else "%.0f %s"
    return String.format(Locale.getDefault(), pattern, value, units[unitIndex])
}

fun formatResolution(width: Int, height: Int): String? =
    if (width > 0 && height > 0) "$width × $height" else null

fun formatImageFormat(mimeType: String): String? {
    val subtype = mimeType.substringAfter('/', "").uppercase(Locale.getDefault())
    return subtype.ifEmpty { null }
}

fun formatExposure(seconds: Double?): String? {
    if (seconds == null || seconds <= 0.0) return null
    return if (seconds < 1.0) {
        "1/${(1.0 / seconds).toInt()}"
    } else {
        val unit = if (isEnglish()) "s" else "с"
        String.format(Locale.getDefault(), "%.1f $unit", seconds)
    }
}

fun formatAperture(fNumber: Double?): String? =
    fNumber?.takeIf { it > 0.0 }?.let { String.format(Locale.getDefault(), "f/%.2f", it) }

fun formatIso(iso: Int?): String? = iso?.takeIf { it > 0 }?.let { "ISO $it" }

fun formatFocalLength(millimeters: Float?): String? {
    if (millimeters == null || millimeters <= 0f) return null
    val unit = if (isEnglish()) "mm" else "мм"
    return String.format(Locale.getDefault(), "%.0f $unit", millimeters)
}

fun formatRelativeTime(millis: Long, now: Long = System.currentTimeMillis()): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(now - millis)
    val english = isEnglish()
    return when {
        minutes < 1 -> if (english) "just now" else "только что"
        minutes < 60 -> if (english) "$minutes min ago" else "$minutes мин назад"
        minutes < 60 * 24 -> if (english) "${minutes / 60} h ago" else "${minutes / 60} ч назад"
        minutes < 60 * 24 * 30 ->
            if (english) "${minutes / (60 * 24)} d ago" else "${minutes / (60 * 24)} дн. назад"
        else -> formatDate(millis)
    }
}