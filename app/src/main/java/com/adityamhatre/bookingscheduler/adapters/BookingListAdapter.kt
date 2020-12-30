package com.adityamhatre.bookingscheduler.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import java.time.Instant
import java.time.ZoneId
import java.util.*

class BookingListAdapter(private val bookingDetailsList: List<BookingDetails>) :
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

            (itemView.findViewById<TextView>(R.id.text1)).text = bookingDetails.bookingMainPerson

            val timing =
                "${bookingDetails.checkIn.toHumanDate()} to ${bookingDetails.checkOut.toHumanDate()}"
            (itemView.findViewById<TextView>(R.id.timing)).text = timing

            (itemView.findViewById<TextView>(R.id.accommodations)).text =
                bookingDetails.accommodations.joinToString { it.readableName }
        }
    }
}

private fun Instant.toHumanDate(): String {
    val localDateTime = Date.from(this)
        .toInstant()
        .atZone(ZoneId.systemDefault())
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
