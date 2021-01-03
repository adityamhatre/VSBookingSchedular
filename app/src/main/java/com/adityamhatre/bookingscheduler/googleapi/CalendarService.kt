package com.adityamhatre.bookingscheduler.googleapi

import android.accounts.Account
import android.content.Context
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.google.api.client.extensions.android.http.AndroidHttp.newCompatibleTransport
import com.google.api.client.extensions.android.json.AndroidJsonFactory
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.util.DateTime
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.api.services.calendar.model.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*

class CalendarService(context: Context, account: Account) {
    private val credential = GoogleAccountCredential.usingOAuth2(
        context,
        listOf(CalendarScopes.CALENDAR)
    ).setSelectedAccount(account)

    private val calendarClient = Calendar.Builder(
        newCompatibleTransport(), AndroidJsonFactory.getDefaultInstance(),
        credential
    ).build()

    fun getCalendarWithId(id: String): com.google.api.services.calendar.model.Calendar {
        return calendarClient.Calendars().get(id).execute()
    }

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

    fun addBookingForDate(
        calendarId: String,
        date: Int,
        month: Int,
        year: Int,
        bookingFor: String
    ) {
        calendarClient.events().insert(
            calendarId, Event()
                .setSummary(bookingFor)
                .setDescription("{details: {}}")
                .setStart(
                    EventDateTime().setDateTime(
                        DateTime(
                            Date.from(
                                LocalDateTime.of(year, month, date, 9, 30).toInstant(
                                    ZoneOffset.ofHoursMinutes(5, 30)
                                )
                            )
                        )
                    )
                ).setEnd(
                    EventDateTime().setDateTime(
                        DateTime(
                            Date.from(
                                LocalDateTime.of(year, month, date, 12 + 9, 0).toInstant(
                                    ZoneOffset.ofHoursMinutes(5, 30)
                                )
                            )
                        )
                    )
                )
        ).execute()
    }

    fun checkAvailability(timeMin: String, timeMax: String): List<Accommodation> {
        return calendarClient.freebusy()
            .query(
                FreeBusyRequest()
                    .setItems(
                        Accommodation.all().map { FreeBusyRequestItem().setId(it.calendarId) })
                    .setTimeMin(DateTime(timeMin))
                    .setTimeMax(DateTime(timeMax))
                    .setTimeZone("UTC+05:30")
            )
            .execute()
            .calendars
            .filterValues { value -> value.busy.isEmpty() }
            .map { (key,_)-> Accommodation.from(key) }
    }
}