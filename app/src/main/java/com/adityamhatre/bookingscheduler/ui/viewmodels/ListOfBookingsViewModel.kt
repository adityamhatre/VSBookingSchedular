package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.adapters.BookingListAdapter
import com.adityamhatre.bookingscheduler.dtos.AppDate
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListOfBookingsViewModel : ViewModel() {
    private val bookingDetailsService = BookingsService()

    var bookingsOn = AppDate(-1, -1, -1)

    private fun getBookings(): List<BookingDetails> {
        if (bookingsOn.isForDate())
            return bookingDetailsService.getAllBookingsForDate(
                date = bookingsOn.date,
                month = bookingsOn.month
            )
        if (bookingsOn.isForMonth())
            return bookingDetailsService.getAllBookingsForMonth(month = bookingsOn.month)
        return emptyList()
    }

    suspend fun getBookingListAdapter(): BookingListAdapter {
        return withContext(Dispatchers.IO) {
            BookingListAdapter(getBookings())
        }
    }
}