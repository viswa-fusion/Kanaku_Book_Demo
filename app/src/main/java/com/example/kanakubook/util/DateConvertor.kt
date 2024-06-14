package com.example.kanakubook.util

import java.text.SimpleDateFormat
import java.util.*

object DateConvertor{
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
}