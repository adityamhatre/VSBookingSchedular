package com.adityamhatre.bookingscheduler.utils

import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import java.time.Instant
import java.time.ZoneId
import java.util.Date
import java.util.Locale

class TwoDigitFormatter {
    companion object {
        fun toTwoDigits(arg: Int): String = if (arg >= 10) arg.toString() else "0$arg"
    }
}

class Utils {
    companion object {

        fun toTitleCase(str: String): String {
            val converted = str[0].uppercaseChar() + str.substring(1).lowercase(Locale.US)
            return converted.split("_")
                .takeIf { it.size > 1 }?.joinToString(" ") { toTitleCase(it) }
                ?: converted
        }

        fun toHumanDate(instant: Instant): String {
            val localDateTime = Date.from(instant)
                .toInstant()
                .atZone(ZoneId.of(ZoneId.SHORT_IDS["IST"]))
                .toLocalDateTime()

            val day =
                if (localDateTime.dayOfMonth > 9) "${localDateTime.dayOfMonth}" else "0${localDateTime.dayOfMonth}"
            val month = toTitleCase(localDateTime.month.name)
            val year = localDateTime.year

            val hourValue =
                if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
            val hour = if (hourValue > 9) "$hourValue" else "0${hourValue}"
            val minute =
                if (localDateTime.minute > 9) "${localDateTime.minute}" else "0${localDateTime.minute}"
            val amPm = if (localDateTime.hour >= 12) "PM" else "AM"

            return "$day $month $year, $hour:$minute $amPm"
        }

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