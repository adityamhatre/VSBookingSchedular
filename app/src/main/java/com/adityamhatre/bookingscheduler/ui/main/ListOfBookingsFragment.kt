package com.adityamhatre.bookingscheduler.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.adityamhatre.bookingscheduler.MainActivity
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.adapters.BookingListAdapter
import com.adityamhatre.bookingscheduler.dtos.BookingDetails

private const val DATE = "date"
private const val MONTH = "month"

class ListOfBookingsFragment : Fragment() {
    private var date: Int = -1
    private var month: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            with(it) {
                date = getInt(DATE, -1)
                month = getInt(MONTH, -1)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_list_of_bookings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (date == -1 && month == -1) {
            return
        }

        /*val bookingDetailsList =
            (activity as MainActivity).bookingDetailsService.getBookingDetailsFor(date, month)*/
        val bookingDetailsList =
            (activity as MainActivity).bookingDetailsService.getAllBookings()

        isAnyAccommodationAvailable(bookingDetailsList)
        setupRecyclerView(view, bookingDetailsList)
    }

    private fun isAnyAccommodationAvailable(bookingDetailsList: List<BookingDetails>) {

    }

    private fun setupRecyclerView(view: View, bookingDetailsList: List<BookingDetails>) {
        val bookingRecyclerView = view.findViewById<RecyclerView>(R.id.booking_list)
        bookingRecyclerView.adapter = BookingListAdapter(bookingDetailsList)
    }

    companion object {
        @JvmStatic
        fun newInstance(date: Int = -1, month: Int) =
            ListOfBookingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(DATE, date)
                    putInt(MONTH, month)
                }
            }

    }
}