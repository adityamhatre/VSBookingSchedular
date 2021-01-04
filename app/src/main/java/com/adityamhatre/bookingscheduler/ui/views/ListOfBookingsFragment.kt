package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.AppDate
import com.adityamhatre.bookingscheduler.ui.viewmodels.ListOfBookingsViewModel
import com.nambimobile.widgets.efab.FabOption
import kotlinx.coroutines.launch

private const val DATE = "date"
private const val MONTH = "month"
private const val YEAR = "year"

class ListOfBookingsFragment : Fragment() {

    private val viewModel: ListOfBookingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            with(it) {
                val date = getInt(DATE, -1)
                val month = getInt(MONTH, -1)
                val year = getInt(YEAR, -1)
                viewModel.bookingsOn = AppDate(date, month, year)
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
        if (viewModel.bookingsOn.nothingInitialized()) {
            return
        }

//        setupRecyclerView(view)
        setupFab(view)
    }

    private fun setupFab(view: View) {
        val newBooking = view.findViewById<FabOption>(R.id.new_booking)
        if (viewModel.bookingsOn.isForMonth()) {
            newBooking.fabOptionEnabled = false
        } else if (viewModel.bookingsOn.isForDate()) {
            newBooking.setOnClickListener {
                requireActivity().supportFragmentManager
                    .beginTransaction()
                    .replace(
                        R.id.container,
                        TimeFrameInputFragment.newInstance(
                            viewModel.bookingsOn.date,
                            viewModel.bookingsOn.month,
                            year = 2021
                        )
                    )
                    .addToBackStack(null)
                    .commit()
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
        fun newInstance(date: Int = -1, month: Int, year: Int = 2021) =
            ListOfBookingsFragment().apply {
                arguments = Bundle().apply {
                    putInt(DATE, date)
                    putInt(MONTH, month)
                    putInt(YEAR, year)
                }
            }

    }
}