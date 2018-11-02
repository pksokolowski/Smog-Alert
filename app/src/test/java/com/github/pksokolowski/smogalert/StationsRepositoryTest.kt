package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.api.SensorsService
import com.github.pksokolowski.smogalert.api.StationsService
import com.github.pksokolowski.smogalert.api.models.SensorsModel
import com.github.pksokolowski.smogalert.api.models.StationModel
import com.github.pksokolowski.smogalert.db.Station
import com.github.pksokolowski.smogalert.db.StationsDao
import com.github.pksokolowski.smogalert.db.StationsUpdateLog
import com.github.pksokolowski.smogalert.db.StationsUpdateLog.Companion.STATUS_FAILURE_POSTPONED
import com.github.pksokolowski.smogalert.db.StationsUpdateLog.Companion.STATUS_SUCCESS
import com.github.pksokolowski.smogalert.db.StationsUpdateLogsDao
import com.github.pksokolowski.smogalert.repository.StationsRepository
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import okhttp3.Request
import org.junit.Assert.*
import org.junit.Test
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException
import java.util.*

class StationsRepositoryTest {

    @Test
    fun doesNotUpdateCachePrematurely() {
        val data = prepareFreshRepository(
                getSampleStationsList(75),
                getSampleStationsList(100).randomlySwapItems(10),
                listOf(
                        StationsUpdateLog(1, STATUS_SUCCESS, 0),
                        StationsUpdateLog(2, STATUS_SUCCESS, 100),
                        StationsUpdateLog(3, STATUS_SUCCESS, Calendar.getInstance().timeInMillis))
        )

        // if it chooses to update the cache, it has to reach out to the API, which indicates failure
        data.repo.getStations()

        if (data.service.mStationsRequestsCount > 0) fail("updated cache when it didn\'t have to.")
    }

    @Test
    fun doesUpdateCacheWhenTooOld() {
        val day = 86400000L /* a day in milliseconds */
        val oldCacheTimeStamp = Calendar.getInstance().timeInMillis - 40 * day
        val data = prepareFreshRepository(
                getSampleStationsList(75),
                getSampleStationsList(100).randomlySwapItems(10),
                listOf(
                        StationsUpdateLog(1, STATUS_SUCCESS, 0),
                        StationsUpdateLog(2, STATUS_SUCCESS, 100),
                        StationsUpdateLog(3, STATUS_SUCCESS, oldCacheTimeStamp))
        )

        data.repo.getStations()

        with(data.service.mStationsRequestsCount) {
            if (this == 0) fail("didn't update cache despite it being too old")
            if (this > 1) fail("sent more than one api request for an up to date stations list")
        }
    }

    @Test
    fun rejectsApiResponseWhenNoStationsAreReturned() {
        val data = prepareFreshRepository(
                getSampleStationsList(),
                getSampleStationsList(0)
        )

        val stations = data.repo.getStations()
        assertEquals(0, data.dao.mDeletedStationsCount)
        assertEquals(0, data.dao.mInsertedStationsCount)
        assertEquals(0, data.dao.mUpdatedStationsCount)
        val lastLog = data.stationsUpdateLogsDao.getLastLog()
        if (lastLog == null
                || lastLog.id != 1L
                || lastLog.status != STATUS_FAILURE_POSTPONED) fail("log of a failed update was incorrect")
    }

    @Test
    fun doesNothingWhenCacheAndApiVersionsAreSame() {
        val data = prepareFreshRepository(
                getSampleStationsList().randomlySwapItems(13, 47),
                getSampleStationsList().randomlySwapItems(30, 55)
        )

        val stations = data.repo.getStations()
        assertArrayEquals(data.cached.toTypedArray(), stations.toTypedArray())
        assertEquals(0, data.dao.mDeletedStationsCount)
        assertEquals(0, data.dao.mInsertedStationsCount)
        assertEquals(0, data.dao.mUpdatedStationsCount)
    }

