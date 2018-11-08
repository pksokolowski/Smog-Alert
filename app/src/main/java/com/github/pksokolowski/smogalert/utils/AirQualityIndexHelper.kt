package com.github.pksokolowski.smogalert.utils

import android.app.Application
import android.content.Context
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.di.PerApp
import javax.inject.Inject

@PerApp
class AirQualityIndexHelper @Inject constructor(private val seasonalKeyPollutantsHelper: SeasonalKeyPollutantsHelper,
                                                private val context: Application) {
    fun getTitle(log: AirQualityLog): String {
        val index = log.airQualityIndex
        return getTitle(index, context)
    }

    fun getColor(log: AirQualityLog): Int {
        val coversKeyPollutantsIfExpected = seasonalKeyPollutantsHelper.coversKeyPollutantsIfExpected(log)
        val index = if (coversKeyPollutantsIfExpected) log.details.getHighestIndex() else log.airQualityIndex
        return getColor(index, context)
    }

    companion object {
        fun getTitle(index: Int, context: Context): String {
            val titles = context.resources.getStringArray(R.array.index_level_titles)
            if (index !in titles.indices) return titles.last()
            return titles[index]
        }

        fun getColor(index: Int, context: Context): Int {
            val colorsArray = context.resources.getIntArray(R.array.air_quality_index_colors)
            if (index !in colorsArray.indices) return colorsArray.last()
            return colorsArray[index]
        }
    }
}