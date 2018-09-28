package com.github.pksokolowski.smogalert

import android.arch.persistence.db.SimpleSQLiteQuery
import android.arch.persistence.room.Room
import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.github.pksokolowski.smogalert.database.*
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CreateTriggersCallbackTest {

    @Test
    fun deletesOldAirQualityLogs() {
        val db = prepareNewDataBase()
        val dao = db.airQualityLogsDao()
        val logs = List(100) { AirQualityLog(timeStamp = it.toLong()) }
        val logsWithIds = mutableListOf<AirQualityLog>()
        for (log in logs) {
            val id = dao.insertAirQualityLog(log)
            logsWithIds.add(log.assignId(id))
        }

        val retrievedLogs = dao.getNLatestLogs(logs.size)
        assertEquals("db contained more logs than allowed", 2, retrievedLogs.size)

        for (i in 0 until retrievedLogs.size) {
            // notice that the retrievedLogs are in reverse order, the latest is the first.
            val logRetrieved = retrievedLogs[i]
            val logExpected = logsWithIds[logsWithIds.lastIndex - i]

            if (logRetrieved.id != logExpected.id || logRetrieved.timeStamp != logExpected.timeStamp)
                fail("Wrong AirQualityLog was returned by dao.")
        }
    }

    @Test
    fun deletesOldStationsUpdateLogs() {
        val db = prepareNewDataBase()
        val dao = db.stationsUpdateLogsDao()
        val logs = Array(100) { StationsUpdateLog(status = 0, timeStamp = it.toLong()) }
        for (log in logs) {
            dao.insertLog(log)
        }
        val latestLogAsExpected = StationsUpdateLog(logs.size.toLong(), 0, logs.size -1L)

        val latestLog = dao.getLastLog()
        val numberOfLogsInDb = db.query(SimpleSQLiteQuery("SELECT * FROM stations_update_logs")).count
        assertEquals("db contained more logs than allowed", 1, numberOfLogsInDb)
        if(latestLog != latestLogAsExpected) fail("stationsUpdateLog returned was not the latest log!")
    }

    private fun prepareNewDataBase(): AppDatabase {
        val app = InstrumentationRegistry.getTargetContext()
        InstrumentationRegistry.getTargetContext().deleteDatabase(TEST_DB_NAME)
        return Room
                .databaseBuilder(app, AppDatabase::class.java, TEST_DB_NAME)
                .fallbackToDestructiveMigration()
                .addCallback(CreateTriggersCallback)
                .allowMainThreadQueries()
                .build()
    }

    private companion object {
        const val TEST_DB_NAME = "testdb"
    }
}