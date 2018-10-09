package com.github.pksokolowski.smogalert.location

import android.app.Application
import android.location.Location
import android.os.SystemClock
import com.github.pksokolowski.smogalert.di.PerApp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerApp
class LocationHelper @Inject constructor(private val context: Application, private val availabilityHelper: LocationAvailabilityHelper) {

    private val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(context)

    class LocationResult(val location: Location?, val errorCode: Int, val activeMethodEmployed: Boolean = false)

    fun getLastLocationData(): LocationResult {
        if (!availabilityHelper.checkGoogleApiAvailability()) {
            return LocationResult(null, GOOGLE_LOCATION_API_IS_UNAVAILABLE)
        }
        if (!availabilityHelper.checkLocationEnabled()) {
            return LocationResult(null, LOCATION_IS_TURNED_OFF)
        }
        if (!availabilityHelper.checkLocationPermission()) {
            return LocationResult(null, NO_LOCATION_PERMISSION)
        }

        val task = fusedLocationClient.lastLocation

        var usedActiveMethod = false
        return try {
            var location = Tasks.await(task, 5000, TimeUnit.MILLISECONDS)

            if (location == null
                    || SystemClock.elapsedRealtimeNanos() - location.elapsedRealtimeNanos > MAX_LOCATION_FIX_AGE_IN_NANOS) {
                usedActiveMethod = true
                location = ActiveLocationRequestHelper.getLocation(context)
            }

            val resultStatus = if (location != null) SUCCESS else UNKNOWN_ERROR
            LocationResult(location, resultStatus, usedActiveMethod)
        } catch (e: java.util.concurrent.TimeoutException) {
            LocationResult(null, TIMEOUT, usedActiveMethod)
        } catch (e: Exception) {
            LocationResult(null, UNKNOWN_EXCEPTION, usedActiveMethod)
        }
    }

    companion object {
        const val SUCCESS = 0
        const val UNKNOWN_ERROR = 1
        const val UNKNOWN_EXCEPTION = 2
        const val NO_LOCATION_PERMISSION = 3
        const val LOCATION_IS_TURNED_OFF = 4
        const val GOOGLE_LOCATION_API_IS_UNAVAILABLE = 5
        const val TIMEOUT = 6

        const val MAX_LOCATION_FIX_AGE_IN_NANOS = 10 * (60 * 1000000000L) /* 10  * (a minute) */
    }
}