    @Test
    fun updatesCacheErasingOldSensorsData() {
        val cached = getSampleStationsList().randomlySwapItems(13, 47)
        val api = getSampleStationsList().randomlySwapItems(30, 55)

        // assign some random sensor to see if it's erased corectly
        cached[2] = cached[2].assignSensors(127)

        val data = prepareFreshRepository(cached, api)

        val stations = data.repo.getStations()
        assertEquals(0, data.dao.mDeletedStationsCount)
        assertEquals(0, data.dao.mInsertedStationsCount)
        assertEquals(1, data.dao.mUpdatedStationsCount)

        for (s in stations) {
            if (s.sensorFlags != 0) fail("no stations should have sensor data after a cache update")
        }
    }

    @Test
    fun returnsFullUpdatedListAfterCacheUpdateWhereOneStationWentAbsent() {
        val cached = listOf(
                Station(550, 31, 50.0, 20.001),
                Station(530, 107, 50.0, 20.002)
        )
        val api = listOf(
                Station(530, 107, 50.0, 20.002)
        )

        val data = prepareFreshRepository(cached, api)

        val stations = data.repo.getStations()
        assertEquals(2, stations.size)
    }

    @Test
    fun addsNewStationCorrectly() {
        val size = 151

        val api = getSampleStationsList(size + 1)
        val theAddedStation = api.last()

        val data = prepareFreshRepository(
                getSampleStationsList(size).randomlySwapItems(40, 1),
                api.randomlySwapItems(60, 2)
        )

        val stations = data.repo.getStations()

        val count = stations.count { it == theAddedStation }
        assertEquals("the new station was added exactly once, without duplication etc", 1, count)
        assertEquals(0, data.dao.mDeletedStationsCount)
        assertEquals(1, data.dao.mInsertedStationsCount)
        assertEquals(0, data.dao.mUpdatedStationsCount)
    }

    @Test
    fun removesStationsCorrectly() {
        val size = 151
        val deletedItemsCount = 5
        // prepare deletions
        val sample = getSampleStationsList(size)
        for (i in 0 until deletedItemsCount) {
            val longAbsentStation = Station(1000L + i, 0, 50.0, 20.0, EXPECTED_MAX_ABSENCE_COUNT_BEFORE_DELETION)
            sample.add(longAbsentStation)
        }

        val data = prepareFreshRepository(
                sample.randomlySwapItems(40, 1),
                getSampleStationsList(size).randomlySwapItems(60, 2)
        )

        val stations = data.repo.getStations()

        assertEquals(5, data.dao.mDeletedStationsCount)
        assertEquals(0, data.dao.mInsertedStationsCount)
        assertEquals(0, data.dao.mUpdatedStationsCount)

        assertEquals("should have used api service", 1, data.service.mStationsRequestsCount)
    }

    @Test
    fun removesStationsCorrectlyButNotTheOnesOnlyRecentlyAbsent() {
        val size = 151
        val deletedItemsCount = 5
        val temporarilyAbsentItemsCount = 4
        // prepare deletions
        val sample = getSampleStationsList(size)
        for (i in 0 until deletedItemsCount) {
            val longAbsentStation = Station(1000L + i, 0, 50.0, 20.0, EXPECTED_MAX_ABSENCE_COUNT_BEFORE_DELETION)
            sample.add(longAbsentStation)
        }
        for (i in 0 until temporarilyAbsentItemsCount) {
            val longAbsentStation = Station(1000L + i, 0, 50.0, 20.0, EXPECTED_MAX_ABSENCE_COUNT_BEFORE_DELETION - 1)
            sample.add(longAbsentStation)
        }

        val data = prepareFreshRepository(
                sample.randomlySwapItems(40, 1),
                getSampleStationsList(size).randomlySwapItems(60, 2)
        )

        val stations = data.repo.getStations()

        assertEquals(deletedItemsCount, data.dao.mDeletedStationsCount)
        assertEquals(0, data.dao.mInsertedStationsCount)
        assertEquals(temporarilyAbsentItemsCount, data.dao.mUpdatedStationsCount)

        assertEquals("should have used api service", 1, data.service.mStationsRequestsCount)
    }

