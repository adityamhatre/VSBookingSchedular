package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.DatePicker
import android.widget.RadioGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.ui.viewmodels.TimeFrameInputDialogViewModel
import com.adityamhatre.bookingscheduler.utils.TwoDigitFormatter
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

class TimeFrameInputDialog(val date: Int, val month: Int, val year: Int) : DialogFragment() {
    private val viewModel by lazy { ViewModelProvider(this)[TimeFrameInputDialogViewModel::class.java] }

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
        val checkInDate = view.findViewById<TextView>(R.id.check_in_date)
        val checkInDateText = "Check in on ${TwoDigitFormatter.toTwoDigits(date)}/${
            TwoDigitFormatter.toTwoDigits(month)
        }/$year\nSelect check out date and time"
        checkInDate.text = checkInDateText


        val checkOutDate = view.findViewById<DatePicker>(R.id.check_out_date_picker)

        checkOutDate.minDate = 0
        checkOutDate.minDate = LocalDate.of(year, month, date).atStartOfDay(ZoneId.systemDefault())
            .toEpochSecond() * 1000 // for milliseconds

        var checkoutDateValue = date
        var checkoutMonthValue = month
        var checkoutYearValue = year
        checkOutDate.setOnDateChangedListener { _, year, monthOfYear, dayOfMonth ->
            checkoutYearValue = year
            checkoutMonthValue = monthOfYear + 1
            checkoutDateValue = dayOfMonth

        }
        val checkOutTime = view.findViewById<RadioGroup>(R.id.check_out_time)
        var checkoutDateTime: LocalDateTime =
            LocalDateTime.of(checkoutYearValue, checkoutMonthValue, checkoutDateValue, 9, 0)
        checkOutTime.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == R.id._9am) {
                checkoutDateTime =
                    LocalDateTime.of(
                        checkoutYearValue,
                        checkoutMonthValue,
                        checkoutDateValue,
                        9,
                        0,
                        0
                    )
            }
            if (checkedId == R.id._5pm) {
                checkoutDateTime =
                    LocalDateTime.of(
                        checkoutYearValue,
                        checkoutMonthValue,
                        checkoutDateValue,
                        17,
                        0,
                        0
                    )
            }
        }

        view.findViewById<Button>(R.id.check_availability).setOnClickListener {
            val timeMin =
                "$year-${TwoDigitFormatter.toTwoDigits(month)}-${TwoDigitFormatter.toTwoDigits(date)}T00:00:00+05:30"
            val timeMax =
                "$checkoutYearValue-${TwoDigitFormatter.toTwoDigits(checkoutMonthValue)}-${
                    TwoDigitFormatter.toTwoDigits(
                        checkoutDateValue
                    )
                }T${TwoDigitFormatter.toTwoDigits(checkoutDateTime.hour)}:${
                    TwoDigitFormatter.toTwoDigits(
                        checkoutDateTime.minute
                    )
                }:00+05:30"
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.checkAvailability(
                    timeMin,
                    timeMax
                )
            }

        }
    }
}
