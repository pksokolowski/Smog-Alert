package com.github.pksokolowski.smogalert.job

import com.github.pksokolowski.smogalert.db.AirQualityLog

class AQLogsComparer {
    companion object {
        fun compare(current: AirQualityLog?, previous: AirQualityLog?, warningThreshold: Int): Int {
            if (current == null) return RESULT_NO_INTERPRETATION

            fun meetsThreshold(log: AirQualityLog) = log.airQualityIndex >= warningThreshold
            fun partialMeetsThreshold(log: AirQualityLog) = if (log.airQualityIndex == -1) {
                log.details.getHighestIndex() >= warningThreshold
            } else false

            if (previous == null) {
                if (partialMeetsThreshold(current)) return RESULT_DEGRADED_PAST_THRESHOLD
                if (current.errorCode > 0) return interpretErrorCode(current.errorCode)
                if (current.airQualityIndex == -1) return RESULT_DATA_SHORTAGE_STARTED
                if (meetsThreshold(current)) return RESULT_DEGRADED_PAST_THRESHOLD

                return RESULT_NO_INTERPRETATION
            }

            if (current.errorCode > 0) {
                return if (previous.errorCode == 0) interpretErrorCode(current.errorCode)
                else RESULT_NO_INTERPRETATION
            }

            val currentHasData = current.airQualityIndex != -1
            val previousHasData = previous.airQualityIndex != -1
            val currentReachedThreshold = meetsThreshold(current)
            val previousReachedThreshold = meetsThreshold(previous)

            // special cases for partial data availability and bad index
            if (!currentHasData
                    && !previousReachedThreshold
                    && partialMeetsThreshold(current)
                    && !partialMeetsThreshold(previous)) return RESULT_DEGRADED_PAST_THRESHOLD

            if (currentReachedThreshold
                    && !previousHasData
                    && partialMeetsThreshold(previous)) return RESULT_NO_INTERPRETATION

            if (!currentHasData && previousHasData) return RESULT_DATA_SHORTAGE_STARTED
            if (currentHasData && !previousHasData && !currentReachedThreshold) return RESULT_OK_AFTER_SHORTAGE_ENDED

            if (currentReachedThreshold && !previousReachedThreshold) return RESULT_DEGRADED_PAST_THRESHOLD
            if (!currentReachedThreshold && previousReachedThreshold) return RESULT_IMPROVED_PAST_THRESHOLD

            return RESULT_NO_INTERPRETATION
        }

        private fun interpretErrorCode(errorCode: Int) =
                if (errorCode > 0) RESULT_ERROR_EMERGED else RESULT_NO_INTERPRETATION

        const val RESULT_NO_INTERPRETATION = 0
        const val RESULT_DEGRADED_PAST_THRESHOLD = 1
        const val RESULT_IMPROVED_PAST_THRESHOLD = 2
        const val RESULT_OK_AFTER_SHORTAGE_ENDED = 3
        const val RESULT_ERROR_EMERGED = 4
        const val RESULT_DATA_SHORTAGE_STARTED = 7
    }
}