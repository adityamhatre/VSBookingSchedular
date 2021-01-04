package com.adityamhatre.bookingscheduler

import android.accounts.Account
import android.app.Application
import com.adityamhatre.bookingscheduler.googleapi.InstantTypeConverter
import com.google.gson.GsonBuilder
import java.time.Instant

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        private var application: com.adityamhatre.bookingscheduler.Application? = null
        fun getApplicationContext() = application!!.applicationContext!!
        lateinit var account: Account
        val gson = GsonBuilder().registerTypeAdapter(Instant::class.java, InstantTypeConverter()).create()
    }
}