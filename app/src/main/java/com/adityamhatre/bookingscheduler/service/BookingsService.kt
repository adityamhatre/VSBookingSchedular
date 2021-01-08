package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
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
    private val gson = Application.getInstance().gson
    private val account = Application.getInstance().account
    private val calendarService =
        CalendarService(Application.getApplicationContext(), account)
    private val herokuService = Application.getInstance().getHerokuService()

    fun getAllBookingsForDate(date: Int, month: Int, year: Int): MutableList<BookingDetails> {
        var allBookings: Sequence<Event>
        val filteredBookings = mutableSetOf<BookingDetails>()
        val map = mutableMapOf<String, HashSet<Pair<String, String>>>()
        Accommodation.all().parallelStream().forEach { accommodationIt ->
            allBookings = calendarService.getBookingsForDate(
                accommodationIt.calendarId,
                date,
                month,
                year = 2021
            )
            filteredBookings.addAll(allBookings.groupBy { it.extendedProperties.private["id"] as String }
                .map { lv ->
                    if (map.containsKey(lv.key)) {
                        map[lv.key]?.addAll(lv.value.map {
                            Pair(
                                accommodationIt.calendarId,
                                it.id
                            )
                        })
                    } else {
                        val a = HashSet<Pair<String, String>>()
                        a.addAll(lv.value.map { Pair(accommodationIt.calendarId, it.id) })
                        map[lv.key] = a
                    }
                    lv
                }
                .map { lv -> oldFormatToNewFormatMapper(lv) }
                .map { gson.fromJson(it, BookingDetails::class.java) }
                .filter {
                    val shouldFilter = it.checkIn.isBefore(
                        LocalDateTime.of(year, month, date, 17, 31).toInstant(
                            ZoneOffset.ofHoursMinutes(5, 30)
                        )
                    )
                    if (!shouldFilter) {
                        map.remove(it.bookingIdOnGoogle)
                    }
                    return@filter shouldFilter
                }
            )
        }


        return filteredBookings.sortedWith(compareBy({ it.checkIn }, { it.checkOut }))
            .map {
                it.eventIds.clear()
                it.eventIds.addAll(map[it.bookingIdOnGoogle] ?: ArrayList())
                it
            }
            .toMutableList()
    }

    private fun oldFormatToNewFormatMapper(lv: Map.Entry<String, List<Event>>): JsonObject? {
        val json = gson.fromJson(lv.value[0].description, JsonObject::class.java)
        json.addProperty("bookingIdOnGoogle", lv.key)
        return json
    }

    fun getAllBookingsForMonth(month: Int, year: Int = 2021): MutableList<BookingDetails> {
        var allBookings: Sequence<Event>
        val filteredBookings = mutableSetOf<BookingDetails>()
        val map = mutableMapOf<String, HashSet<Pair<String, String>>>()
        Accommodation.all().parallelStream().forEach { accommodationIt ->
            allBookings = calendarService.getBookingsForMonth(
                accommodationIt.calendarId,
                month,
                year = 2021
            )
            filteredBookings.addAll(allBookings.groupBy { it.extendedProperties.private["id"] as String }
                .map { lv ->
                    if (map.containsKey(lv.key)) {
                        map[lv.key]?.addAll(lv.value.map {
                            Pair(
                                accommodationIt.calendarId,
                                it.id
                            )
                        })
                    } else {
                        val a = HashSet<Pair<String, String>>()
                        a.addAll(lv.value.map { Pair(accommodationIt.calendarId, it.id) })
                        map[lv.key] = a
                    }
                    lv
                }
                .map { lv -> oldFormatToNewFormatMapper(lv) }
                .map { gson.fromJson(it, BookingDetails::class.java) }
                .filter {
                    val shouldFilter = it.checkIn.isBefore(
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
                    if (!shouldFilter) {
                        map.remove(it.bookingIdOnGoogle)
                    }
                    return@filter shouldFilter
                }
            )
        }

        return filteredBookings.sortedWith(compareBy({ it.checkIn }, { it.checkOut }))
            .map {
                it.eventIds.clear()
                it.eventIds.addAll(map[it.bookingIdOnGoogle] ?: ArrayList())
                it
            }
            .toMutableList()
    }

    fun checkAvailability(timeMin: AppDateTime, timeMax: AppDateTime): List<Accommodation> {
        return calendarService.checkAvailability(timeMin, timeMax)
    }

    fun createBooking(bookingDetails: BookingDetails) {
        calendarService.createBooking(bookingDetails)
        herokuService.notifyNewBooking(bookingDetails)
    }

    fun removeBooking(bookingDetails: BookingDetails) {
        calendarService.removeBooking(bookingDetails)
    }

    fun updateBooking(bookingDetails: BookingDetails) {
        calendarService.updateBooking(bookingDetails)
        herokuService.notifyUpdateBooking(bookingDetails)
    }
}