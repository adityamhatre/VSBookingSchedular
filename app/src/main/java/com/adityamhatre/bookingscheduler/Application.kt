package com.adityamhatre.bookingscheduler

import android.accounts.Account
import android.app.Application
import android.content.Context
import com.adityamhatre.bookingscheduler.converters.BookingDetailsDeserializer
import com.adityamhatre.bookingscheduler.converters.InstantTypeConverter
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.google.gson.GsonBuilder
import java.time.Instant

class Application : Application() {
    lateinit var account: Account
    val gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeConverter())
        .registerTypeAdapter(BookingDetails::class.java, BookingDetailsDeserializer())
        .create()
     var firebaseToken: String=""

    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        private lateinit var application: com.adityamhatre.bookingscheduler.Application
        fun getApplicationContext(): Context = application.applicationContext
        @Synchronized
        fun getInstance() = application
    }
}