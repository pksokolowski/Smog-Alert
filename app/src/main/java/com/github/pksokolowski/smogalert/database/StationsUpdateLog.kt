package com.github.pksokolowski.smogalert.database

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey

@Entity(tableName = "stations_update_log")
data class StationsUpdateLog(
        @PrimaryKey(autoGenerate = true)
        @ColumnInfo(name = "id")
        val id: Long = 0,

        @ColumnInfo(name = "status")
        val status: Int = -1,

        @ColumnInfo(name = "time_stamp")
        val timeStamp: Long
) {
        companion object {
            const val STATUS_SUCCESS = 0
            const val STATUS_FAILURE_POSTPONED = 1
            const val STATUS_FAILURE_RETRY_SOON = 2
        }
}