package com.adityamhatre.bookingscheduler.ui.views

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.SpannableStringBuilder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.FileProvider
import androidx.core.text.bold
import androidx.fragment.app.Fragment
import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.BuildConfig
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.adityamhatre.bookingscheduler.enums.Accommodation
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.time.Instant
import java.time.ZoneId
import java.util.*





class ViewBookingDetails(private val bookingDetails: BookingDetails) : Fragment() {
    companion object {
        fun newInstance(bookingDetails: BookingDetails) = ViewBookingDetails(bookingDetails)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val scrollView = ScrollView(inflater.context)
        scrollView.addView(inflater.inflate(R.layout.booking_details_card, container, false))

        return scrollView
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bind(view)
    }


    private fun bind(itemView: View) {
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
            bookingDetails.advancePaymentInfo.toSpannableString(receiptMode = true)

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

        (itemView.findViewById<TextView>(R.id.delete_button)).visibility = View.GONE

        (itemView.findViewById<TextView>(R.id.edit_button)).visibility = View.GONE

        (itemView.findViewById<TextView>(R.id.share_button)).visibility = View.VISIBLE
        (itemView.findViewById<TextView>(R.id.share_button)).setOnClickListener {
            (itemView.findViewById<TextView>(R.id.share_button)).visibility = View.GONE


            val totalHeight: Int = (itemView as ScrollView).getChildAt(0).height
            val totalWidth: Int = itemView.getChildAt(0).width
            val bitmap = getBitmapFromView(itemView, totalHeight, totalWidth) //viewToImage(itemView)
            (itemView.findViewById<TextView>(R.id.share_button)).visibility = View.VISIBLE
            shareBitmap(bitmap)
        }

        val trackingIdView = itemView.findViewById<TextView>(R.id.tracking_id)
        trackingIdView.visibility = View.VISIBLE
        trackingIdView.text = bookingDetails.bookingIdOnGoogle

        itemView.findViewById<ConstraintLayout>(R.id.header).visibility = View.VISIBLE
        itemView.findViewById<TextView>(R.id.footer).visibility = View.VISIBLE

    }

    private fun shareBitmap(bitmap: Bitmap) {
        val cachePath = File(Application.getInstance().externalCacheDir, "shared_receipts/")
        cachePath.mkdirs()

        //create png file
        val file = File(cachePath, "${bookingDetails.bookingIdOnGoogle}.png")
        val fileOutputStream: FileOutputStream
        try {
            fileOutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream)
            fileOutputStream.flush()
            fileOutputStream.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        //---Share File---//
        //get file uri
        val myImageFileUri: Uri = FileProvider.getUriForFile(
            Application.getApplicationContext(),
            BuildConfig.APPLICATION_ID + ".provider",
            file
        )

        //create a intent
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, myImageFileUri)
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.type = "image/png"
        startActivity(Intent.createChooser(intent, "Share with"))
    }

    private fun viewToImage(view: View): Bitmap {
        val returnedBitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(returnedBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        view.draw(canvas)
        return returnedBitmap
    }

    fun getBitmapFromView(view: View, totalHeight: Int, totalWidth: Int): Bitmap {
        val height = totalHeight.coerceAtMost(totalHeight)
        val percent = height / totalHeight.toFloat()
        val canvasBitmap = Bitmap.createBitmap(
            (totalWidth * percent).toInt(),
            (totalHeight * percent).toInt(), Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(canvasBitmap)
        val bgDrawable = view.background
        if (bgDrawable != null) bgDrawable.draw(canvas) else canvas.drawColor(Color.WHITE)
        canvas.save()
        canvas.scale(percent, percent)
        view.draw(canvas)
        canvas.restore()
        return canvasBitmap
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
