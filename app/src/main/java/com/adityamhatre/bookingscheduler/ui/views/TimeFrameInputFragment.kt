package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.ui.viewmodels.TimeFrameInputDialogViewModel
import com.adityamhatre.bookingscheduler.utils.TwoDigitFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

private const val DATE = "date"
private const val MONTH = "month"
private const val YEAR = "year"

class TimeFrameInputFragment : Fragment() {
    private val viewModel: TimeFrameInputDialogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            with(it) {
                val date = getInt(DATE, -1)
                val month = getInt(MONTH, -1)
                val year = getInt(YEAR, -1)
                viewModel.checkInDateTime = AppDateTime(
                    date,
                    month,
                    year,
                    if (viewModel.checkInDateTime.hour == -1) 9 else viewModel.checkInDateTime.hour,
                    if (viewModel.checkInDateTime.minute == -1) 30 else viewModel.checkInDateTime.minute
                )
                viewModel.checkOutDateTime = AppDateTime(
                    if (viewModel.checkOutDateTime.date == -1) date else viewModel.checkOutDateTime.date,
                    if (viewModel.checkOutDateTime.month == -1) month else viewModel.checkOutDateTime.month,
                    if (viewModel.checkOutDateTime.year == -1) year else viewModel.checkOutDateTime.year,
                    if (viewModel.checkOutDateTime.hour == -1) 17 else viewModel.checkOutDateTime.hour,
                    if (viewModel.checkOutDateTime.minute == -1) 0 else viewModel.checkOutDateTime.minute
                )
            }
        }


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_time_frame_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
    }


    private fun setupView(view: View) {
        setupCheckInDate(view)
        setupCheckInTime(view)
        setupCheckOutDateSpinner(view)
        setupCheckOutTime(view)
        setupCheckAvailabilityButton(view)
        setupNext(view)
        setupObservers(view)
    }

    private fun setupNext(view: View) {
        view.findViewById<Button>(R.id.next).setOnClickListener {
            if (!viewModel.isValid()) {
                Toast.makeText(requireContext(), "Some data is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(
                    R.id.container,
                    NewBookingDetailsFragment.newInstance(
                        viewModel.checkInDateTime,
                        viewModel.checkOutDateTime,
                        viewModel.getSelectedAccommodations().value!!
                    )
                )
                .addToBackStack(null)
                .commit()
        }
    }

    private fun setupObservers(view: View) {
        viewModel.getSelectedAccommodations().observe(viewLifecycleOwner, {
            view.findViewById<Button>(R.id.next).isEnabled = it.isNotEmpty()
        })
    }

    private fun setupCheckInTime(view: View) {
        val checkInTime = view.findViewById<RadioGroup>(R.id.check_in_time)

        checkInTime.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id._9_30am) {
                viewModel.checkInDateTime.hour = 9
            }
            if (checkedId == R.id._5_30pm) {
                viewModel.checkInDateTime.hour = 17
            }
        }
    }

    private fun setupCheckAvailabilityButton(view: View) {
        view.findViewById<Button>(R.id.check_availability).setOnClickListener { btn ->
            if (viewModel.checkOutDateTime < viewModel.checkInDateTime) {
                Toast.makeText(
                    requireContext(),
                    "Check out cannot before check in",
                    Toast.LENGTH_LONG
                ).show()
                return@setOnClickListener
            }
            btn.isEnabled = false
            view.findViewById<ProgressBar>(R.id.loading_icon).visibility = View.VISIBLE
            viewLifecycleOwner.lifecycleScope.launch {
                val accommodationListLayout1 =
                    view.findViewById<LinearLayout>(R.id.accommodation_list1)
                accommodationListLayout1.removeAllViews()
                val accommodationListLayout2 =
                    view.findViewById<LinearLayout>(R.id.accommodation_list2)
                accommodationListLayout2.removeAllViews()
                viewModel.clearAccommodations()

                val availableAccommodations = viewModel.checkAvailability(
                    viewModel.checkInDateTime,
                    viewModel.checkOutDateTime
                ).toSet()

                Accommodation.all().forEachIndexed { i, it ->
                    val checkBox = CheckBox(requireContext())
                    checkBox.text = it.readableName
                    checkBox.isEnabled = availableAccommodations.contains(it)
                    checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        val accommodation = Accommodation.byReadableName(buttonView.text.toString())
                        if (isChecked) {
                            viewModel.addAccommodation(accommodation)
                        } else {
                            viewModel.removeAccommodation(accommodation)
                        }
                    }
                    if (i < Accommodation.all().size / 2) {
                        accommodationListLayout1.addView(checkBox)
                    } else {
                        accommodationListLayout2.addView(checkBox)
                    }
                }

            }.invokeOnCompletion {
                view.findViewById<ProgressBar>(R.id.loading_icon).visibility = View.GONE
                btn.isEnabled = true
            }

        }
    }

    private fun setupCheckOutTime(view: View) {
        val checkOutTime = view.findViewById<RadioGroup>(R.id.check_out_time)

        checkOutTime.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id._9am) {
                viewModel.checkOutDateTime.hour = 9
            }
            if (checkedId == R.id._5pm) {
                viewModel.checkOutDateTime.hour = 17
            }
        }
    }

    private fun setupCheckOutDateSpinner(view: View) {
        val checkOutDate = view.findViewById<DatePicker>(R.id.check_out_date_picker)

        with(viewModel.checkOutDateTime) {
            checkOutDate.updateDate(year, month - 1, date)
        }
        checkOutDate.minDate = 0
        checkOutDate.minDate = LocalDate.of(
            viewModel.checkInDateTime.year,
            viewModel.checkInDateTime.month,
            viewModel.checkInDateTime.date
        ).atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond() * 1000 // for milliseconds

        checkOutDate.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            viewModel.checkOutDateTime.year = year
            viewModel.checkOutDateTime.month = monthOfYear + 1
            viewModel.checkOutDateTime.date = dayOfMonth
        }
    }

    private fun setupCheckInDate(view: View) {
        val checkInDate = view.findViewById<TextView>(R.id.check_in_date)
        val checkInDateText =
            "Check in on ${TwoDigitFormatter.toTwoDigits(viewModel.checkInDateTime.date)}/${
                TwoDigitFormatter.toTwoDigits(viewModel.checkInDateTime.month)
            }/${viewModel.checkInDateTime.year}\nSelect check out date and time"
        checkInDate.text = checkInDateText
    }

    companion object {
        fun newInstance(date: Int, month: Int, year: Int) = TimeFrameInputFragment().apply {
            arguments = Bundle().apply {
                putInt(DATE, date)
                putInt(MONTH, month)
                putInt(YEAR, year)
            }
        }
    }
}
