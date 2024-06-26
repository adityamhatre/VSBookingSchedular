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
import java.time.Duration
import java.time.Instant
import kotlin.math.abs
import kotlin.random.Random


class AppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Application.getInstance().firebaseToken = p0
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        if (!remoteMessage.data.containsKey("notificationId")) {
            return
        }

        val now = Instant.now()
        val sharedPrefs = getSharedPreferences("notifications", MODE_PRIVATE)
        val lastSentNotificationOn = sharedPrefs.getLong("last-sent-notification-on", -1)

        if (lastSentNotificationOn == -1L) {
            sharedPrefs.edit()
                .putLong("last-sent-notification-on", Instant.now().toEpochMilli()).apply()
        } else {
            if (abs(Duration.between(now, Instant.ofEpochMilli(lastSentNotificationOn)).toMillis()) < 500L) {
                return
            } else {
                sharedPrefs.edit()
                    .putLong("last-sent-notification-on", now.toEpochMilli())
                    .apply()
            }
        }

        val topic = remoteMessage.data["topic"]
        val builder =
            NotificationCompat.Builder(this, getString(R.string.booking_created_channel_id))
                .setSmallIcon(R.drawable.icon)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.icon))
                .setContentText("From ${remoteMessage.data["checkIn"]} ...")
                .setStyle(
                    NotificationCompat.BigTextStyle()
                        .bigText("From ${remoteMessage.data["checkIn"]} to ${remoteMessage.data["checkOut"]}")
                )
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(PendingIntent.getActivity(this, 0, Intent(), PendingIntent.FLAG_IMMUTABLE));

        var init = true
        when (topic) {
            Application.getInstance().topics[0] -> {
                builder.setChannelId(getString(R.string.booking_created_channel_id))
                builder.setContentTitle("New booking for ${remoteMessage.data["bookingMainPerson"]}")
            }
            Application.getInstance().topics[1] -> {
                builder.setChannelId(getString(R.string.booking_updated_channel_id))
                builder.setContentTitle("Booking updated for ${remoteMessage.data["bookingMainPerson"]}")
            }
            Application.getInstance().topics[2] -> {
                builder.setChannelId(getString(R.string.booking_updated_channel_id))
                builder.setContentTitle("Booking for ${remoteMessage.data["bookingMainPerson"]} is tomorrow!")
            }
            else -> init = false
        }

        if (init) {
            val notificationId = Random.nextInt(100)
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        }
    }
}