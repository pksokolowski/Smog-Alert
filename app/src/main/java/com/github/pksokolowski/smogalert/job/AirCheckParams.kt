package com.github.pksokolowski.smogalert.job

import android.os.PersistableBundle

/**
 * holds parameters for air quality assessment procedure. Like treshold air quality index
 * above which a warning should be shown to the user.
 */
data class AirCheckParams(val sensitivity: Int, val isOneTimeRetry: Boolean = false) {
    constructor(bundle: PersistableBundle) : this(
            bundle.getInt(WARNING_INDEX_LEVEL, 4),
            bundle.getBoolean(IS_ONE_TIME_RETRY, false))

    fun getExtras(): PersistableBundle {
        val bundle = PersistableBundle()

        bundle.putInt(WARNING_INDEX_LEVEL, sensitivity)
        bundle.putBoolean(IS_ONE_TIME_RETRY, isOneTimeRetry)

        return bundle
    }

    fun getMinimumWarningIndexLevel(): Int {
        return 5 - sensitivity
    }

    companion object {
        private const val WARNING_INDEX_LEVEL = "warning_level"
        private const val IS_ONE_TIME_RETRY = "is_one_time_retry"
    }
}