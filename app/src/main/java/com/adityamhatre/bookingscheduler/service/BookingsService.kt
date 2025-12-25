package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.customViews.MonthView
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.dtos.GCalResult
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.exceptions.NeedsConsentException
import com.adityamhatre.bookingscheduler.googleapi.CalendarService
import com.google.api.services.calendar.model.Event
import com.google.gson.JsonObject
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.concurrent.ConcurrentHashMap

class BookingsService {
    private val gson = Application.getInstance().gson
    private val account = Application.getInstance().account
    private val calendarService =
        CalendarService(Application.getApplicationContext(), account)
    private val renderService = Application.getInstance().getRenderService()

    private val needsConsentIntent = java.util.concurrent.atomic.AtomicReference<android.content.Intent?>(null)
    private val firstFailure = java.util.concurrent.atomic.AtomicReference<Throwable?>(null)

    fun getAllBookingsForDate(date: Int, month: Int, year: Int): MutableList<BookingDetails> {
        var allBookings: Sequence<GCalResult<Event>>
        val filteredBookings = mutableSetOf<BookingDetails>()
        val map = mutableMapOf<String, HashSet<Pair<String, String>>>()
        Accommodation.allForGettingBookings().parallelStream().forEach { accommodationIt ->
            allBookings = calendarService.getBookingsForDate(
                accommodationIt.calendarId,
                date,
                month,
                year = year
            )
            filteredBookings.addAll(getFilteredBookings(allBookings, map, accommodationIt, MonthOrDate.Date, year, month, date))
        }

        needsConsentIntent.get()?.let { intent -> throw NeedsConsentException(intent) }
        firstFailure.get()?.let { t -> throw t }

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

    fun getAllBookingsForMonth(month: Int, year: Int): MutableList<BookingDetails> {
        var allBookings: Sequence<GCalResult<Event>>
        val filteredBookings = ConcurrentHashMap.newKeySet<BookingDetails>()
        val map = ConcurrentHashMap<String, HashSet<Pair<String, String>>>()
        Accommodation.allForGettingBookings().parallelStream().forEach { accommodationIt ->
            allBookings = calendarService.getBookingsForMonth(
                accommodationIt.calendarId,
                month,
                year = year
            )
            filteredBookings.addAll(getFilteredBookings(
                allBookings,
                map,
                accommodationIt,
                MonthOrDate.Month,
                year,
                month
            ))
        }

        needsConsentIntent.get()?.let { intent -> throw NeedsConsentException(intent) }
        firstFailure.get()?.let { t -> throw t }

        return filteredBookings.sortedWith(compareBy({ it.checkIn }, { it.checkOut }))
            .map {
                it.eventIds.clear()
                it.eventIds.addAll(map[it.bookingIdOnGoogle] ?: ArrayList())
                it
            }
            .toMutableList()
    }

    fun getFilteredBookings(
        allBookings: Sequence<GCalResult<Event>>,
        map: MutableMap<String, HashSet<Pair<String, String>>>,
        accommodationIt: Accommodation,
        monthOrDate: MonthOrDate,
        year: Int,
        month: Int,
        date: Int = -1
    ): List<BookingDetails> {
        return (
            allBookings
                .onEach { r ->
                    when (r) {
                        is GCalResult.NeedsConsent -> needsConsentIntent.set(r.intent)
                        is GCalResult.Failure -> firstFailure.set(r.error)
                        else -> Unit
                    }
                }
                .filterIsInstance<GCalResult.Success<Event>>().map { it.value }
                .groupBy { it.extendedProperties.private["id"] as String }
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
                            if (date == -1) MonthView.maxDaysInThisMonth(month, year) else date,
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

    fun checkAvailability(timeMin: AppDateTime, timeMax: AppDateTime): List<Accommodation> {
        return calendarService.checkAvailability(timeMin, timeMax)
    }

    fun createBooking(bookingDetails: BookingDetails): MutableList<Pair<String, String>> {
        val returnIds = calendarService.createBooking(bookingDetails)
        renderService.notifyNewBooking(bookingDetails)
        return returnIds
    }

    fun removeBooking(bookingDetails: BookingDetails) {
        calendarService.removeBooking(bookingDetails)
        renderService.removeBooking(bookingDetails)
    }

    fun updateBooking(bookingDetails: BookingDetails) {
        calendarService.updateBooking(bookingDetails)
        renderService.notifyUpdateBooking(bookingDetails)
    }

    enum class MonthOrDate{
        Date,
        Month
    }
}