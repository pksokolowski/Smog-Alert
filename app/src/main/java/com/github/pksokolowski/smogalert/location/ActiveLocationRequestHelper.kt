package com.github.pksokolowski.smogalert.location

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.SystemClock
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit

class ActiveLocationRequestHelper : BroadcastReceiver() {
    override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent == null) return
        if (intent.action == ACTION_PROCESS_UPDATES) {
            val result = LocationResult.extractResult(intent)
                    ?: return
            synchronized(lock) {
                lastActivelyFetchedLocation = result.lastLocation
                isLocationReady = true
                lock.notifyAll()
            }
        }
    }

    companion object {
        private const val ACTION_PROCESS_UPDATES = "com.github.pksokolowski.smogalert.action_process_location_update"
        private const val TIMEOUT_MILLIS = 20000L /* 20 seconds */

        @Volatile
        private var lastActivelyFetchedLocation: Location? = null
        private var isLocationReady: Boolean = false

        private val lock = java.lang.Object()

        fun getLocation(context: Context): Location? = synchronized(lock) {
            lastActivelyFetchedLocation = null
            isLocationReady = false

            // check permission
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) return null

            // get fused location client
            val fusedLocationClient: FusedLocationProviderClient =
                    LocationServices.getFusedLocationProviderClient(context)

            val mLocationRequest = LocationRequest().apply {
                priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
                interval = 5000
                // fastest interval set low, but not zero (just in case), because it delayed
                // responses for subsequent requests in testing.
                fastestInterval = 1
                numUpdates = 1
                setExpirationDuration(TIMEOUT_MILLIS)
            }
            val task = fusedLocationClient.requestLocationUpdates(
                    mLocationRequest,
                    getPendingIntent(context))

            try {
                Tasks.await(task, 5000, TimeUnit.MILLISECONDS)
            } catch (e: Exception) {
                return null
            }

            var timeLeft = TIMEOUT_MILLIS
            val startTime = SystemClock.elapsedRealtime()
            while (!isLocationReady && timeLeft > 0) {
                lock.wait(timeLeft)
                val now = SystemClock.elapsedRealtime()
                timeLeft = TIMEOUT_MILLIS - (now - startTime)
            }

            return lastActivelyFetchedLocation
        }

        private fun getPendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, ActiveLocationRequestHelper::class.java)
            intent.action = ACTION_PROCESS_UPDATES
            return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
    }
}