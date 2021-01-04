package com.adityamhatre.bookingscheduler.utils

import com.adityamhatre.bookingscheduler.dtos.AppDateTime

class TwoDigitFormatter {
    companion object {
        fun toTwoDigits(arg: Int): String = if (arg >= 10) arg.toString() else "0$arg"
    }
}

class TimeStampConverter {
    companion object {
        fun convertToTimestampString(
            year: Int,
            month: Int,
            date: Int,
            hour: Int,
            minute: Int
        ): String {
            return "${year}-${TwoDigitFormatter.toTwoDigits(month)}-${
                TwoDigitFormatter.toTwoDigits(date)
            }T${TwoDigitFormatter.toTwoDigits(hour)}:${TwoDigitFormatter.toTwoDigits(minute)}:00+05:30"
        }

        fun convertToTimestampString(appDateTime: AppDateTime): String {
            return convertToTimestampString(
                appDateTime.year,
                appDateTime.month,
                appDateTime.date,
                appDateTime.hour,
                appDateTime.minute
            )
        }
    }
}