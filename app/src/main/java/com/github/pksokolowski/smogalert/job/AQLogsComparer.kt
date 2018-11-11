package com.github.pksokolowski.smogalert.job

import com.github.pksokolowski.smogalert.db.AirQualityLog

class AQLogsComparer {
    companion object {
        fun compare(current: AirQualityLog?, previous: AirQualityLog?, warningThreshold: Int): Int {
            if (current == null) return RESULT_NO_INTERPRETATION

            fun AirQualityLog.meetsThreshold() = airQualityIndex >= warningThreshold
            fun AirQualityLog.partialMeetsThreshold() = !hasIndex() && details.getHighestIndex() >= warningThreshold
            fun AirQualityLog.isLikelyOK() = errorCode == 0 && hasIndex() && !hasExpectedCoverage() && details.getHighestIndex() < warningThreshold

            if (previous == null) {
                if (current.isLikelyOK()) return RESULT_LIKELY_OK
                if (current.partialMeetsThreshold()) return RESULT_DEGRADED_PAST_THRESHOLD
                if (current.errorCode > 0) return interpretErrorCode(current.errorCode)
                if (current.airQualityIndex == -1) return RESULT_DATA_SHORTAGE_STARTED
                if (current.meetsThreshold()) return RESULT_DEGRADED_PAST_THRESHOLD

                return RESULT_NO_INTERPRETATION
            }

            if (current.errorCode > 0) {
                return if (previous.errorCode == 0) interpretErrorCode(current.errorCode)
                else RESULT_NO_INTERPRETATION
            }

            if(current.isLikelyOK() && !previous.isLikelyOK()) return RESULT_LIKELY_OK

            // special cases for poor data availability and bad index
            if (!previous.meetsThreshold()
                    && current.partialMeetsThreshold()
                    && !previous.partialMeetsThreshold()) return RESULT_DEGRADED_PAST_THRESHOLD

            if (current.meetsThreshold()
                    && !previous.hasIndex()
                    && previous.partialMeetsThreshold()) return RESULT_NO_INTERPRETATION

            if (!current.hasIndex() && previous.hasIndex()) return RESULT_DATA_SHORTAGE_STARTED
            if (current.hasExpectedCoverage() && !previous.hasExpectedCoverage() && !current.meetsThreshold()) return RESULT_OK_AFTER_SHORTAGE_ENDED

            if (current.meetsThreshold() && !previous.meetsThreshold()) return RESULT_DEGRADED_PAST_THRESHOLD
            if (!current.meetsThreshold() && previous.meetsThreshold()) return RESULT_IMPROVED_PAST_THRESHOLD

            return RESULT_NO_INTERPRETATION
        }

        private fun interpretErrorCode(errorCode: Int) =
                if (errorCode > 0) RESULT_ERROR_EMERGED else RESULT_NO_INTERPRETATION

        const val RESULT_NO_INTERPRETATION = 0
        const val RESULT_DEGRADED_PAST_THRESHOLD = 1
        const val RESULT_IMPROVED_PAST_THRESHOLD = 2
        const val RESULT_OK_AFTER_SHORTAGE_ENDED = 3
        const val RESULT_ERROR_EMERGED = 4
        const val RESULT_LIKELY_OK = 5
        const val RESULT_DATA_SHORTAGE_STARTED = 7
    }
}