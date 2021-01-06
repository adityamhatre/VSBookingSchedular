package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.utils.Utils
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class NotificationService {
    val queue = Volley.newRequestQueue(Application.getApplicationContext())
    fun notifyNewBooking(bookingDetails: BookingDetails) {
        val data = mapOf(
            "checkIn" to Utils.toHumanDate(bookingDetails.checkIn),
            "checkOut" to Utils.toHumanDate(bookingDetails.checkOut),
            "bookingFor" to bookingDetails.bookingMainPerson,
        )
        val jsonData = Application.getInstance().gson.toJson(data)

        val request = JsonObjectRequest(
            Request.Method.POST,
            "https://vs-booking-scheduler-push-serv.herokuapp.com/notifications/newBookings",
            JSONObject(jsonData),
            {},
            {}
        )

        queue.add(request)
    }

}