    @Test
    fun combinesAddRemoveAndUpdateCorrectly() {
        val size = 151
        val cached = getSampleStationsList(size)
        for (i in 0 until 15) {
            val longAbsentStation = Station(1000L + i, 0, 50.0, 20.0, EXPECTED_MAX_ABSENCE_COUNT_BEFORE_DELETION)
            cached.add(longAbsentStation)
        }
        val api = getSampleStationsList(size)

        //add new stations
        api.addAll(listOf(Station(405, 0, 47.123, 19.321),
                Station(550, 0, 51.123, 21.321)))

        // modify stations
        api[32] = Station(api[32].id, 0, 51.53, 20.21)

        val data = prepareFreshRepository(cached, api)

        val stations = data.repo.getStations()

        assertArrayEquals(api.toTypedArray(), stations.toTypedArray())
        assertEquals(15, data.dao.mDeletedStationsCount)
        assertEquals(2, data.dao.mInsertedStationsCount)
        assertEquals(1, data.dao.mUpdatedStationsCount)

        assertEquals("should have used api service", 1, data.service.mStationsRequestsCount)
    }

    @Test
    fun usesCacheInsteadWhenItsFresh() {
        val data = prepareFreshRepository(
                getSampleStationsList(),
                getSampleStationsList()
        )

        data.repo.getStations()
        assertEquals("should have used api service", 1, data.service.mStationsRequestsCount)
        assertEquals("should have used cache twice by now, for comparison and in the end of getStations()", 2, data.dao.mStationsRequestsCount)

        // second call should use cache instead of API, because cache is presumably up to date now.
        // this means 3 uses of cache, because it was used twice in the previous case
        data.repo.getStations()
        assertEquals("should not use API secondTime!", 1, data.service.mStationsRequestsCount)
        assertEquals("should have used cache", 3, data.dao.mStationsRequestsCount)

    }

    @Test
    fun retrievesCorrectSensorsForStations() {
        val stations = getSampleStationsList(100, 3)
        val repo = prepareFreshRepository(stations, stations).repo
        for (s in stations) {
            val sensorFlags = repo.fetchSensorsData(s.id)?.sensorFlags
            val expectedValueSource = SensorsServiceMock.getExpectedValuePerStationId(s.id)
            if (sensorFlags == null || !expectedValueSource.hasSensors(sensorFlags)) fail("sensors retrieved are not correct")
        }
    }

    private fun getSampleStationsList(n: Int = 100, seed: Long = 0): MutableList<Station> {
        val random = Random(seed)
        fun next() = (random.nextDouble() * 5) - 2.5
        return MutableList(n) {
            val latitude = 50.0 + next()
            val longitude = 20 + next()
            Station(it + 1L, 0, latitude, longitude)
        }
    }

    private fun MutableList<Station>.randomlySwapItems(times: Int, seed: Long = 0): MutableList<Station> {
        val random = Random(seed)
        for (i in 1..times) {
            val A = random.nextInt(this.size)
            val B = random.nextInt(this.size)
            //swap
            val item = this[A]
            this[A] = this[B]
            this[B] = item
        }
        return this
    }

    /**
     * A shortcut method to get a ready to use repository object with a given state of cache and
     * API data available 'online', as well as metadata, which has default values provided already.
     * With the defaults the repo will always update cache. Set plannedNextUpdate parameter to
     * Long.MAX_VALUE to prevent any cache updates.
     *
     * Returned object also contains the original data fed to the mock objects.
     */
    private fun prepareFreshRepository(cached: List<Station>,
                                       online: List<Station>,
                                       updateLogs: List<StationsUpdateLog> = listOf()): RepoData {
        val dao = StationsDaoMock(cached)
        val service = StationsServiceMock(online)
        val logs = StationsUpdateLogsDaoMock(updateLogs)

        val repo = StationsRepository(dao, service, SensorsServiceMock(), logs)
        return RepoData(repo, dao, service, logs, cached, online, updateLogs)
    }

