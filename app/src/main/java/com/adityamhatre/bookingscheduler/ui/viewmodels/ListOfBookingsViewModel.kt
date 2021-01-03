package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.adapters.BookingListAdapter
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListOfBookingsViewModel : ViewModel() {
    private val bookingDetailsService = BookingsService()

    var date = -1
    var month = -1

     fun isForMonth() = date == -1 && month != -1
     fun isForDate() = date != -1 && month != -1
    fun nothingInitialized() = date == -1 && month == -1

    private fun getBookings(): List<BookingDetails> {
        if (isForDate())
            return bookingDetailsService.getAllBookingsForDate(date = date, month = month)
        if (isForMonth())
            return bookingDetailsService.getAllBookingsForMonth(month = month)
        return emptyList()
    }

    suspend fun getBookingListAdapter(): BookingListAdapter {
        return withContext(Dispatchers.IO) {
            BookingListAdapter(getBookings())
        }
    }
}