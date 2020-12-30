package com.adityamhatre.bookingscheduler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adityamhatre.bookingscheduler.service.BookingDetailsService
import com.adityamhatre.bookingscheduler.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    val bookingDetailsService by lazy { BookingDetailsService() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, MainFragment.newInstance())
                .commitNow()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFragmentManager.popBackStack()
    }
}