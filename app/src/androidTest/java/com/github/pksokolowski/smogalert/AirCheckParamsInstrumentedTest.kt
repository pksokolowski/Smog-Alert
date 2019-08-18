package com.github.pksokolowski.smogalert

import android.os.PersistableBundle
import com.github.pksokolowski.smogalert.job.AirCheckParams
import org.junit.Assert.assertEquals
import org.junit.Test

class AirCheckParamsInstrumentedTest {
    @Test
    fun retrievesCorrectValueGivenIntInConstruction() {
        val params = AirCheckParams(2)
        val retrieved = params.sensitivity
        assertEquals(2, retrieved)
    }

    @Test
    fun retrievesCorrectValueGivenBundleInConstruction() {
        val bundle = PersistableBundle().apply {
            putInt(WARNING_INDEX_LEVEL, 3)
        }

        val params = AirCheckParams(bundle)
        val retrieved = params.sensitivity
        assertEquals(3, retrieved)
    }

    @Test
    fun knowsWhenJobIsRetryWhileItIs() {
        val params = AirCheckParams(2, true)
        val retrieved = params.isOneTimeRetry
        assertEquals(true, retrieved)
    }

    @Test
    fun knowsWhenJobIsNotRetryWhileItIsNot() {
        val params = AirCheckParams(2, false)
        val retrieved = params.isOneTimeRetry
        assertEquals(false, retrieved)
    }

    @Test
    fun knowsWhenJobIsRetryWhileItIs_WithBundleConstructor() {
        val bundle = PersistableBundle().apply {
            putInt(WARNING_INDEX_LEVEL, 3)
            putBoolean(IS_ONE_TIME_RETRY, true)
        }
        val params = AirCheckParams(bundle)
        val retrieved = params.isOneTimeRetry
        assertEquals(true, retrieved)
    }

    @Test
    fun knowsWhenJobIsNotRetryWhileItIsNot_WithBundleConstructor() {
        val bundle = PersistableBundle().apply {
            putInt(WARNING_INDEX_LEVEL, 3)
            putBoolean(IS_ONE_TIME_RETRY, false)
        }
        val params = AirCheckParams(bundle)
        val retrieved = params.isOneTimeRetry
        assertEquals(false, retrieved)
    }

    @Test
    fun doesSaveBothParametersCorrectlyInTheBundle() {
        val params = AirCheckParams(AirCheckParams(2, true).getExtras())

        assertEquals(true, params.isOneTimeRetry)
        assertEquals(2, params.sensitivity)
    }

    companion object {
        private const val WARNING_INDEX_LEVEL = "warning_level"
        private const val IS_ONE_TIME_RETRY = "is_one_time_retry"
    }
}