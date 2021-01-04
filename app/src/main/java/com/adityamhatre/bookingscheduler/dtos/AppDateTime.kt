package com.adityamhatre.bookingscheduler.dtos

import com.adityamhatre.bookingscheduler.utils.TimeStampConverter
import com.adityamhatre.bookingscheduler.utils.TwoDigitFormatter
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

data class AppDateTime(
    var date: Int,
    var month: Int,
    var year: Int,
    var hour: Int = -1,
    var minute: Int = 0
) : Comparable<AppDateTime> {

    fun isValid(): Boolean = date != -1 && month != -1 && year != -1 && hour != -1 && minute != -1

    fun toHumanFormat() =
        "${TwoDigitFormatter.toTwoDigits(date)}/${TwoDigitFormatter.toTwoDigits(month)}/$year ${
            TwoDigitFormatter.toTwoDigits(if (hour == 0) 12 else hour % 12)
        }:${TwoDigitFormatter.toTwoDigits(minute)} ${if (hour in 0..12) "AM" else "PM"}"


    override fun compareTo(other: AppDateTime): Int {
        val thisDateTime =
            OffsetDateTime.parse(TimeStampConverter.convertToTimestampString(this)).toInstant()
        val otherDateTime =
            OffsetDateTime.parse(TimeStampConverter.convertToTimestampString(other)).toInstant()
        if (thisDateTime.isAfter(otherDateTime)) {
            return 1
        }
        if (thisDateTime.isBefore(otherDateTime)) {
            return -1
        }
        return 0
    }

    fun toInstant(): Instant =
        LocalDateTime.of(year, month, date, hour, minute).atZone(ZoneId.of(ZoneId.SHORT_IDS["IST"]))
            .toInstant()

}

data class AppDate(
    var date: Int,
    var month: Int,
    var year: Int
) {
    fun isForMonth() = date == -1 && month != -1
    fun isForDate() = date != -1 && month != -1
    fun nothingInitialized() = date == -1 && month == -1
}