package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.Application.Companion.gson
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.googleapi.CalendarService
import com.google.api.services.calendar.model.Event
import java.time.LocalDateTime
import java.time.ZoneOffset

class BookingsService {
    private val calendarService =
        CalendarService(Application.getApplicationContext(), Application.account)

    fun getAllBookingsForDate(date: Int, month: Int, year: Int): List<BookingDetails> {
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
                .map { lv -> lv.value[0].description }
                .map { gson.fromJson(it, BookingDetails::class.java) }.toList()
                .filter {
                    it.checkIn.isBefore(
                        LocalDateTime.of(year, month, date, 17, 31).toInstant(
                            ZoneOffset.ofHoursMinutes(5, 30)
                        )
                    )
                }.toList()
            )
        }

        return filteredBookings.toList()
    }

    fun getAllBookingsForMonth(month: Int): List<BookingDetails> {
        TODO("Not yet implemented")
    }

    fun checkAvailability(timeMin: AppDateTime, timeMax: AppDateTime): List<Accommodation> {
        return calendarService.checkAvailability(timeMin, timeMax)
    }

    fun createBooking(bookingDetails: BookingDetails) {
        calendarService.createBooking(bookingDetails)
    }
}