package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.enums.Accommodation

private const val MONTH = "month"
private const val DATE = "date"

class AccommodationList : Fragment() {
    private var month = -1
    private var date = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            with(it) {
                month = getInt(MONTH, -1)
                date = getInt(DATE, -1)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_accommodation_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isForBooking()) {
            Toast.makeText(activity, "For booking on $month $date", Toast.LENGTH_SHORT).show()
        }
        val accommodationList = view.findViewById<LinearLayout>(R.id.accommodation_list)

        Accommodation.values().forEach { accommodation ->
            val accommodationTextView =
                (LayoutInflater.from(activity).inflate(R.layout.text_view, null)) as TextView
            accommodationTextView.text = accommodation.readableName
            accommodationTextView.setOnClickListener { println(accommodation) }

            accommodationList.addView(accommodationTextView)
        }

    }

    private fun isForBooking() = month != -1 && date != -1

    companion object {
        @JvmStatic
        fun newInstance(month: Int, date: Int) = AccommodationList().apply {
            arguments = Bundle().apply {
                putInt(MONTH, month)
                putInt(DATE, date)
            }
        }
    }
}