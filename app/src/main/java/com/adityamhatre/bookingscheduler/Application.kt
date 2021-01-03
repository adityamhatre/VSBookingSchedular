package com.adityamhatre.bookingscheduler

import android.accounts.Account
import android.app.Application

class Application : Application() {
    override fun onCreate() {
        super.onCreate()
        application = this
    }

    companion object {
        private var application: com.adityamhatre.bookingscheduler.Application? = null
        fun getApplicationContext() = application!!.applicationContext!!
        lateinit var account: Account
    }
}