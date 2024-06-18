package com.example.domain.helper


import java.text.SimpleDateFormat

object DateTimeHelper {

    fun getCurrentTime(): Long {
        return System.currentTimeMillis()
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