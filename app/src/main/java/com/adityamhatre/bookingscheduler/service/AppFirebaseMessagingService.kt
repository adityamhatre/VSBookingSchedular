package com.adityamhatre.bookingscheduler.service

import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
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
        val builder =
            NotificationCompat.Builder(this, getString(R.string.booking_created_channel_id))
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.icon))
                .setContentTitle("New booking for ${remoteMessage.data["bookingFor"]}")
                .setContentText("From ${remoteMessage.data["checkIn"]} ...")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("From ${remoteMessage.data["checkIn"]} to ${remoteMessage.data["checkOut"]}")
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent.getActivity(this, 0, Intent(), 0));

        val notificationId = Random.nextInt(100)
        with(NotificationManagerCompat.from(this)) {
            notify(notificationId, builder.build())
        }
    }
}