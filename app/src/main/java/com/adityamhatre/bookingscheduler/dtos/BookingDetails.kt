package com.adityamhatre.bookingscheduler.dtos

import android.text.SpannableStringBuilder
import androidx.core.text.bold
import com.adityamhatre.bookingscheduler.enums.Accommodation
import java.io.Serializable
import java.time.Instant
import java.util.*

data class BookingDetails(
    val accommodations: Set<Accommodation>,
    val checkIn: Instant,
    val checkOut: Instant,
    val bookingMainPerson: String,
    val totalNumberOfPeople: Int,
    val bookedBy: ApprovedPerson,
    val advancePaymentInfo: AdvancePayment
) : Serializable

enum class PaymentType : Serializable {
    CASH, CHEQUE, BANK_DEPOSIT, NONE;

    companion object {
        fun fromTitleCase(title: String): PaymentType {
            return values().first {
                it.name == (title.replace(" ", "_").toUpperCase(Locale.getDefault()))
            }
        }
    }
}

data class AdvancePayment(
    val advanceReceived: Boolean,
    val amount: Int = -1,
    val paymentType: PaymentType = PaymentType.NONE
) : Serializable {
    fun toSpannableString(): SpannableStringBuilder {
        if (!advanceReceived) {
            return SpannableStringBuilder().append("No advance payment received")
        }
        return SpannableStringBuilder()
            .bold { append("₹$amount ") }
            .append("received by ")
            .bold { append(paymentType.name.toTitleCase()) }
    }
}


private fun String.toTitleCase(): String {
    val converted = this[0].toUpperCase() + this.substring(1).toLowerCase(Locale.getDefault())
    return converted.split("_")
        .takeIf { it.size > 1 }?.joinToString(" ") { it.toTitleCase() }
        ?: converted
}