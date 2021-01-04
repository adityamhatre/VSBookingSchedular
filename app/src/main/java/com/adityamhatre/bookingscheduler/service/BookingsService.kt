package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.googleapi.CalendarService
import com.google.api.services.calendar.model.Event
import com.google.gson.Gson

class BookingsService {
    private val calendarService =
        CalendarService(Application.getApplicationContext(), Application.account)

    fun getAllBookingsForDate(date: Int, month: Int): List<BookingDetails> {
        val allBookings = mutableListOf<Event>()
        val gson = Gson()
        Accommodation.all().forEach {
            allBookings += calendarService.getBookingsForDate(
                it.calendarId,
                date,
                month,
                year = 2021
            ).toList()
        }

        return allBookings.groupBy { it.extendedProperties.private["id"] as String }
            .map { lv -> lv.value[0].description }
            .map { gson.fromJson(it, BookingDetails::class.java) }.toList()
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