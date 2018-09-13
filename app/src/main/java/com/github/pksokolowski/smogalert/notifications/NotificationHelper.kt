package com.github.pksokolowski.smogalert.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.database.AirQualityLog
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject
import android.app.PendingIntent
import com.github.pksokolowski.smogalert.MainActivity
import com.github.pksokolowski.smogalert.utils.TimeHelper


@PerApp
class NotificationHelper @Inject constructor(private val context: Application) {

    init {
        createNotificationChannels()
    }

    fun showAlert(airQualityLog: AirQualityLog) {
        val message = "id: ${airQualityLog.id}, AQ: ${airQualityLog.airQualityIndex}, stationId: ${airQualityLog.stationId}, err: ${airQualityLog.errorCode}, time: ${TimeHelper.getTimeStampString(airQualityLog.timeStamp)}"

        val b = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentText(message)
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_warning_white_24dp)
                .setContentIntent(getOpenMainActivityPendingIntent(context))

        val notification = b.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATION_ID_ALERT, notification)
    }

    private fun getOpenMainActivityPendingIntent(context: Context): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.notification_channel_alerts_title)
            val description = context.getString(R.string.notification_channel_alerts_description)

            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID_ALERTS, name, importance)
            channel.description = description

            val notificationManager = context.getSystemService(NotificationManager::class.java)
            notificationManager!!.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID_ALERTS = "alerts"
        const val NOTIFICATION_ID_ALERT = 0
    }
}