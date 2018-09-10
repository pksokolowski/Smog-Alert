package com.github.pksokolowski.smogalert.utils

interface ICacheMetadataHelper {
    fun getPlannedCacheUpdateTime(): Long
    fun setNextUpdateTime(timeStamp: Long)
    fun readFailedUpdatesCount(): Int
    fun incrementFailedUpdatesCount()
}