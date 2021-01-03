package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.ui.viewmodels.ListOfBookingsViewModel
import com.nambimobile.widgets.efab.FabOption
import kotlinx.coroutines.launch

private const val DATE = "date"
private const val MONTH = "month"

class ListOfBookingsFragment : Fragment() {

    private val viewModel by lazy { ViewModelProvider(this)[ListOfBookingsViewModel::class.java] }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            with(it) {
                viewModel.date = getInt(DATE, -1)
                viewModel.month = getInt(MONTH, -1)
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
        if (viewModel.nothingInitialized()) {
            return
        }

//        setupRecyclerView(view)
        setupFab(view)
    }

    private fun setupFab(view: View) {
        val newBooking = view.findViewById<FabOption>(R.id.new_booking)
        if (viewModel.isForMonth()) {
            newBooking.fabOptionEnabled = false
        } else if (viewModel.isForDate()) {
            newBooking.setOnClickListener {
                TimeFrameInputDialog(viewModel.date, viewModel.month, year=2021).show(activity?.supportFragmentManager!!, null)
            }
        }

    }


    private fun setupRecyclerView(view: View) {
        val bookingRecyclerView = view.findViewById<RecyclerView>(R.id.booking_list)
        viewLifecycleOwner.lifecycleScope.launch {
            bookingRecyclerView.adapter = viewModel.getBookingListAdapter()
        }

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