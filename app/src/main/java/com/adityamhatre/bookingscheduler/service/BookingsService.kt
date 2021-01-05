package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.Application.Companion.gson
import com.adityamhatre.bookingscheduler.customViews.MonthView
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.googleapi.CalendarService
import com.google.api.services.calendar.model.Event
import com.google.gson.JsonObject
import java.time.LocalDateTime
import java.time.ZoneOffset

class BookingsService {
    private val calendarService =
        CalendarService(Application.getApplicationContext(), Application.account)

    fun getAllBookingsForDate(date: Int, month: Int, year: Int): MutableList<BookingDetails> {
        var allBookings: Sequence<Event>
        val filteredBookings = mutableSetOf<BookingDetails>()
        Accommodation.all().forEach { it ->
            allBookings = calendarService.getBookingsForDate(
                it.calendarId,
                date,
                month,
                year = 2021
            )
            filteredBookings.addAll(allBookings.groupBy { it.extendedProperties.private["id"] as String }
                .map { lv ->
                    val json = gson.fromJson(lv.value[0].description, JsonObject::class.java)
                    json.addProperty("bookingIdOnGoogle", lv.key)
                    json
                }
                .map { gson.fromJson(it, BookingDetails::class.java) }
                .filter {
                    it.checkIn.isBefore(
                        LocalDateTime.of(year, month, date, 17, 31).toInstant(
                            ZoneOffset.ofHoursMinutes(5, 30)
                        )
                    )
                }
            )
        }

        return filteredBookings.sortedWith(compareBy({ it.checkIn }, { it.checkOut }))
            .toMutableList()
    }

    fun getAllBookingsForMonth(month: Int, year: Int = 2021): MutableList<BookingDetails> {
        var allBookings: Sequence<Event>
        val filteredBookings = mutableSetOf<BookingDetails>()
        Accommodation.all().forEach { it ->
            allBookings = calendarService.getBookingsForMonth(
                it.calendarId,
                month,
                year = 2021
            )
            filteredBookings.addAll(allBookings.groupBy { it.extendedProperties.private["id"] as String }
                .map { lv ->
                    val json = gson.fromJson(lv.value[0].description, JsonObject::class.java)
                    json.addProperty("bookingIdOnGoogle", lv.key)
                    json
                }
                .map { gson.fromJson(it, BookingDetails::class.java) }
                .filter {
                    it.checkIn.isBefore(
                        LocalDateTime.of(
                            year,
                            month,
                            MonthView.maxDaysInThisMonth(month, year),
                            17,
                            31
                        ).toInstant(
                            ZoneOffset.ofHoursMinutes(5, 30)
                        )
                    )
                }
            )
        }

        return filteredBookings.sortedWith(compareBy({ it.checkIn }, { it.checkOut }))
            .toMutableList()
    }

    fun checkAvailability(timeMin: AppDateTime, timeMax: AppDateTime): List<Accommodation> {
        return calendarService.checkAvailability(timeMin, timeMax)
    }

    fun createBooking(bookingDetails: BookingDetails) {
        calendarService.createBooking(bookingDetails)
    }

    fun removeBooking(bookingDetails: BookingDetails) {
        calendarService.removeBooking(bookingDetails)
    }
}