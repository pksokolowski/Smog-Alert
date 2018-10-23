package com.github.pksokolowski.smogalert.notifications

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.support.v4.app.NotificationCompat
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject
import android.app.PendingIntent
import android.graphics.Color
import android.media.RingtoneManager
import com.github.pksokolowski.smogalert.MainActivity


@PerApp
class NotificationHelper @Inject constructor(private val context: Application) {

    init {
        createNotificationChannels()
    }

    fun showAlert() {
        val b = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                .setContentText(context.getString(R.string.notification_alerts_message))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_warning_white_24dp)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.RED)
                .setLights(Color.RED, LIGHTS_ON_TIME, LIGHT_OFF_TIME)
                .setContentIntent(getOpenMainActivityPendingIntent(context))
                .setAutoCancel(true)

        val notification = b.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATIONS_ID, notification)
    }

    fun showImprovement() {
        val b = NotificationCompat.Builder(context, CHANNEL_ID_IMPROVEMENT)
                .setContentText(context.getString(R.string.notification_improvement_message))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_info_white_24dp)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.GREEN)
                .setLights(Color.GREEN, LIGHTS_ON_TIME, LIGHT_OFF_TIME)
                .setContentIntent(getOpenMainActivityPendingIntent(context))
                .setAutoCancel(true)

        val notification = b.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATIONS_ID, notification)
    }

    fun showDataShortage() {
        val b = NotificationCompat.Builder(context, CHANNEL_ID_DATA_SHORTAGE)
                .setContentText(context.getString(R.string.notification_data_shortage_message))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_warning_white_24dp)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.YELLOW)
                .setLights(Color.YELLOW, LIGHTS_ON_TIME, LIGHT_OFF_TIME)
                .setContentIntent(getOpenMainActivityPendingIntent(context))
                .setAutoCancel(true)

        val notification = b.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATIONS_ID, notification)
    }

    fun showAirIsOkAfterShortage() {
        val b = NotificationCompat.Builder(context, CHANNEL_ID_DATA_SHORTAGE)
                .setContentText(context.getString(R.string.notification_data_shortage_over_air_ok_message))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_info_white_24dp)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.GREEN)
                .setLights(Color.GREEN, LIGHTS_ON_TIME, LIGHT_OFF_TIME)
                .setContentIntent(getOpenMainActivityPendingIntent(context))
                .setAutoCancel(true)

        val notification = b.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATIONS_ID, notification)
    }

    fun showError() {
        val b = NotificationCompat.Builder(context, CHANNEL_ID_DATA_SHORTAGE)
                .setContentText(context.getString(R.string.notification_error_message))
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_warning_white_24dp)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setColor(Color.YELLOW)
                .setLights(Color.YELLOW, LIGHTS_ON_TIME, LIGHT_OFF_TIME)
                .setContentIntent(getOpenMainActivityPendingIntent(context))
                .setAutoCancel(true)

        val notification = b.build()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(NOTIFICATIONS_ID, notification)
    }

    private fun getOpenMainActivityPendingIntent(context: Context): PendingIntent {
        val notificationIntent = Intent(context, MainActivity::class.java)
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        return PendingIntent.getActivity(context, 0, notificationIntent, PendingIntent.FLAG_CANCEL_CURRENT)
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val alertsChannel = NotificationChannel(CHANNEL_ID_ALERTS,
                    context.getString(R.string.notification_channel_alerts_title),
                    NotificationManager.IMPORTANCE_HIGH).apply {
                description = context.getString(R.string.notification_channel_alerts_description)
                enableLights(true)
                lightColor = Color.RED
            }

            val improvementChannel = NotificationChannel(CHANNEL_ID_IMPROVEMENT,
                    context.getString(R.string.notification_channel_improvement_title),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notification_channel_improvement_description)
                enableLights(true)
                lightColor = Color.GREEN
            }

            val errorsChannel = NotificationChannel(CHANNEL_ID_ERRORS,
                    context.getString(R.string.notification_channel_errors_title),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notification_channel_errors_description)
                enableLights(true)
                lightColor = Color.YELLOW
            }

            val dataShortageChannel = NotificationChannel(CHANNEL_ID_DATA_SHORTAGE,
                    context.getString(R.string.notification_channel_data_shortage_title),
                    NotificationManager.IMPORTANCE_DEFAULT).apply {
                description = context.getString(R.string.notification_channel_data_shortage_description)
                enableLights(true)
                lightColor = Color.YELLOW
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(
                    alertsChannel,
                    improvementChannel,
                    errorsChannel,
                    dataShortageChannel))
        }

    }

    companion object {
        const val CHANNEL_ID_ALERTS = "alerts"
        const val CHANNEL_ID_IMPROVEMENT = "improvement"
        const val CHANNEL_ID_ERRORS = "errors"
        const val CHANNEL_ID_DATA_SHORTAGE = "data_shortage"

        const val NOTIFICATIONS_ID = 0

        const val LIGHTS_ON_TIME = 1000
        const val LIGHT_OFF_TIME = 600
    }
}