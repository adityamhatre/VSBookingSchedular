package com.adityamhatre.bookingscheduler.utils

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
    }
}