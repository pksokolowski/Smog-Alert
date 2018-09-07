package com.github.pksokolowski.smogalert.location

import android.app.Application
import android.location.Location
import com.github.pksokolowski.smogalert.di.PerApp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@PerApp
class LocationHelper @Inject constructor(appContext: Application, private val availabilityHelper: LocationAvailabilityHelper) {

    private val fusedLocationClient: FusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(appContext)

    class LocationResult(val location: Location?, val errorCode: Int)

    fun getLastLocation(): LocationResult {
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

        return try {
            val location = Tasks.await(task, 5000, TimeUnit.MILLISECONDS)
            val resultStatus = if (location != null) SUCCESS else UNKNOWN_ERROR
            LocationResult(location, resultStatus)
        } catch (e: java.util.concurrent.TimeoutException) {
            LocationResult(null, TIMEOUT)
        } catch (e: Exception) {
            LocationResult(null, UNKNOWN_EXCEPTION)
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
    }
}