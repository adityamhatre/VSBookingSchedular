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
    private val bookingsCount: MutableLiveData<Int> = MutableLiveData(-1)
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

    suspend fun getBookingListAdapter(
        onItemEditClicked: (item: BookingDetails, notifyDataChangedFnc: () -> Unit) -> Unit,
        confirmDelete: (position: Int, onConfirm: suspend () -> Unit) -> Unit
    ): BookingListAdapter {
        return withContext(Dispatchers.IO) {
            val bookings = getBookings()
            bookingsCount.postValue(bookings.size)
            BookingListAdapter(
                bookings,
                onItemEdited = { item, afterItemEdit -> onItemEditClicked(item, afterItemEdit) },
                onItemDeleted = { i, _ ->
                    confirmDelete(i) {
                        withContext(Dispatchers.IO) {
                            bookingsCount.postValue(bookings.size - 1)
                            bookingDetailsService.removeBooking(bookings.removeAt(i))
                        }
                    }
                })
        }
    }

    fun setBookingsCount(i: Int) {
        bookingsCount.value = i
    }
}