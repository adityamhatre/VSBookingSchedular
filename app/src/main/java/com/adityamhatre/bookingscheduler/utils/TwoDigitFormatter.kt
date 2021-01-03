package com.adityamhatre.bookingscheduler.utils

class TwoDigitFormatter {
    companion object {
        fun toTwoDigits(arg: Int): String = if (arg >= 10) arg.toString() else "0$arg"
    }
}