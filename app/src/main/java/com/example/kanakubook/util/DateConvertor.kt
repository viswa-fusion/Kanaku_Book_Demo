package com.example.kanakubook.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

object DateConvertor {
    fun millisToDateTime(millis: Long): String {
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis



        val sdfTime = if (isToday(calendar, currentTime)) {
            SimpleDateFormat("hh:mm a", Locale.getDefault())
        } else {
            SimpleDateFormat("d MMM", Locale.getDefault())
        }

        return sdfTime.format(calendar.time)
    }

    private fun isToday(calendar: Calendar, currentTimeMillis: Long): Boolean {
        val todayCalendar = Calendar.getInstance()
        todayCalendar.timeInMillis = currentTimeMillis

        return calendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR) &&
                calendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR)
    }

    fun formatTime(millis: Long): String {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = millis

        return when {
            DateUtils.isToday(millis) -> {
                SimpleDateFormat("'Today, 'hh:mm a", Locale.getDefault()).format(calendar.time)
            }

            DateUtils.isToday(millis + DateUtils.DAY_IN_MILLIS) -> {
                SimpleDateFormat("'Yesterday, 'hh:mm a", Locale.getDefault()).format(calendar.time)
            }

            else -> {
                SimpleDateFormat("d MMM, hh:mm a", Locale.getDefault()).format(calendar.time)
            }
        }
    }
}