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
import com.adityamhatre.bookingscheduler.dtos.*
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.ui.viewmodels.NewBookingDetailsViewModel
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.util.*


class NewBookingDetailsFragment(
    val checkInDateTime: AppDateTime,
    val checkOutDateTime: AppDateTime,
    val accommodationSet: Set<Accommodation>
) : Fragment() {

    private val viewModel: NewBookingDetailsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_new_booking_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookingScheduleText =
            "From: ${checkInDateTime.toHumanFormat()}" +
                    "\nTo: ${checkOutDateTime.toHumanFormat()}" +
                    "\nIn ${accommodationSet.joinToString { it.readableName }}"
        view.findViewById<TextView>(R.id.booking_schedule).text = bookingScheduleText


        val paymentType = view.findViewById<AutoCompleteTextView>(R.id.payment_type)
        val items = PaymentType.values().map { it.name.toTitleCase() }
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        paymentType.setAdapter(adapter)
        paymentType.setText(items[0])

        val bookingForEditText = view.findViewById<TextInputEditText>(R.id.booking_for)
        val bookingForContainer = view.findViewById<TextInputLayout>(R.id.booking_for_container)

        val numberOfPeopleEditText = view.findViewById<TextInputEditText>(R.id.number_of_people)
        val numberOfPeopleContainer =
            view.findViewById<TextInputLayout>(R.id.number_of_people_container)

        val advanceOrNotAdvanceRadioGroup =
            view.findViewById<RadioGroup>(R.id.advance_or_not_advance_payment)

        val advanceAmountEditText =
            view.findViewById<TextInputEditText>(R.id.advance_payment_amount)
        val advanceAmountContainer =
            view.findViewById<TextInputLayout>(R.id.advance_payment_amount_container)


        var advancePaymentRequired = false
        advanceOrNotAdvanceRadioGroup.setOnCheckedChangeListener { _, btnId ->
            if (btnId == R.id.advance_payment) {
                advanceAmountContainer.visibility = View.VISIBLE
                advancePaymentRequired = true
            }
            if (btnId == R.id.not_advance_payment) {
                advanceAmountContainer.visibility = View.GONE
                advancePaymentRequired = false
            }
        }

        view.findViewById<Button>(R.id.book_button).setOnClickListener {
            val loading = view.findViewById<ProgressBar>(R.id.loading_icon)
            loading.visibility = View.VISIBLE

            var error = false
            if (bookingForEditText.text.toString().isBlank()) {
                bookingForContainer.error = "Provide a name"
                loading.visibility = View.GONE
                error = true
            }
            if (numberOfPeopleEditText.text.toString().isBlank()) {
                numberOfPeopleContainer.error = "Enter number"
                loading.visibility = View.GONE
                error = true
            }
            if (advancePaymentRequired && advanceAmountEditText.text.toString().isBlank()) {
                advanceAmountContainer.error = "Enter amount"
                loading.visibility = View.GONE
                error = true
            }

            if (error) {
                return@setOnClickListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.createBooking(
                    BookingDetails(
                        accommodations = accommodationSet,
                        checkIn = checkInDateTime.toInstant(),
                        checkOut = checkOutDateTime.toInstant(),
                        bookingMainPerson = bookingForEditText.text.toString(),
                        totalNumberOfPeople = numberOfPeopleEditText.text.toString().toInt(),
                        bookedBy = ApprovedPerson.ADITYA_MHATRE,
                        advancePaymentInfo = AdvancePayment(
                            advancePaymentRequired,
                            advanceAmountEditText.text.toString().toInt(),
                            PaymentType.fromTitleCase(paymentType.text.toString())
                        )
                    )
                )
            }.invokeOnCompletion {
                loading.visibility = View.GONE
                Toast.makeText(requireContext(), "Added new booking", Toast.LENGTH_SHORT).show()
                requireActivity().onBackPressed()
                requireActivity().onBackPressed()
            }

        }

    }

    companion object {

        @JvmStatic
        fun newInstance(
            checkInDateTime: AppDateTime,
            checkOutDateTime: AppDateTime,
            accommodationSet: Set<Accommodation>
        ) =
            NewBookingDetailsFragment(checkInDateTime, checkOutDateTime, accommodationSet)
    }
}

private fun String.toTitleCase(): String {
    return (this[0].toUpperCase() + this.substring(1).toLowerCase(Locale.getDefault())).replace(
        "_",
        ""
    )
}
