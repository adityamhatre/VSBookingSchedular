package com.adityamhatre.bookingscheduler.ui.views

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.MainActivity
import com.adityamhatre.bookingscheduler.R
import com.adityamhatre.bookingscheduler.dtos.*
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.ui.viewmodels.NewBookingDetailsViewModel
import com.ebanx.swipebtn.SwipeButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.util.*
import kotlin.collections.ArrayList


class NewBookingDetailsFragment(
    val checkInDateTime: AppDateTime,
    val checkOutDateTime: AppDateTime,
    val accommodationSet: Set<Accommodation>,
    val editMode: Boolean = false,
    val originalBookingDetails: BookingDetails? = null,
    val onBookingDetailsUpdated: () -> Unit = { }
) : Fragment() {

    private val viewModel: NewBookingDetailsViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.checkInDateTime = checkInDateTime
        viewModel.checkOutDateTime = checkOutDateTime
        viewModel.accommodationSet = accommodationSet
        if (editMode) {
            viewModel.fillValues(originalBookingDetails!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_new_booking_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bookingScheduleText =
            "From: ${viewModel.checkInDateTime.toHumanFormat()}" +
                    "\nTo: ${viewModel.checkOutDateTime.toHumanFormat()}" +
                    "\nIn ${
                        Accommodation.bungalow51List(viewModel.accommodationSet)
                            .joinToString { it.readableName }
                    }"
        view.findViewById<TextView>(R.id.booking_schedule).text = bookingScheduleText


        val paymentType = view.findViewById<MaterialAutoCompleteTextView>(R.id.payment_type)
        val paymentTypeContainer = view.findViewById<TextInputLayout>(R.id.payment_type_container)
        paymentType.doOnTextChanged { text, _, _, _ ->
            viewModel.paymentType = PaymentType.fromTitleCase(text.toString())
        }
        paymentType.setAdapter(viewModel.getPaymentTypeAdapter(requireContext()))
        paymentType.setText(paymentType.adapter.getItem(0).toString(), false)

        val bookingForEditText = view.findViewById<TextInputEditText>(R.id.booking_for)
        val bookingForContainer = view.findViewById<TextInputLayout>(R.id.booking_for_container)
        if (editMode) {
            bookingForEditText.setText(viewModel.bookingFor)
        }
        bookingForEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.bookingFor = text.toString()
            viewModel.validate()
        }


        val numberOfPeopleEditText = view.findViewById<TextInputEditText>(R.id.number_of_people)
        val numberOfPeopleContainer =
            view.findViewById<TextInputLayout>(R.id.number_of_people_container)
        if (editMode) {
            numberOfPeopleEditText.setText(viewModel.numberOfPeople.toString())
        }
        numberOfPeopleEditText.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isNotBlank()) {
                viewModel.numberOfPeople = text.toString().toInt()
            } else {
                viewModel.numberOfPeople = -1
            }
            viewModel.validate()
        }

        val phoneNumberEditText = view.findViewById<TextInputEditText>(R.id.phone_number)
        val phoneNumberContainer =
            view.findViewById<TextInputLayout>(R.id.phone_number_container)
        if (editMode) {
            phoneNumberEditText.setText(viewModel.phoneNumber)
        }
        phoneNumberEditText.doOnTextChanged { text, _, _, _ ->
            viewModel.phoneNumber = text.toString()
            viewModel.validate()
        }

        val advanceOrNotAdvanceRadioGroup =
            view.findViewById<RadioGroup>(R.id.advance_or_not_advance_payment)

        val advanceAmountEditText =
            view.findViewById<TextInputEditText>(R.id.advance_payment_amount)
        val advanceAmountContainer =
            view.findViewById<TextInputLayout>(R.id.advance_payment_amount_container)
        advanceAmountEditText.doOnTextChanged { text, _, _, _ ->
            if (text.toString().isNotBlank()) {
                viewModel.advancePaymentAmount = text.toString().toInt()
            } else {
                viewModel.advancePaymentAmount = -1
            }
            viewModel.validate()
        }

        advanceOrNotAdvanceRadioGroup.setOnCheckedChangeListener { _, btnId ->
            if (btnId == R.id.advance_payment) {
                advanceAmountContainer.visibility = View.VISIBLE
                paymentTypeContainer.visibility = View.VISIBLE
                viewModel.advancePaymentRequired = true
                if (advanceAmountEditText.text.toString().isNotBlank()) {
                    viewModel.advancePaymentAmount = advanceAmountEditText.text.toString().toInt()
                } else {
                    viewModel.advancePaymentAmount = -1
                }
            }
            if (btnId == R.id.not_advance_payment) {
                advanceAmountContainer.visibility = View.GONE
                paymentTypeContainer.visibility = View.GONE
                viewModel.advancePaymentRequired = false
                viewModel.advancePaymentAmount = -1
            }
            viewModel.validate()
        }

        if (editMode) {
            if (viewModel.advancePaymentRequired) {
                advanceAmountEditText.setText(viewModel.advancePaymentAmount.toString())
            } else {
                view.findViewById<RadioButton>(R.id.not_advance_payment).isChecked = true
            }
        }

        val bookButton = view.findViewById<SwipeButton>(R.id.book_button)
        bookButton.setText(if (editMode) "Swipe to update booking" else "Swipe to book")
        viewModel.isValid().observe(viewLifecycleOwner) {
            bookButton.isEnabled = it
            bookButton.setDisabledDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    if (it) R.drawable.book_arrow else R.drawable.invalid
                )
            )
        }
        bookButton.setOnActiveListener {
            if (!viewModel.isValid().value!!) {
                Toast.makeText(requireContext(), "Enter all data", Toast.LENGTH_SHORT).show()
                return@setOnActiveListener
            }

            (requireActivity() as MainActivity).hideKeyboard()

            val loading = view.findViewById<ProgressBar>(R.id.loading_icon)
            loading.visibility = View.VISIBLE

            var error = false
            if (viewModel.bookingFor.isBlank()) {
                bookingForContainer.error = "Provide a name"
                loading.visibility = View.GONE
                error = true
            }
            if (viewModel.phoneNumber.isBlank()) {
                phoneNumberContainer.error = "Provide a phone number"
                loading.visibility = View.GONE
                error = true
            }
            if (viewModel.numberOfPeople < 1) {
                numberOfPeopleContainer.error = "Enter number"
                loading.visibility = View.GONE
                error = true
            }
            if (viewModel.advancePaymentRequired && viewModel.advancePaymentAmount < 1) {
                advanceAmountContainer.error = "Enter amount"
                loading.visibility = View.GONE
                error = true
            }

            if (error) {
                return@setOnActiveListener
            }

            viewLifecycleOwner.lifecycleScope.launch {
                val bookingDetails = BookingDetails(
                    accommodations = viewModel.accommodationSet,
                    checkIn = viewModel.checkInDateTime.toInstant(),
                    checkOut = viewModel.checkOutDateTime.toInstant(),
                    bookingMainPerson = viewModel.bookingFor,
                    totalNumberOfPeople = viewModel.numberOfPeople,
                    bookedBy = if (editMode) originalBookingDetails!!.bookedBy else ApprovedPerson.findByEmail(
                        Application.getInstance().account.name
                    ),
                    advancePaymentInfo = AdvancePayment(
                        viewModel.advancePaymentRequired,
                        viewModel.advancePaymentAmount,
                        viewModel.paymentType
                    ),
                    phoneNumber = viewModel.phoneNumber,
                    bookingIdOnGoogle = UUID.randomUUID().toString(),
                    eventIds = if (editMode) originalBookingDetails!!.eventIds else ArrayList()
                )
                if (editMode) {
                    viewModel.updateBooking(bookingDetails)
                    onBookingDetailsUpdated()
                } else {
                    viewModel.createBooking(bookingDetails)
                }
            }.invokeOnCompletion {
                loading.visibility = View.GONE
                try {
                    Toast.makeText(
                        requireContext(),
                        if (!editMode) "Added new booking" else "Updated booking",
                        Toast.LENGTH_SHORT
                    ).show()
                    if (!editMode) {
                        requireActivity().onBackPressed()
                    }
                    requireActivity().onBackPressed()
                } catch (ie: IllegalStateException) {
                    Log.e(
                        "NewBookingDetailsFragment:viewModel.createBooking#invokeOnCompletion",
                        "Not attached to fragment"
                    )
                }
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

        @JvmStatic
        fun newInstance(
            bookingDetails: BookingDetails,
            onBookingDetailsUpdated: () -> Unit
        ): NewBookingDetailsFragment {
            return NewBookingDetailsFragment(
                bookingDetails.checkIn.toAppDateTime(),
                bookingDetails.checkIn.toAppDateTime(),
                bookingDetails.accommodations,
                editMode = true,
                bookingDetails,
                onBookingDetailsUpdated
            )
        }
    }
}

private fun Instant.toAppDateTime(): AppDateTime {
    return with(
        this.atZone(ZoneId.of(ZoneId.SHORT_IDS["IST"]))
            .toLocalDateTime()
    ) { AppDateTime(dayOfMonth, monthValue, year, hour, minute) }
}

