package com.github.pksokolowski.smogalert.utils

import android.content.Context
import com.github.pksokolowski.smogalert.R

class AirQualityIndexHelper {
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