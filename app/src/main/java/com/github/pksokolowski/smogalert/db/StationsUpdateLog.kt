package com.github.pksokolowski.smogalert.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stations_update_logs")
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