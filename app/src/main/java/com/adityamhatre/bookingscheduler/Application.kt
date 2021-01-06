package com.adityamhatre.bookingscheduler

import android.accounts.Account
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import com.adityamhatre.bookingscheduler.converters.BookingDetailsDeserializer
import com.adityamhatre.bookingscheduler.converters.InstantTypeConverter
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.GsonBuilder
import java.time.Instant
import java.util.*

class Application : Application() {
    lateinit var account: Account
    val gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeConverter())
        .registerTypeAdapter(BookingDetails::class.java, BookingDetailsDeserializer())
        .create()
    lateinit var firebaseToken: String
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.booking_created)
            val descriptionText = getString(R.string.booking_created)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(
                getString(R.string.booking_created_channel_id),
                name,
                importance
            ).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        createNotificationChannel()
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }
            val token = task.result
            firebaseToken = token.toString()

            Firebase.messaging.subscribeToTopic("new-booking-topic")
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.i("Application", "Subscribed to new-booking-topic")
                    } else {
                        Log.e("Application", "Error in subscribing to new-booking-topic")
                    }

                }

        })
    }

    companion object {
        private lateinit var application: com.adityamhatre.bookingscheduler.Application
        fun getApplicationContext(): Context = application.applicationContext

        @Synchronized
        fun getInstance() = application
    }
}