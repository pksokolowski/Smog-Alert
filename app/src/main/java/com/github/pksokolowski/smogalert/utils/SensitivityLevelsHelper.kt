package com.github.pksokolowski.smogalert.utils

import android.content.Context
import com.github.pksokolowski.smogalert.R
import com.github.pksokolowski.smogalert.job.AirCheckParams

class SensitivityLevelsHelper{
    companion object {
        fun getTitle(index: Int, context: Context): String {
            val titles = context.resources.getStringArray(R.array.sensitivity_level_titles)
            if (index !in titles.indices) return titles.last()
            return titles[index]
        }

        fun explain(index: Int, context: Context): String {
            val titles = context.resources.getStringArray(R.array.sensitivity_level_descriptions)
            if (index !in titles.indices) return titles.last()
            return titles[index]
        }

        fun getColor(sensitivity: Int, context: Context): Int{
            if(sensitivity == 0) return context.getColor(R.color.color_sensitivity_zero)

            val warningLevel = AirCheckParams(sensitivity).getMinimumWarningIndexLevel()
            return AirQualityIndexHelper.getColor(warningLevel, context)
        }
    }
}