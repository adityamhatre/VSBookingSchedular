package com.adityamhatre.bookingscheduler.service

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import com.adityamhatre.bookingscheduler.Application
import com.adityamhatre.bookingscheduler.BuildConfig
import com.adityamhatre.bookingscheduler.dtos.BookingDetails
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.File


class HerokuService(context: Context) {
    private val queue = Volley.newRequestQueue(context)
    private val baseUrl = "https://vs-booking-scheduler-push-serv.herokuapp.com"

    fun notifyNewBooking(bookingDetails: BookingDetails) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "$baseUrl/notifications/newBookingCreated",
            bookingDetails.toNotificationServerJson(),
            {},
            {}
        )

        queue.add(request)
    }

    fun notifyUpdateBooking(bookingDetails: BookingDetails) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "$baseUrl/notifications/updatedBooking",
            bookingDetails.toNotificationServerJson(),
            {},
            {}
        )

        queue.add(request)
    }

    fun removeBooking(bookingDetails: BookingDetails) {
        val request = JsonObjectRequest(
            Request.Method.POST,
            "$baseUrl/deleteBooking",
            bookingDetails.toNotificationServerJson(),
            {},
            {}
        )

        queue.add(request)
    }

    fun checkForUpdates() {
        val buildNumber = BuildConfig.VERSION_CODE
        val request = StringRequest(Request.Method.GET,
            "$baseUrl/checkForUpdates",
            { response ->
                val validResponse = JSONObject(response)
                if (validResponse.getLong("buildNumber") > buildNumber) {
                    Toast.makeText(
                        Application.getInstance().applicationContext,
                        "Downloading update...",
                        Toast.LENGTH_SHORT
                    ).show()
                    downloadAndInstall(validResponse.getString("downloadLink"))
                }
            },
            {}
        )

        queue.add(request)
    }

    private fun downloadAndInstall(downloadLink: String) {
        if (!isExternalStorageWritable()) {
            return
        }

        val externalCacheFile = File(Application.getInstance().externalCacheDir, "update.apk")
        val request = FileRequest(
            downloadLink,
            {
                externalCacheFile.writeBytes(it)


                val urlapk: Uri = FileProvider.getUriForFile(
                    Application.getApplicationContext(),
                    BuildConfig.APPLICATION_ID + ".provider",
                    externalCacheFile
                )
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setDataAndType(urlapk, "application/vnd.android.package-archive")
                intent.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_ACTIVITY_NEW_TASK

                startActivity(Application.getApplicationContext(), intent, null)
            },
            { Log.e("HerokuService.downloadAndInstall", it?.message.toString()) }
        )

        queue.add(request)
    }

    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    fun getBookingSummary(callback: (JSONObject) -> Unit) {
        val request = StringRequest(Request.Method.GET,
            "$baseUrl/bookingsSummary",
            { response ->
                val validResponse = JSONObject(response)
                callback(validResponse)
            },
            {}
        )

        queue.add(request)
    }

}
