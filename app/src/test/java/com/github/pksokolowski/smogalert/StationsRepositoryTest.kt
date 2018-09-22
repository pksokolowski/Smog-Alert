package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.airquality.StationsService
import com.github.pksokolowski.smogalert.airquality.models.StationModel
import com.github.pksokolowski.smogalert.database.Station
import com.github.pksokolowski.smogalert.database.StationsDao
import com.github.pksokolowski.smogalert.repository.StationsRepository
import com.github.pksokolowski.smogalert.utils.ICacheMetadataHelper
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
    fun doesNothingWhenCacheAndApiVersionsAreSame() {
        val size = 151
        val seed = 0L
        val cached = getSampleStationsList(size, seed)
        val api = getSampleStationsList(size, seed)

        cached.randomlySwapItems(13, 47)
        api.randomlySwapItems(30, 55)

        val repoData = prepareFreshRepository(cached, api)
        val repo = repoData.repo


        val stations = repo.getStations()
        assertArrayEquals(api.toTypedArray(), stations.toTypedArray())
        assertEquals(0, repoData.dao.mDeletedStationsCount)
        assertEquals(0, repoData.dao.mInsertedStationsCount)
        assertEquals(0, repoData.dao.mUpdatedStationsCount)
    }

    @Test
    fun addsNewStationCorrectly(){
        val size = 151
        val seed = 0L
        val cached = getSampleStationsList(size, seed)
        val api = getSampleStationsList(size+1, seed)

        cached.randomlySwapItems(40, 1)
        api.randomlySwapItems(60, 2)

        val repoData = prepareFreshRepository(cached, api)
        val repo = repoData.repo

        val stations = repo.getStations()

        assertArrayEquals("should have returned the list from API", api.toTypedArray(), stations.toTypedArray())
        assertEquals(0, repoData.dao.mDeletedStationsCount)
        assertEquals(1, repoData.dao.mInsertedStationsCount)
        assertEquals(0, repoData.dao.mUpdatedStationsCount)
    }

    @Test
    fun removesStationsCorrectly(){
        val size = 151
        val seed = 0L
        val cached = getSampleStationsList(size, seed)
        val api = getSampleStationsList(size-5, seed)

        cached.randomlySwapItems(40, 1)
        api.randomlySwapItems(60, 2)

        val repoData = prepareFreshRepository(cached, api)
        val repo = repoData.repo

        val stations = repo.getStations()

        assertArrayEquals("should have returned the list from API", api.toTypedArray(), stations.toTypedArray())
        assertEquals(5, repoData.dao.mDeletedStationsCount)
        assertEquals(0, repoData.dao.mInsertedStationsCount)
        assertEquals(0, repoData.dao.mUpdatedStationsCount)

        assertEquals("should have used api service", 1, repoData.service.mStationsRequestsCount)
    }

    @Test
    fun combinesAddRemoveAndUpdateCorrectly(){
        val size = 151
        val seed = 0L
        val cached = getSampleStationsList(size, seed)
        val api = getSampleStationsList(size-15, seed)

        //add new stations
        api.addAll(listOf(Station(405, 47.123, 19.321),
                Station(550, 51.123, 21.321)))

        // modify stations
        api[32] = Station(api[32].id, 51.53, 20.21)

        cached.randomlySwapItems(40, 1)
        api.randomlySwapItems(60, 2)

        val repoData = prepareFreshRepository(cached, api)
        val repo = repoData.repo

        val stations = repo.getStations()

        assertArrayEquals("should have returned the list from API", api.toTypedArray(), stations.toTypedArray())
        assertEquals(15, repoData.dao.mDeletedStationsCount)
        assertEquals(2, repoData.dao.mInsertedStationsCount)
        assertEquals(1, repoData.dao.mUpdatedStationsCount)

        assertEquals("should have used api service", 1, repoData.service.mStationsRequestsCount)
    }

    @Test
    fun usesCacheInsteadWhenItsFresh(){
        val size = 151
        val seed = 0L
        val cached = getSampleStationsList(size, seed)
        val api = getSampleStationsList(size, seed)

        val repoData = prepareFreshRepository(cached, api)
        val repo = repoData.repo

        val stations = repo.getStations()
        assertEquals("should have used api service", 1, repoData.service.mStationsRequestsCount)
        assertEquals("should have used cache for comparison", 1, repoData.dao.mStationsRequestsCount)

        // second call should use cache instead of API, because cache is presumably up to date now.
        val stationsFromCache = repo.getStations()
        assertEquals("should not use API secondTime!", 1, repoData.service.mStationsRequestsCount)
        assertEquals("should have used cache", 2, repoData.dao.mStationsRequestsCount)

    }

    private fun getSampleStationsList(n: Int, seed: Long = 0) : MutableList<Station>{
        val random = Random(seed)
        fun next() = (random.nextDouble() * 5) - 2.5
        return MutableList(n){
            val latitude = 50.0 + next()
            val longitude = 20 + next()
            Station(it+1L, latitude, longitude)
        }
    }

    private fun MutableList<Station>.randomlySwapItems(times: Int, seed: Long){
        val random = Random(seed)
        for(i in 1..times){
            val A = random.nextInt(this.size)
            val B = random.nextInt(this.size)
            //swap
            val item = this[A]
            this[A] = this[B]
            this[B] = item
        }
    }

    /**
     * A shortcut method to get a ready to use repository object with a given state of cache and
     * API data available 'online', as well as metadata, which has default values provided already.
     * With the defaults the repo will always update cache. Set plannedNextUpdate parameter to
     * Long.MAX_VALUE to prevent any cache updates.
     */
    private fun prepareFreshRepository(cached: List<Station>,
                                       online: List<Station>,
                                       plannedNextUpdate: Long = 0,
                                       errorsCount: Int = 0): RepoData {
        val dao = StationsDaoMock(cached)
        val service = StationsServiceMock(online)
        val metadata = MetadataHelperMock(plannedNextUpdate, errorsCount)

        val repo = StationsRepository(dao, service, metadata)
        return RepoData(repo, dao, service, metadata)
    }

    private class RepoData(val repo: StationsRepository,
                           val dao: StationsDaoMock,
                           val service: StationsServiceMock,
                           val metadata: MetadataHelperMock)

    private class StationsDaoMock(stations: List<Station>) : StationsDao {
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

    private class MetadataHelperMock(private var plannedNextUpdate: Long,
                                     private var errorsCount: Int) : ICacheMetadataHelper {
        override fun getPlannedCacheUpdateTime() = plannedNextUpdate

        override fun setNextUpdateTime(timeStamp: Long) {
            plannedNextUpdate = timeStamp
        }

        override fun readFailedUpdatesCount() = errorsCount

        override fun incrementFailedUpdatesCount() {
            errorsCount++
        }

    }
}

