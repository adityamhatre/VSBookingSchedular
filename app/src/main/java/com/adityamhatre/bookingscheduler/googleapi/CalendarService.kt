package com.adityamhatre.bookingscheduler.googleapi

import android.accounts.Account
import android.content.Context
import android.util.Log
import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.utils.TimeStampConverter
import com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.*
import com.google.firebase.crashlytics.FirebaseCrashlytics
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*


class CalendarService(context: Context, account: Account) {
    private val gson = Application.getInstance().gson
    private val credential = GoogleAccountCredential.usingOAuth2(
        context,
        listOf(CalendarScopes.CALENDAR)
    ).setSelectedAccount(account)

    private val calendarClient = Calendar.Builder(
        newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(),
        credential
    ).setApplicationName(Application.getApplicationContext().getString(R.string.app_name)).build()

    fun getBookingsForMonth(
        calendarId: String,
        month: Int,
        year: Int
    ) = getBookingsForDate(calendarId, 1, month, year)

    fun getBookingsForDate(
        calendarId: String,
        date: Int,
        month: Int,
        year: Int
    ): Sequence<Event> {
        return sequence {
            var pageToken: String? = null
            do {
                val events: Events =
                    calendarClient.events()
                        .list(calendarId)
                        .setTimeMin(
                            DateTime(
                                Date.from(
                                    LocalDateTime.of(year, month, date, 9, 30).toInstant(
                                        ZoneOffset.ofHoursMinutes(5, 30)
                                    )
                                )
                            )
                        )
                        .setTimeZone("UTC+05:30")
                        .setPageToken(pageToken)
                        .execute()

                val items: List<Event> = events.items
                for (event in items) {
                    yield(event)
                }
                pageToken = events.nextPageToken
            } while (pageToken != null)
        }
    }

    fun checkAvailability(
        timeMin: AppDateTime,
        timeMax: AppDateTime
    ): List<Accommodation> {
        return calendarClient.freebusy()
            .query(
                FreeBusyRequest()
                    .setItems(
                        Accommodation.all().map { FreeBusyRequestItem().setId(it.calendarId) })
                    .setTimeMin(DateTime(with(timeMin) {
                        TimeStampConverter.convertToTimestampString(
                            year, month, date, hour, minute
                        )
                    }))
                    .setTimeMax(DateTime(with(timeMax) {
                        TimeStampConverter.convertToTimestampString(
                            year, month, date, hour, minute
                        )
                    }))
                    .setTimeZone("UTC+05:30")
            )
            .execute()
            .calendars
            .filterValues { value -> value.busy.isEmpty() }
            .map { (key, _) -> Accommodation.from(key) }
    }

    fun createBooking(bookingDetails: BookingDetails): MutableList<Pair<String, String>> {
        val returnIds = mutableListOf<Pair<String,String>>()
        bookingDetails.accommodations.parallelStream().forEach {
            try {
                val executeResult = calendarClient.events().insert(
                    it.calendarId, Event()
                        .setSummary(bookingDetails.bookingMainPerson)
                        .setExtendedProperties(
                            Event.ExtendedProperties()
                                .setPrivate(mapOf("id" to bookingDetails.bookingIdOnGoogle))
                        )
                        .setDescription(gson.toJson(bookingDetails))
                        .setStart(
                            EventDateTime().setDateTime(DateTime(Date.from(bookingDetails.checkIn)))
                        ).setEnd(
                            EventDateTime().setDateTime(DateTime(Date.from(bookingDetails.checkOut)))
                        )
                ).execute()
                returnIds.add(Pair(it.calendarId, executeResult.id))
            } catch (gjre: GoogleJsonResponseException) {
                Log.e("create booking", gjre.details.message)
                FirebaseCrashlytics.getInstance().recordException(gjre)
            }
        }
        return returnIds

    }

    fun removeBooking(bookingDetails: BookingDetails) {
        bookingDetails.eventIds.parallelStream().forEach {
            try {
                calendarClient.events().delete(it.first, it.second)
                    .setSendNotifications(false)
                    .execute()
            } catch (gjre: GoogleJsonResponseException) {
                Log.e("remove booking", gjre.details.message)
                FirebaseCrashlytics.getInstance().recordException(gjre)
            }
        }
    }

    fun updateBooking(bookingDetails: BookingDetails) {
        bookingDetails.eventIds.parallelStream().forEach {
            try {
                calendarClient.events().patch(
                    it.first, it.second, Event()
                        .setSummary(bookingDetails.bookingMainPerson)
                        .setDescription(gson.toJson(bookingDetails))
                ).execute()
            } catch (gjre: GoogleJsonResponseException) {
                Log.e("update booking", gjre.details.message)
                FirebaseCrashlytics.getInstance().recordException(gjre)
            }

        }
    }
}