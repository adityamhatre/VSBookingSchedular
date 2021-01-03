package com.adityamhatre.bookingscheduler

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.adityamhatre.bookingscheduler.ui.views.GoogleSignInFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, GoogleSignInFragment.newInstance())
                .commitNow()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        supportFragmentManager.popBackStack()
    }
}