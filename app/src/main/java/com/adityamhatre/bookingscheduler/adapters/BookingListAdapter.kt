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
import java.util.Date
import java.util.Locale

class BookingListAdapter(
    private val bookingDetailsList: MutableList<BookingDetails>,
    private val onItemEdited: (Int, BookingDetails, BookingListAdapter) -> Unit,
    private val onItemDeleted: (Int, BookingDetails) -> Unit,
    private val onClick: (BookingDetails) -> Unit
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

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            for (payload in payloads) {
                val map = payload as Map<*, *>
                bookingDetailsList[position].notes = map["notes"].toString()
                holder.bind(position)
            }
        } else {
            super.onBindViewHolder(holder, position, payloads)
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(position)
    fun setItem(position: Int, bookingDetails: BookingDetails) {
        bookingDetailsList[position] = bookingDetails
        notifyItemChanged(position)
    }

    fun addItem(bookingDetails: BookingDetails) {
        bookingDetailsList.add(bookingDetails)
        notifyItemInserted(bookingDetailsList.size - 1)
    }

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
                            else -> Accommodation.bungalow51List(bookingDetails.accommodations)
                                .joinToString { it.readableName }
                        })
            (itemView.findViewById<TextView>(R.id.accommodations)).text = accommodations

            (itemView.findViewById<TextView>(R.id.advance_payment_info)).text =
                bookingDetails.advancePaymentInfo.toSpannableString()

            val notesView = (itemView.findViewById<TextView>(R.id.notes))
            if (bookingDetails.notes.isNotEmpty()) {
                val notes = SpannableStringBuilder()
                    .bold { append("Notes:  ") }
                    .append(bookingDetails.notes)
                notesView.text = notes
            } else {
                notesView.visibility = View.GONE
            }

            val bookedBy = SpannableStringBuilder()
                .bold { append("Booked by ") }
                .append(bookingDetails.bookedBy.readableName)

            (itemView.findViewById<TextView>(R.id.booked_by)).text = bookedBy

            (itemView.findViewById<TextView>(R.id.delete_button)).setOnClickListener {
                onItemDeleted(adapterPosition, bookingDetails)
            }

            (itemView.findViewById<TextView>(R.id.edit_button)).setOnClickListener {
                onItemEdited(adapterPosition, bookingDetails, this@BookingListAdapter)
            }

            itemView.setOnClickListener { onClick(bookingDetails) }
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
    return this[0].uppercaseChar() + this.substring(1).lowercase(Locale.getDefault())
}
