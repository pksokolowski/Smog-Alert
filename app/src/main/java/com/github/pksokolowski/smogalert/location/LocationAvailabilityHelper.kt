package com.github.pksokolowski.smogalert.location

import android.Manifest
import android.app.Application
import android.content.IntentSender
import android.content.pm.PackageManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import com.github.pksokolowski.smogalert.di.PerApp
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task

import javax.inject.Inject

/**
 * Helps with permissions acquisition, settings and google play services version checking.
 */
@PerApp
class LocationAvailabilityHelper @Inject constructor(private val context: Application) {

    fun checkAvailabilityAndPromptUserIfNeeded(activity: AppCompatActivity) {
        // API must first be in place to prevent potential crashes if used in below methods
        if (!checkGoogleApiAvailability(activity)) return

        checkLocationPermission(activity)
        checkLocationSettings(activity)
    }

    fun checkOverallAvailability(): Boolean {
        if (checkGoogleApiAvailability()
                && checkLocationEnabled()
                && checkLocationPermission()) return true
        return false
    }

    private fun createLocationRequest(): LocationRequest {
        val updatesInterval = 50 * 60000L

        return LocationRequest().apply {
            interval = updatesInterval
            fastestInterval = updatesInterval
            priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        }
    }

    private fun checkLocationSettings(activity: AppCompatActivity) {
        val locationRequest = createLocationRequest()
        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest)

        val client: SettingsClient = LocationServices.getSettingsClient(activity)
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())
        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    exception.startResolutionForResult(activity,
                            REQUEST_CHECK_SETTINGS)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    fun checkLocationEnabled(): Boolean {
        val locationSetting = Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        return locationSetting != Settings.Secure.LOCATION_MODE_OFF
    }

    /**
     * @param activity if set, the method will show a permission request dialog if needed.
     */
    fun checkLocationPermission(activity: AppCompatActivity? = null): Boolean {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return true
        }

        if (activity != null) {
            ActivityCompat.requestPermissions(activity,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)

        }

        return false
    }

    /**
     * @param activity if set, the method will show a prompt user can act on to fix issues encountered
     */
    fun checkGoogleApiAvailability(activity: AppCompatActivity? = null): Boolean {
        val apiAvailabilityChecker = GoogleApiAvailability.getInstance()
        val resultCode = apiAvailabilityChecker.isGooglePlayServicesAvailable(context)

        if (resultCode == ConnectionResult.SUCCESS) return true

        if (activity != null) {
            if (apiAvailabilityChecker.isUserResolvableError(resultCode)) {
                apiAvailabilityChecker.getErrorDialog(activity,
                        resultCode,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show()
            }

        }

        return false
    }

    companion object {
        private const val PLAY_SERVICES_RESOLUTION_REQUEST = 9000
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 0
        private const val REQUEST_CHECK_SETTINGS = 1
    }
}