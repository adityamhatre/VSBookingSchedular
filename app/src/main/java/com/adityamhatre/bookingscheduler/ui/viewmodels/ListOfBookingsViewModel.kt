package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.adapters.BookingListAdapter
import com.adityamhatre.bookingscheduler.dtos.AppDate
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

class ListOfBookingsViewModel : ViewModel() {
    private val bookingDetailsService = BookingsService()
    private val bookingsCount: MutableLiveData<Int> = MutableLiveData(0)
    fun getBookingsCount(): LiveData<Int> = bookingsCount

    var bookingsOn = AppDate(-1, -1, -1)

    private fun getBookings(): MutableList<BookingDetails> {
        if (bookingsOn.isForDate())
            return bookingDetailsService.getAllBookingsForDate(
                date = bookingsOn.date,
                month = bookingsOn.month,
                year = 2021
            )
        if (bookingsOn.isForMonth())
            return bookingDetailsService.getAllBookingsForMonth(month = bookingsOn.month)
        return emptyList<BookingDetails>() as LinkedList<BookingDetails>
    }

    suspend fun getBookingListAdapter(confirmDelete: (position: Int, onConfirm: suspend () -> Unit) -> Unit): BookingListAdapter {
        return withContext(Dispatchers.IO) {
            val bookings = getBookings()
            bookingsCount.postValue(bookings.size)
            BookingListAdapter(
                bookings,
                onItemClicked = { _, item -> println(item) },
                onItemDeleted = { i, _ ->
                    confirmDelete(i) {
                        withContext(Dispatchers.IO) {
                            bookingDetailsService.removeBooking(bookings.removeAt(i))
                        }

                        bookingsCount.value = bookings.size
                    }
                })
        }
    }
}