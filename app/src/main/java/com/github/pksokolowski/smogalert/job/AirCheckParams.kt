package com.github.pksokolowski.smogalert.job

import android.os.PersistableBundle

/**
 * holds parameters for air quality assessment procedure. Like treshold air quality index
 * above which a warning should be shown to the user.
 */
data class AirCheckParams(val sensitivity: Int) {
    constructor(bundle: PersistableBundle) : this(bundle.getInt(WARNING_INDEX_LEVEL, 4))

    fun getExtras(): PersistableBundle {
        val bundle = PersistableBundle()

        bundle.putInt(WARNING_INDEX_LEVEL, sensitivity)

        return bundle
    }

    fun getMinimumWarningIndexLevel(): Int{
        return 5 - sensitivity
    }

    companion object {
       private const val WARNING_INDEX_LEVEL = "warning_level"
    }
}