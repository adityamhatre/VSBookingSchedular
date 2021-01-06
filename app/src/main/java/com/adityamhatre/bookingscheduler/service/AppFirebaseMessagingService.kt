package com.adityamhatre.bookingscheduler.service

import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlin.random.Random

class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Application.getInstance().firebaseToken = p0
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        val builder =
            NotificationCompat.Builder(this, getString(R.string.booking_created_channel_id))
                .setSmallIcon(R.drawable.icon)
                .setContentTitle("Booking for ${remoteMessage.data["bookingFor"]}")
                .setContentText("From ${remoteMessage.data["checkIn"]} to ${remoteMessage.data["checkOut"]}")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("From ${remoteMessage.data["checkIn"]} to ${remoteMessage.data["checkOut"]}")
                )
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            notify(Random.nextInt(100), builder.build())
        }
    }
}