    private class RepoData(val repo: StationsRepository,
                           val dao: StationsDaoMock,
                           val service: StationsServiceMock,
                           val stationsUpdateLogsDao: StationsUpdateLogsDaoMock,
                           val cached: List<Station>,
                           val online: List<Station>,
                           val updateLogs: List<StationsUpdateLog>
    )

    private class StationsUpdateLogsDaoMock(logs: List<StationsUpdateLog>) : StationsUpdateLogsDao {
        val mLogs = logs.toMutableList()
        override fun getLastLog(): StationsUpdateLog? {
            return mLogs.lastOrNull()
        }

        override fun insertLog(log: StationsUpdateLog) {
            mLogs.add(StationsUpdateLog(mLogs.size + 1L, log.status, log.timeStamp))
        }
    }

    private class StationsDaoMock(stations: List<Station>) : StationsDao {
        override fun getStationById(id: Long): Station? {
            for (s in mStations) {
                if (s.id == id) return s
            }
            return null
        }

        override fun updateStation(station: Station) {
            for (i in mStations.indices) {
                if (mStations[i].id == station.id) mStations[i] = station
            }
        }

        val mStations = stations.toMutableList()
        var mStationsRequestsCount = 0
        var mDeletedStationsCount = 0
        var mInsertedStationsCount = 0
        var mUpdatedStationsCount = 0

        override fun getStations(): List<Station> {
            mStationsRequestsCount++
            return mStations.toList()
        }

        override fun insertStations(stations: List<Station>) {
            mInsertedStationsCount += stations.size
            mStations.addAll(stations)
        }

        override fun updateStations(stations: List<Station>) {
            mUpdatedStationsCount += stations.size
            val updated = stations.map { it.id to it }.toMap()

            for (i in mStations.indices) {
                val it = mStations[i]
                val updatedVersion = updated[it.id]
                if (updatedVersion != null) {
                    mStations[i] = updatedVersion
                }
            }

        }

        override fun deleteStations(stations: List<Station>) {
            mDeletedStationsCount += stations.size
            mStations.removeAll(stations)
        }
    }

    private class StationsServiceMock(private val stations: List<Station>) : StationsService {
        var mStationsRequestsCount = 0

        override fun getStations(): Call<List<StationModel>> {
            mStationsRequestsCount += 1
            val stationModels = List(stations.size) {
                val stationModel = StationModel()
                with(stations[it]) {
                    stationModel.id = id.toInt()
                    stationModel.latitude = latitude.toString()
                    stationModel.longitude = longitude.toString()
                }
                stationModel
            }
            return CallMock(stationModels)
        }

        class CallMock(private val stations: List<StationModel>) : Call<List<StationModel>> {
            override fun isExecuted(): Boolean {
                throw RuntimeException("unimplemented method in CallMock class")
            }

            override fun clone(): Call<List<StationModel>> {
                throw RuntimeException("unimplemented method in CallMock class")
            }

            override fun isCanceled(): Boolean {
                throw RuntimeException("unimplemented method in CallMock class")
            }

            override fun cancel() {
                throw RuntimeException("unimplemented method in CallMock class")
            }

            override fun request(): Request {
                throw RuntimeException("unimplemented method in CallMock class")
            }

            override fun enqueue(callback: Callback<List<StationModel>>) {
                throw RuntimeException("unimplemented method in CallMock class")
            }

            override fun execute(): Response<List<StationModel>> {
                return Response.success(stations)
            }
        }
    }

