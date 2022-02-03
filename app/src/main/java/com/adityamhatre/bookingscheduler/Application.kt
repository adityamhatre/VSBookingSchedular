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
import com.adityamhatre.bookingscheduler.service.HerokuService
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.Instant
import java.util.*

class Application : Application() {
    lateinit var account: Account
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(Instant::class.java, InstantTypeConverter())
        .registerTypeAdapter(BookingDetails::class.java, BookingDetailsDeserializer())
        .create()
    lateinit var firebaseToken: String
    private val herokuService by lazy { HerokuService(this.applicationContext) }
    val topics = listOf("new-booking-topic", "updated-booking-topic", "tomorrow-booking-topic")

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            topics.forEach {
                lateinit var name: String
                lateinit var descriptionText: String
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                lateinit var channel: NotificationChannel
                var init = true
                when (it) {
                    topics[0] -> {
                        name = getString(R.string.booking_created)
                        descriptionText = getString(R.string.booking_created)
                        channel = NotificationChannel(
                            getString(R.string.booking_created_channel_id),
                            name,
                            importance
                        ).apply {
                            description = descriptionText
                        }
                    }
                    topics[1] -> {
                        name = getString(R.string.booking_updated)
                        descriptionText = getString(R.string.booking_updated)
                        channel = NotificationChannel(
                            getString(R.string.booking_updated_channel_id),
                            name,
                            importance
                        ).apply {
                            description = descriptionText
                        }
                    }
                    topics[2] -> {
                        name = getString(R.string.booking_tomorrow)
                        descriptionText = getString(R.string.booking_tomorrow)
                        channel = NotificationChannel(
                            getString(R.string.booking_tomorrow_channel_id),
                            name,
                            importance
                        ).apply {
                            description = descriptionText
                        }
                    }
                    else -> init = false
                }

                if (init) {
                    val notificationManager: NotificationManager =
                        getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }
            }

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



            topics.forEach {
                Firebase.messaging.subscribeToTopic(it)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.i("Application", "Subscribed to $it")
                        } else {
                            Log.e("Application", "Error in subscribing to $it")
                        }
                    }
            }

        })
    }

    fun checkForUpdates() {
        herokuService.checkForUpdates()
    }

    @JvmName("getHerokuServiceJvm")
    fun getHerokuService(): HerokuService = herokuService

    companion object {
        val year: Int = Calendar.getInstance(TimeZone.getDefault()).get(Calendar.YEAR)
        private lateinit var application: com.adityamhatre.bookingscheduler.Application
        fun getApplicationContext(): Context = application.applicationContext

        @Synchronized
        fun getInstance() = application
    }
}