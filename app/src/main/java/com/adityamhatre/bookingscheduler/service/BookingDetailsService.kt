package com.adityamhatre.bookingscheduler.service

import com.adityamhatre.bookingscheduler.dtos.*
import com.adityamhatre.bookingscheduler.enums.Accommodation
import java.time.LocalDateTime
import java.time.Month
import java.time.ZoneId
import java.util.*

class BookingDetailsService {
    private val bookingDetailsList = listOf(
        BookingDetails(
            accommodations = Accommodation.all(),
            checkIn = LocalDateTime.of(
                2021,
                Month.JANUARY,
                2,
                9, 30
            ).atZone(ZoneId.systemDefault()).toInstant(),
            checkOut = LocalDateTime.of(
                2021,
                Month.JANUARY,
                2,
                9 + 12, 0
            ).atZone(ZoneId.systemDefault()).toInstant(),
            bookingMainPerson = "Jhumani",
            totalNumberOfPeople = -1,
            bookedBy = ApprovedPerson.RAJESH_MHATRE,
            advancePaymentInfo = AdvancePayment(
                advanceReceived = true,
                amount = 27_000,
                paymentType = PaymentType.CASH
            )
        ),


        BookingDetails(
            accommodations = Accommodation.all(),
            checkIn = LocalDateTime.of(
                2021,
                Month.FEBRUARY,
                18,
                9, 30
            ).atZone(ZoneId.systemDefault()).toInstant(),
            checkOut = LocalDateTime.of(
                2021,
                Month.FEBRUARY,
                20,
                5 + 12, 0
            ).atZone(ZoneId.systemDefault()).toInstant(),
            bookingMainPerson = "Raju Chafekar",
            totalNumberOfPeople = -1,
            bookedBy = ApprovedPerson.RAJESH_MHATRE,
            advancePaymentInfo = AdvancePayment(
                advanceReceived = true,
                amount = 5_000,
                paymentType = PaymentType.CASH
            )
        ),

        BookingDetails(
            accommodations = Accommodation.BUNGALOW_3_1.asList(),
            checkIn = LocalDateTime.of(
                2020,
                Month.DECEMBER,
                31,
                5 + 12, 30
            ).atZone(ZoneId.systemDefault()).toInstant(),
            checkOut = LocalDateTime.of(
                2021,
                Month.JANUARY,
                2,
                9, 0
            ).atZone(ZoneId.systemDefault()).toInstant(),
            bookingMainPerson = "Tendulkar",
            totalNumberOfPeople = 11,
            bookedBy = ApprovedPerson.RAJESH_MHATRE,
            advancePaymentInfo = AdvancePayment(
                advanceReceived = true,
                amount = 5_000,
                paymentType = PaymentType.BANK_DEPOSIT
            )
        ),

        BookingDetails(
            accommodations = listOf(
                Accommodation.ROOM_1_VIHAR,
                Accommodation.ROOM_2_VISHAVA,
                Accommodation.ROOM_3_VISHRAM
            ),
            checkIn = LocalDateTime.of(
                2020,
                Month.DECEMBER,
                31,
                5 + 12, 30
            ).atZone(ZoneId.systemDefault()).toInstant(),
            checkOut = LocalDateTime.of(
                2021,
                Month.JANUARY,
                1,
                9, 0
            ).atZone(ZoneId.systemDefault()).toInstant(),
            bookingMainPerson = "Jadhav",
            totalNumberOfPeople = -1,
            bookedBy = ApprovedPerson.RAJESH_MHATRE,
            advancePaymentInfo = AdvancePayment(
                advanceReceived = true,
                amount = 6_400,
                paymentType = PaymentType.BANK_DEPOSIT
            )
        ),

        BookingDetails(
            accommodations = Accommodation.ROOM_4_VISHRANT.asList(),
            checkIn = LocalDateTime.of(
                2020,
                Month.DECEMBER,
                31,
                5 + 12, 30
            ).atZone(ZoneId.systemDefault()).toInstant(),
            checkOut = LocalDateTime.of(
                2021,
                Month.JANUARY,
                1,
                9, 0
            ).atZone(ZoneId.systemDefault()).toInstant(),
            bookingMainPerson = "Pratik Dalvi",
            totalNumberOfPeople = -1,
            bookedBy = ApprovedPerson.RAJESH_MHATRE,
            advancePaymentInfo = AdvancePayment(
                advanceReceived = true,
                amount = 5_000,
                paymentType = PaymentType.CASH
            )
        ),


        )

    fun getBookingDetailsFor(date: Int = -1, month: Int): List<BookingDetails> {
        return bookingDetailsList.filter {
            val localDateTime = Date.from(it.checkIn)
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()

            if (date != -1) {
                return@filter localDateTime.month.value == month && localDateTime.dayOfMonth == date
            }
            return@filter localDateTime.month.value == month
        }
    }

    fun getAllBookings() =
        bookingDetailsList.sortedWith(compareBy<BookingDetails> { it.checkIn }.thenBy { it.checkOut })
}