    private class SensorsServiceMock : SensorsService {
        override fun getSensors(stationId: Long?): Call<List<SensorsModel>> {
            val stationId = stationId ?: 0
            val sensorsPresence = getExpectedValuePerStationId(stationId)
            val sensorList = mutableListOf<SensorsModel>()
            for (s in SENSORS) {
                if (sensorsPresence.hasSensors(s)) {
                    val model = SensorsModel()
                    model.id = stationId.toInt() + 5
                    model.stationId = stationId.toInt()
                    model.param = SensorsModel.Param().apply {
                        paramCode = SENSOR_FLAG_TO_CODE[s]
                        paramFormula = SENSOR_FLAG_TO_CODE[s]
                        paramName = SENSOR_FLAG_TO_CODE[s] + " param name"
                        idParam = SENSOR_FLAG_TO_PARAM_ID[s]
                    }
                    sensorList.add(model)
                }
            }
            return SensorsCallMock(sensorList)
        }

        companion object {
            fun getExpectedValuePerStationId(id: Long): SensorsPresence {
                val rand = Random(id)
                var flags = 0
                for (s in SENSORS) {
                    if (rand.nextInt(10) > 4) continue
                    flags = flags or s
                }
                return SensorsPresence(flags)
            }
        }

        class SensorsCallMock(private val sensors: List<SensorsModel>) : Call<List<SensorsModel>> {
            override fun isExecuted(): Boolean {
                throw RuntimeException("unimplemented method in SensorsCallMock class")
            }

            override fun clone(): Call<List<SensorsModel>> {
                throw RuntimeException("unimplemented method in SensorsCallMock class")
            }

            override fun isCanceled(): Boolean {
                throw RuntimeException("unimplemented method in SensorsCallMock class")
            }

            override fun cancel() {
                throw RuntimeException("unimplemented method in SensorsCallMock class")
            }

            override fun request(): Request {
                throw RuntimeException("unimplemented method in SensorsCallMock class")
            }

            override fun enqueue(callback: Callback<List<SensorsModel>>) {
                throw RuntimeException("unimplemented method in SensorsCallMock class")
            }

            override fun execute(): Response<List<SensorsModel>> {
                return Response.success(sensors)
            }
        }
    }

    companion object {
        val SENSORS = listOf(
                SensorsPresence.FLAG_SENSOR_PM10,
                SensorsPresence.FLAG_SENSOR_PM25,
                SensorsPresence.FLAG_SENSOR_O3,
                SensorsPresence.FLAG_SENSOR_NO2,
                SensorsPresence.FLAG_SENSOR_SO2,
                SensorsPresence.FLAG_SENSOR_C6H6,
                SensorsPresence.FLAG_SENSOR_CO
        )

        val SENSOR_FLAG_TO_CODE = mapOf(
                SensorsPresence.FLAG_SENSOR_PM10 to "PM10",
                SensorsPresence.FLAG_SENSOR_PM25 to "PM2.5",
                SensorsPresence.FLAG_SENSOR_O3 to "O3",
                SensorsPresence.FLAG_SENSOR_NO2 to "NO2",
                SensorsPresence.FLAG_SENSOR_SO2 to "SO2",
                SensorsPresence.FLAG_SENSOR_C6H6 to "C6H6",
                SensorsPresence.FLAG_SENSOR_CO to "CO"
        )

        val SENSOR_FLAG_TO_PARAM_ID = mapOf(
                SensorsPresence.FLAG_SENSOR_PM10 to 3,
                SensorsPresence.FLAG_SENSOR_PM25 to 69,
                SensorsPresence.FLAG_SENSOR_O3 to 5,
                SensorsPresence.FLAG_SENSOR_NO2 to 6,
                SensorsPresence.FLAG_SENSOR_SO2 to 1,
                SensorsPresence.FLAG_SENSOR_C6H6 to 10,
                SensorsPresence.FLAG_SENSOR_CO to 8
        )

        const val EXPECTED_MAX_ABSENCE_COUNT_BEFORE_DELETION = 6
    }

}

