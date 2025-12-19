package com.adityamhatre.bookingscheduler.ui.viewmodels

import android.content.Context
import android.widget.ArrayAdapter
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.dtos.PaymentType
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.UUID

class NewBookingDetailsViewModel : ViewModel() {
    private val bookingsService = BookingsService()

    lateinit var checkInDateTime: AppDateTime
    lateinit var checkOutDateTime: AppDateTime
    lateinit var accommodationSet: Set<Accommodation>

    var bookingFor: String = ""
    var numberOfPeople: Int = -1
    var paymentType = PaymentType.CASH
    var advancePaymentAmount: Int = -1
    var advancePaymentRequired: Boolean = true
    var phoneNumber = ""
    var notes = ""
    var bookingIdOnGoogle = UUID.randomUUID().toString()

    private val isDataValid: MutableLiveData<Boolean> = MutableLiveData(false)
    fun validate() {
        isDataValid.value = checkInDateTime.isValid()
                && checkOutDateTime.isValid()
                && accommodationSet.isNotEmpty()
                && bookingFor.isNotBlank()
                && phoneNumber.isNotBlank()
                && numberOfPeople > 0

        if (advancePaymentRequired) {
            isDataValid.value = isDataValid.value!! && advancePaymentAmount > 0
        }

    }

    fun isValid(): LiveData<Boolean> {
        return isDataValid
    }

    private val paymentTypeItems =
        PaymentType.values().filter { it != PaymentType.NONE }.map { it.name.toTitleCase() }

    fun getPaymentTypeAdapter(context: Context): ArrayAdapter<String> {
        return ArrayAdapter(context, R.layout.list_item, paymentTypeItems)
    }

    suspend fun createBooking(bookingDetails: BookingDetails): MutableList<Pair<String, String>> {
        return withContext(Dispatchers.IO) {
            return@withContext bookingsService.createBooking(bookingDetails)
        }
    }

    suspend fun updateBooking(bookingDetails: BookingDetails) {
        withContext(Dispatchers.IO) {
            bookingsService.updateBooking(bookingDetails)
        }
    }

    fun fillValues(originalBookingDetails: BookingDetails) {
        with(originalBookingDetails) {
            bookingFor = bookingMainPerson
            numberOfPeople = totalNumberOfPeople
            paymentType = advancePaymentInfo.paymentType
            advancePaymentAmount = advancePaymentInfo.amount
            advancePaymentRequired = advancePaymentInfo.advanceReceived
        }
        bookingIdOnGoogle = originalBookingDetails.bookingIdOnGoogle
        notes = originalBookingDetails.notes
        phoneNumber = originalBookingDetails.phoneNumber
    }

}

private fun String.toTitleCase(): String {
    val converted = this[0].uppercaseChar() + this.substring(1).lowercase(Locale.getDefault())
    return converted.split("_")
        .takeIf { it.size > 1 }?.joinToString(" ") { it.toTitleCase() }
        ?: converted
}