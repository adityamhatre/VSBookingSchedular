package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.adapters.BookingListAdapter
import com.adityamhatre.bookingscheduler.dtos.AdapterContainer
import com.adityamhatre.bookingscheduler.dtos.AppDate
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.exceptions.NeedsConsentException
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ListOfBookingsViewModel : ViewModel() {
    private val bookingDetailsService = BookingsService()
    private val bookingsCount: MutableLiveData<Int> = MutableLiveData(-1)
    var bookingsList: MutableList<BookingDetails>? = null

    private val needsConsentIntent = MutableLiveData<android.content.Intent>()
    fun getNeedsConsentIntent(): LiveData<android.content.Intent> = needsConsentIntent

    private val error = MutableLiveData<Throwable>()
    fun getError(): LiveData<Throwable> = error

    var adapterContainer: AdapterContainer<BookingListAdapter> = AdapterContainer()

    fun getBookingsCount(): LiveData<Int> = bookingsCount

    var bookingsOn = AppDate(-1, -1, -1)

    private fun getBookings(): MutableList<BookingDetails> {
        return try {
            if (bookingsOn.isForDate())
                bookingDetailsService.getAllBookingsForDate(
                    date = bookingsOn.date,
                    month = bookingsOn.month,
                    year = bookingsOn.year
                )
            else if (bookingsOn.isForMonth())
                bookingDetailsService.getAllBookingsForMonth(
                    month = bookingsOn.month,
                    year = bookingsOn.year
                )
            else
                mutableListOf()
        } catch (e: NeedsConsentException) {
            needsConsentIntent.postValue(e.intent)
            mutableListOf()
        }
    }

    suspend fun getBookingListAdapter(
        afterUpdateComplete: (position: Int, updatedBookingDetails: BookingDetails, adapter: BookingListAdapter) -> Unit,
        confirmDelete: (position: Int, onConfirm: suspend () -> Unit) -> Unit,
        onClick: (BookingDetails) -> Unit
    ): BookingListAdapter {
        return withContext(Dispatchers.IO) {
            this@ListOfBookingsViewModel.bookingsList =
                this@ListOfBookingsViewModel.bookingsList ?: getBookings()
            val bookings = this@ListOfBookingsViewModel.bookingsList!!

            bookingsCount.postValue(bookings.size)

            this@ListOfBookingsViewModel.adapterContainer.setAdapter(
                this@ListOfBookingsViewModel.adapterContainer.getAdapter() ?: BookingListAdapter(
                    bookings,
                    onItemEdited = { i, item, adapter -> afterUpdateComplete(i, item, adapter) },
                    onItemDeleted = { i, _ ->
                        confirmDelete(i) {
                            withContext(Dispatchers.IO) {
                                bookingsCount.postValue(bookings.size - 1)
                                bookingDetailsService.removeBooking(bookings.removeAt(i))
                            }
                        }
                    },
                    onClick = onClick
                )
            )

            val adapter = this@ListOfBookingsViewModel.adapterContainer.getAdapter()!!

            return@withContext adapter
        }
    }

    fun setBookingsCount(i: Int) {
        bookingsCount.value = i
    }
}