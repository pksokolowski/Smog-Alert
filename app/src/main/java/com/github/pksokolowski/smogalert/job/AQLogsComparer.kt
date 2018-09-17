package com.github.pksokolowski.smogalert.job

import com.github.pksokolowski.smogalert.database.AirQualityLog

class AQLogsComparer {
    companion object {
        fun compare(current: AirQualityLog?, previous: AirQualityLog?, warningThreshold: Int): Int {
            if (current == null) return RESULT_NO_INTERPRETATION

            if (previous == null) {
                if (current.errorCode > 0) return interpretErrorCode(current.errorCode)
                if (current.airQualityIndex == -1) return RESULT_DATA_SHORTAGE_STARTED
                if (meetsThreshold(current, warningThreshold)) return RESULT_DEGRADED_PAST_THRESHOLD

                return RESULT_NO_INTERPRETATION
            }

            if (current.errorCode > 0) {
                return if (previous.errorCode == 0) interpretErrorCode(current.errorCode)
                else RESULT_NO_INTERPRETATION
            }

            val currentHasData = current.airQualityIndex != -1
            val previousHasData = previous.airQualityIndex != -1
            if (!currentHasData && previousHasData) return RESULT_DATA_SHORTAGE_STARTED

            val currentReachedThreshold = meetsThreshold(current, warningThreshold)
            val previousReachedThreshold = meetsThreshold(previous, warningThreshold)
            if (currentReachedThreshold && !previousReachedThreshold) return RESULT_DEGRADED_PAST_THRESHOLD
            if (!currentReachedThreshold && previousReachedThreshold) return RESULT_IMPROVED_PAST_THRESHOLD

            return RESULT_NO_INTERPRETATION
        }

        private fun interpretErrorCode(errorCode: Int) =
                if (errorCode > 0) RESULT_ERROR_EMERGED else RESULT_NO_INTERPRETATION


        private fun meetsThreshold(log: AirQualityLog, threshold: Int) = log.airQualityIndex >= threshold

        const val RESULT_NO_INTERPRETATION = 0
        const val RESULT_DEGRADED_PAST_THRESHOLD = 1
        const val RESULT_IMPROVED_PAST_THRESHOLD = 2
        const val RESULT_ERROR_EMERGED = 3
        const val RESULT_DATA_SHORTAGE_STARTED = 7
    }
}