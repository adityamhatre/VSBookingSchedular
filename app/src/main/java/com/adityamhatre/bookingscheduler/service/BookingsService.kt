package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.googleapi.CalendarService

class BookingsService {
    private val calendarService =
        CalendarService(Application.getApplicationContext(), Application.account)

    fun getAllBookings() {
        TODO("Not yet implemented")
    }

    fun getAllBookingsForDate(date: Int, month: Int): List<BookingDetails> {
        /*Accommodation.all().forEach {
            calendarService.addBookingForDate(
                it.calendarId,
                date,
                month,
                year = 2021,
                bookingFor = "Jhumani-${UUID.randomUUID()}"
            )
        }*/
        Accommodation.all().forEach {
            calendarService.getBookingsForDate(it.calendarId, date, month, year = 2021)
                .iterator()
                .forEach { event -> println("${event.id}, ${event.summary}, ${event.organizer.displayName}") }
        }


        TODO("Not yet implemented")

    }

    fun getAllBookingsForMonth(month: Int): List<BookingDetails> {
        TODO("Not yet implemented")
    }

    fun checkAvailability(timeMin: String, timeMax: String): List<Accommodation> {
        return calendarService.checkAvailability(timeMin, timeMax)
    }
}