package com.github.pksokolowski.smogalert.utils

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

@PerApp
class InternetConnectionChecker @Inject constructor(private val context: Application) {

    fun isConnectionAvailable(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetwork: NetworkInfo? = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }
}