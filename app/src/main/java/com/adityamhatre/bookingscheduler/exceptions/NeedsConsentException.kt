package com.adityamhatre.bookingscheduler.exceptions

import android.content.Intent

class NeedsConsentException(val intent: Intent, message: String = "User consent required") : Exception(message)
