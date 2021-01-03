package com.adityamhatre.bookingscheduler.dtos

data class AppDateTime(
    var date: Int,
    var month: Int,
    var year: Int,
    var hour: Int = -1,
    var minute: Int = 0
)

data class AppDate(
    var date: Int,
    var month: Int,
    var year: Int
) {
    fun isForMonth() = date == -1 && month != -1
    fun isForDate() = date != -1 && month != -1
    fun nothingInitialized() = date == -1 && month == -1
}