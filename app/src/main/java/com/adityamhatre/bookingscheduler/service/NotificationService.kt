package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class NotificationService {
    private val queue = Volley.newRequestQueue(Application.getApplicationContext())
    fun notifyNewBooking(bookingDetails: BookingDetails) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "https://vs-booking-scheduler-push-serv.herokuapp.com/notifications/newBookingCreated",
            bookingDetails.toNotificationServerJson(),
            {},
            {}
        )

        queue.add(request)
    }

    fun notifyUpdateBooking(bookingDetails: BookingDetails) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "https://vs-booking-scheduler-push-serv.herokuapp.com/notifications/updatedBooking",
            bookingDetails.toNotificationServerJson(),
            {},
            {}
        )

        queue.add(request)
    }

}
