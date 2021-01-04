package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewBookingDetailsViewModel : ViewModel() {
    suspend fun createBooking(bookingDetails: BookingDetails) {
        withContext(Dispatchers.IO) {
            bookingsService.createBooking(bookingDetails)
        }
    }

    private val bookingsService = BookingsService()

}
