package com.adityamhatre.bookingscheduler.dtos

sealed class GCalResult<out T> {
    data class Success<T>(val value: T) : GCalResult<T>()
    data class NeedsConsent(val intent: android.content.Intent) : GCalResult<Nothing>()
    data class Failure(val error: Throwable) : GCalResult<Nothing>()
}