package com.refugio.pawrescue.ui.theme.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object DateUtils {

    private const val DATE_FORMAT = "dd/MM/yyyy"
    private const val TIME_FORMAT = "HH:mm"
    private const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm"

    fun formatDate(date: Date?): String {
        if (date == null) return ""
        return SimpleDateFormat(DATE_FORMAT, Locale.getDefault()).format(date)
    }

    fun formatTime(date: Date?): String {
        if (date == null) return ""
        return SimpleDateFormat(TIME_FORMAT, Locale.getDefault()).format(date)
    }

    fun formatDateTime(date: Date?): String {
        if (date == null) return ""
        return SimpleDateFormat(DATETIME_FORMAT, Locale.getDefault()).format(date)
    }

    fun getTimeAgo(date: Date?): String {
        if (date == null) return ""

        val diff = Date().time - date.time
        val seconds = TimeUnit.MILLISECONDS.toSeconds(diff)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
        val hours = TimeUnit.MILLISECONDS.toHours(diff)
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            seconds < 60 -> "Hace $seconds segundos"
            minutes < 60 -> "Hace $minutes minutos"
            hours < 24 -> "Hace $hours horas"
            days < 7 -> "Hace $days días"
            else -> formatDate(date)
        }
    }

    fun isToday(date: Date?): Boolean {
        if (date == null) return false
        val calendar1 = Calendar.getInstance().apply { time = date }
        val calendar2 = Calendar.getInstance()

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }
}