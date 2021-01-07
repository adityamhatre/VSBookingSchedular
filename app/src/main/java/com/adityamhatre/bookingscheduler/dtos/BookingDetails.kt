package com.adityamhatre.bookingscheduler.dtos

import android.text.SpannableStringBuilder
import androidx.core.text.bold
import com.adityamhatre.bookingscheduler.enums.Accommodation
import org.json.JSONObject
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
    val advancePaymentInfo: AdvancePayment,
    val phoneNumber: String,
    val bookingIdOnGoogle: String,
    val eventIds: ArrayList<Pair<String, String>>, //calendarId, eventId
    var notes: String
    //ADD NEW FIELDS IN DESERIALIZER
) : Serializable {
    fun toNotificationServerJson(): JSONObject = //only string to string key values allowed
        JSONObject()
            .put("bookingIdOnGoogle", bookingIdOnGoogle)
            .put("accommodations", accommodations.joinToString { it.readableName })
            .put("checkIn", com.adityamhatre.bookingscheduler.utils.Utils.toHumanDate(checkIn))
            .put("checkOut", com.adityamhatre.bookingscheduler.utils.Utils.toHumanDate(checkOut))
            .put("bookingMainPerson", bookingMainPerson)
            .put("totalNumberOfPeople", totalNumberOfPeople.toString())
            .put("bookedBy", bookedBy.readableName)
            .put("advancedPaymentReceived", advancePaymentInfo.advanceReceived.toString())
            .put("advancedPaymentType", advancePaymentInfo.paymentType.name.toTitleCase())
            .put("advancedPaymentAmount", advancePaymentInfo.amount.toString())
            .put("phoneNumber", phoneNumber)
            .put("notes", notes)

}

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