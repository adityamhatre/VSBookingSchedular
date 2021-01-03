package com.adityamhatre.bookingscheduler.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.adityamhatre.bookingscheduler.enums.Accommodation
import com.adityamhatre.bookingscheduler.service.BookingsService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TimeFrameInputDialogViewModel : ViewModel() {
    private val bookingsService = BookingsService()

    suspend fun checkAvailability(timeMin: String, timeMax: String): List<Accommodation> {
        return withContext(Dispatchers.IO) {
            return@withContext bookingsService.checkAvailability(timeMin, timeMax)
        }
    }
}
