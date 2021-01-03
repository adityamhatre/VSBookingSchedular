package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.dtos.AppDateTime
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TimeFrameInputDialogViewModel : ViewModel() {
    private val bookingsService = BookingsService()

    var checkOutDateTime = AppDateTime(-1, -1, -1, 1, -1)
    var checkInDateTime = AppDateTime(-1, -1, -1, 1, -1)

    suspend fun checkAvailability(timeMin: AppDateTime, timeMax: AppDateTime): List<Accommodation> {
        return withContext(Dispatchers.IO) {
            return@withContext bookingsService.checkAvailability(timeMin, timeMax)
        }
    }
}
