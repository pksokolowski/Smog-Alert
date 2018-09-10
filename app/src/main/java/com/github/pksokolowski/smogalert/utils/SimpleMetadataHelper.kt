package com.github.pksokolowski.smogalert.utils

import android.app.Application
import android.content.Context.MODE_PRIVATE

class SimpleMetadataHelper(appContext: Application, filePath: String) : ICacheMetadataHelper {
    private val sharedPrefs = appContext.getSharedPreferences(filePath, MODE_PRIVATE)

    override fun getPlannedCacheUpdateTime() = sharedPrefs.getLong(KEY_PLANNED_UPDATE, 0)

    override fun setNextUpdateTime(timeStamp: Long) {
        val editor = sharedPrefs.edit()
        editor.putLong(KEY_PLANNED_UPDATE, timeStamp)
        editor.apply()
    }

    override fun readFailedUpdatesCount() = sharedPrefs.getInt(KEY_UPDATE_FAILURES_COUNT, 0)

    override fun incrementFailedUpdatesCount() {
        val count = sharedPrefs.getInt(KEY_UPDATE_FAILURES_COUNT, 0)
        val editor = sharedPrefs.edit()
        editor.putInt(KEY_UPDATE_FAILURES_COUNT, count + 1)
        editor.apply()
    }

    private companion object {
        const val KEY_PLANNED_UPDATE = "next_cache_update"
        const val KEY_UPDATE_FAILURES_COUNT = "update_failures_count"
    }
}