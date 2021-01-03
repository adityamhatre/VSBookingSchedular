package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.ui.viewmodels.TimeFrameInputDialogViewModel
import com.adityamhatre.bookingscheduler.utils.TwoDigitFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

private const val DATE = "date"
private const val MONTH = "month"
private const val YEAR = "year"

class TimeFrameInput : Fragment() {

    private val viewModel: TimeFrameInputDialogViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            with(it) {
                val date = getInt(DATE, -1)
                val month = getInt(MONTH, -1)
                val year = getInt(YEAR, -1)
                viewModel.checkInDateTime = AppDateTime(date, month, year, 0, 0)
                viewModel.checkOutDateTime = AppDateTime(date, month, year, 9, 0)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.time_frame_input, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupView(view)
    }


    private fun setupView(view: View) {
        setupCheckInDate(view)
        setupCheckOutDateSpinner(view)
        setupCheckOutTime(view)
        setupCheckAvailabilityButton(view)
    }

    private fun setupCheckAvailabilityButton(view: View) {
        view.findViewById<Button>(R.id.check_availability).setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.checkAvailability(
                    viewModel.checkInDateTime,
                    viewModel.checkOutDateTime
                ).forEach { println(it.readableName) }
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
        fun newInstance(date: Int, month: Int, year: Int) = TimeFrameInput().apply {
            arguments = Bundle().apply {
                putInt(DATE, date)
                putInt(MONTH, month)
                putInt(YEAR, year)
            }
        }
    }
}
