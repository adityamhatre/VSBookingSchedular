package com.adityamhatre.bookingscheduler.dtos

import com.adityamhatre.bookingscheduler.utils.TimeStampConverter
import java.time.OffsetDateTime

data class AppDateTime(
    var date: Int,
    var month: Int,
    var year: Int,
    var hour: Int = -1,
    var minute: Int = 0
) : Comparable<AppDateTime> {
    fun isValid(): Boolean = date != -1 && month != -1 && year != -1 && hour != -1 && minute != -1
    override fun compareTo(other: AppDateTime): Int {
        val thisDateTime = OffsetDateTime.parse(TimeStampConverter.convertToTimestampString(this)).toInstant()
        val otherDateTime = OffsetDateTime.parse(TimeStampConverter.convertToTimestampString(other)).toInstant()
        if (thisDateTime.isAfter(otherDateTime)) {
            return 1
        }
        if (thisDateTime.isBefore(otherDateTime)) {
            return -1
        }
        return 0
    }

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