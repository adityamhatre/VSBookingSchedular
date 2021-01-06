package com.adityamhatre.bookingscheduler.adapters

import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.text.bold
import androidx.recyclerview.widget.RecyclerView
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.enums.Accommodation
import java.time.Instant
import java.time.ZoneId
import java.util.*

class BookingListAdapter(
    private val bookingDetailsList: List<BookingDetails>,
    private val onItemEdited: (BookingDetails, afterItemEdit: () -> Unit) -> Unit,
    private val onItemDeleted: (Int, BookingDetails) -> Unit
) :
    RecyclerView.Adapter<BookingListAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.booking_details_card, parent, false)
        )
    }

    override fun getItemCount() = bookingDetailsList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(position: Int) {
            val bookingDetails = bookingDetailsList[position]

            val title =
                "${bookingDetails.bookingMainPerson} (${bookingDetails.totalNumberOfPeople} people)"
            (itemView.findViewById<TextView>(R.id.title)).text = title

            val phoneNumberView = (itemView.findViewById<TextView>(R.id.phone_number))
            if (bookingDetails.phoneNumber.isNotEmpty()) {
                val phoneNumber = SpannableStringBuilder()
                    .bold { append("Phone:  ") }
                    .append(bookingDetails.phoneNumber)
                phoneNumberView.text = phoneNumber
            } else {
                phoneNumberView.visibility = View.GONE
            }

            val checkInTiming = SpannableStringBuilder()
                .bold { append("Check in:  ") }
                .append(bookingDetails.checkIn.toHumanDate())
            val checkOutTiming = SpannableStringBuilder()
                .bold { append("Check out: ") }
                .append(bookingDetails.checkOut.toHumanDate())
            (itemView.findViewById<TextView>(R.id.check_in_timing)).text = checkInTiming
            (itemView.findViewById<TextView>(R.id.check_out_timing)).text = checkOutTiming

            val accommodations =
                SpannableStringBuilder()
                    .bold { append("Accommodations: ") }
                    .append(
                        when {
                            Accommodation.isWholeResort(bookingDetails.accommodations) -> "Whole Resort"
                            Accommodation.isBungalowAndRooms(bookingDetails.accommodations) -> "Bungalow (5 + 1) + All other accommodations"
                            else -> Accommodation.bungalow51List(bookingDetails.accommodations)
                                .joinToString { it.readableName }
                        })
            (itemView.findViewById<TextView>(R.id.accommodations)).text = accommodations

            (itemView.findViewById<TextView>(R.id.advance_payment_info)).text =
                bookingDetails.advancePaymentInfo.toSpannableString()

            val bookedBy = SpannableStringBuilder()
                .bold { append("Booked by ") }
                .append(bookingDetails.bookedBy.readableName)

            (itemView.findViewById<TextView>(R.id.booked_by)).text = bookedBy

            (itemView.findViewById<TextView>(R.id.delete_button)).setOnClickListener {
                onItemDeleted(adapterPosition, bookingDetails)
            }

            (itemView.findViewById<TextView>(R.id.edit_button)).setOnClickListener {
                onItemEdited(bookingDetails) { notifyItemChanged(adapterPosition) }
            }
        }
    }
}

private fun Instant.toHumanDate(): String {
    val localDateTime = Date.from(this)
        .toInstant()
        .atZone(ZoneId.of(ZoneId.SHORT_IDS["IST"]))
        .toLocalDateTime()

    val day =
        if (localDateTime.dayOfMonth > 9) "${localDateTime.dayOfMonth}" else "0${localDateTime.dayOfMonth}"
    val month = localDateTime.month.name.toTitleCase()
    val year = localDateTime.year

    val hourValue =
        if (localDateTime.hour == 0) 12 else if (localDateTime.hour > 12) localDateTime.hour - 12 else localDateTime.hour
    val hour = if (hourValue > 9) "$hourValue" else "0${hourValue}"
    val minute =
        if (localDateTime.minute > 9) "${localDateTime.minute}" else "0${localDateTime.minute}"
    val amPm = if (localDateTime.hour >= 12) "PM" else "AM"

    return "$day $month $year, $hour:$minute $amPm"
}

private fun String.toTitleCase(): String {
    return this[0].toUpperCase() + this.substring(1).toLowerCase(Locale.getDefault())
}
