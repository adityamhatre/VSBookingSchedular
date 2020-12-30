package com.adityamhatre.bookingscheduler.dtos

import com.adityamhatre.bookingscheduler.enums.Accommodation
import java.time.Instant

data class BookingDetails(
    val accommodations: List<Accommodation>,
    val checkIn: Instant,
    val checkOut: Instant,
    val bookingMainPerson: String,
    val totalNumberOfPeople: Int,
    val bookedBy: ApprovedPerson,
    val advancePaymentInfo: AdvancePayment
)

enum class PaymentType {
    CASH, CHEQUE, BANK_DEPOSIT, NONE
}

data class AdvancePayment(
    val advanceReceived: Boolean,
    val amount: Int = -1,
    val paymentType: PaymentType = PaymentType.NONE
)