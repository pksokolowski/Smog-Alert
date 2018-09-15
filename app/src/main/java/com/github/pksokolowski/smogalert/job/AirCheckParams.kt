package com.github.pksokolowski.smogalert.job

import android.os.PersistableBundle

/**
 * holds parameters for air quality assessment procedure. Like treshold air quality index
 * above which a warning should be shown to the user.
 */
data class AirCheckParams(val minimumWarningIndexLevel: Int) {
    constructor(bundle: PersistableBundle) : this(bundle.getInt(WARNING_INDEX_LEVEL, 4))

    fun getExtras(): PersistableBundle {
        val bundle = PersistableBundle()

        bundle.putInt(WARNING_INDEX_LEVEL, minimumWarningIndexLevel)

        return bundle
    }

    companion object {
       private const val WARNING_INDEX_LEVEL = "warning_level"

        /**
         * A special value. It is greater than the maximum air quality index value
         * thus it will never trigger warning the user, as the actual index level will always
         * be smaller than this.
         *
         * Meant to be used with the minimumWarningIndexLevel of the AirCheckParams class.
         */
        const val INDEX_LEVEL_UNREACHABLE = Int.MAX_VALUE
    }
}