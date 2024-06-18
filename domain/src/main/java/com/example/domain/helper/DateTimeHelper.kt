package com.example.domain.helper


import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Date
object DateTimeHelper {

    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
    }

    fun convertToStdTime(millis: Long?): String? {
        return if (millis != null) {
            val localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.systemDefault())
            val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a")
            localDateTime.format(formatter)
        } else null
    }

    fun convertToMillis(string: String): Long? {
        return try {
            val formatter = SimpleDateFormat("dd/mm/yyyy")
            val date: Date = formatter.parse(string)
            date.time
        } catch (e: Exception) {
            null
        }
    }


    fun dateStringToMillis(dateString: String): Long? {
        try {
            val formatter = SimpleDateFormat("d/M/yyyy")
            val date = formatter.parse(dateString)
            return date?.time
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

}