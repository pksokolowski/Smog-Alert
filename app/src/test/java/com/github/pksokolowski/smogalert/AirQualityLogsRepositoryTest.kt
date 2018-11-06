package com.github.pksokolowski.smogalert

import android.arch.lifecycle.LiveData
import android.location.Location
import com.github.pksokolowski.smogalert.api.AirQualityService
import com.github.pksokolowski.smogalert.api.models.AirQualityModel
import com.github.pksokolowski.smogalert.db.AirQualityLog
import com.github.pksokolowski.smogalert.db.AirQualityLogsDao
import com.github.pksokolowski.smogalert.db.PollutionDetails
import com.github.pksokolowski.smogalert.db.Station
import com.github.pksokolowski.smogalert.location.LocationHelper
import com.github.pksokolowski.smogalert.repository.AirQualityLogsRepository
import com.github.pksokolowski.smogalert.repository.StationsRepository
import com.github.pksokolowski.smogalert.utils.InternetConnectionChecker
import com.github.pksokolowski.smogalert.utils.SeasonalKeyPollutantsHelper
import com.github.pksokolowski.smogalert.utils.SensorsPresence
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_C6H6
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_CO
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_NO2
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_O3
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM10
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_PM25
import com.github.pksokolowski.smogalert.utils.SensorsPresence.Companion.FLAG_SENSOR_SO2
import org.junit.Assert.assertEquals
import okhttp3.Request
import org.junit.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.anyLong
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AirQualityLogsRepositoryTest {

    @Test
    fun fetchesASimpleLog() {
        val pack = MockPack(mapOf(
                Station(1, FLAG_SENSOR_O3 or FLAG_SENSOR_NO2, 50.0, 20.0) to makeLog(9912999)
        ))

        val freshLog = pack.airQualityLogsRepo.getLatestLogData().log
        assertEquals("cannot fetch basic result", 2, freshLog.airQualityIndex)
    }

    @Test
    fun fetchesSensorsAvailabilityDataCorrectly() {
        val pack = MockPack(
                mapOf(
                        Station(1, 0, 50.0, 20.0) to makeLog(9919994)
                )
                ,
                stationSensorsById = mapOf(
                        1L to (FLAG_SENSOR_O3 or FLAG_SENSOR_CO)
                ))

        val freshLog = pack.airQualityLogsRepo.getLatestLogData().log
        assertEquals("cannot fetch sensors availability data", 4, freshLog.airQualityIndex)
    }

    @Test
    fun combinesDataFromMultipleStationsToFillPartialShortagesOnPreviousStations() {
        val pack = MockPack(
                mapOf(
                        // note: sensorFlags = 127 means that all sensors' data should be present.
                        // it, however, isn't thus it's a partial shortage and the repo must use
                        // all of these stations to fill all the gaps.
                        Station(1, 127, 50.0, 20.001) to makeLog(2299999),
                        Station(2, 127, 50.0, 20.002) to makeLog(3399922),
                        Station(3, 127, 50.0, 20.003) to makeLog(9922299)
                )
        )
        val log = pack.airQualityLogsRepo.getLatestLogData().log
        assertEquals("incorrect fusion of stations", 2222222, log.details.encode())
    }

    @Test
    fun usesNewlyMadeAvailableSensorsEvenBeforeCacheIsUpdatedToIncludeThem() {
        val pack = MockPack(
                mapOf(
                        Station(1, FLAG_SENSOR_SO2 or FLAG_SENSOR_C6H6, 50.0, 20.001) to makeLog(9999000),
                        Station(2, FLAG_SENSOR_CO, 50.0, 20.002) to makeLog(9999993)
                )
        )
        val log = pack.airQualityLogsRepo.getLatestLogData().log
        assertEquals("Used result from the second station whereas it should have been done after the first one", 9999000, log.details.encode())
    }

    @Test
    fun detectsFullDataShortage() {
        val pack = MockPack(mapOf(
                Station(1, FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25, 50.0, 20.0) to makeLog(9999999)
        ))

        val freshLog = pack.airQualityLogsRepo.getLatestLogData().log
        assertEquals("cannot detect a full data shortage", -1, freshLog.airQualityIndex)
    }

    @Test
    fun detectsPartialDataShortage() {
        val pack = MockPack(mapOf(
                Station(1, FLAG_SENSOR_PM10 or FLAG_SENSOR_PM25 or FLAG_SENSOR_O3, 50.0, 20.0) to makeLog(2919999)
        ))

        val freshLog = pack.airQualityLogsRepo.getLatestLogData().log
        assertEquals("cannot detect a partial data shortage", -1, freshLog.airQualityIndex)
    }

    /**
     * takes encodedPollutionDetails Int and transforms it into pollution details. The encoded
     * Int format is comprised of 7 digits, each represents level of a subsequent pollutant/subindex
     * The order is typical, same as in PollutionDetails class.
     * 9 stands for no data or -1 index value
     *
     * For example 9000000 means that PM10 level is -1 and all the others are equal to 0.
     */
    private fun makeLog(encodedPollutionDetails: Int): AirQualityModel {
        val details = PollutionDetails(encodedPollutionDetails)
        val levelValues = details.getDetailsArray()
        val indexLevels = Array(levelValues.size) {
            getIndexLevel(levelValues[it])
        }
        return AirQualityModel().apply {
            indexLevel = getIndexLevel(details.getHighestIndex())
            pm10 = indexLevels[0]
            pm25 = indexLevels[1]
            o3 = indexLevels[2]
            no2 = indexLevels[3]
            so2 = indexLevels[4]
            c6h6 = indexLevels[5]
            co = indexLevels[6]
        }
    }

    private fun getIndexLevel(level: Int): AirQualityModel.IndexLevel {
        val indexLevel = AirQualityModel.IndexLevel()
        indexLevel.value = level
        return indexLevel
    }

    private class MockPack(
            val stationModelMap: Map<Station, AirQualityModel>,
            cachedAQLogs: MutableList<AirQualityLog> = mutableListOf(),
            deviceLatitude: Double = 50.0,
            deviceLongitude: Double = 20.0,
            val stationSensorsById: Map<Long, Int> = stationModelMap.keys.map { it.id to 127 }.toMap(),
            var locationResult: LocationHelper.LocationResult = LocationHelper.LocationResult(Location("loc").apply { latitude = deviceLatitude; longitude = deviceLongitude }, LocationHelper.SUCCESS, false),
            var netAvailable: Boolean = true,
            val seasonalKeyPollutantFlags: Int = 0) {

        val airQualityLogsRepo: AirQualityLogsRepository

        init {
            val dao = AirQualityLogsDaoMock(cachedAQLogs)
            val service = AirQualityServiceMock(stationModelMap)
            val netChecker = getInternetConnectionCheckerMock()
            val seasonalHelper = getSeasonalHelperMock()
            val locationHelper = getLocationHelperMock()
            val stationsRepo = getStationsRepoMock()
            airQualityLogsRepo = AirQualityLogsRepository(dao, service, stationsRepo, locationHelper, netChecker, seasonalHelper)
        }

        // fix for Mockito's bug with Kotlin's null safety
        // this replaces argument matcher "any()"
        private fun <T> anything(): T {
            Mockito.any<T>()
            return uninitialized()
        }
        private fun <T> uninitialized(): T = null as T

        private fun getSeasonalHelperMock(): SeasonalKeyPollutantsHelper {
            val mock = Mockito.mock(SeasonalKeyPollutantsHelper::class.java)
            `when`(mock.includeKeyPollutants(anything(), anyLong())).then {
                val gainedCoverage = it.arguments[0] as SensorsPresence
                gainedCoverage.combinedWith(seasonalKeyPollutantFlags)
            }
            return mock
        }

        private fun getInternetConnectionCheckerMock(): InternetConnectionChecker {
            val mock = Mockito.mock(InternetConnectionChecker::class.java)
            `when`(mock.isConnectionAvailable()).thenReturn(netAvailable)
            return mock
        }

        private fun getLocationHelperMock(): LocationHelper {
            val mock = Mockito.mock(LocationHelper::class.java)
            `when`(mock.getLastLocationData()).thenReturn(locationResult)
            return mock
        }

        private fun getStationsRepoMock(): StationsRepository {
            val stations = stationModelMap.keys.toList()
            val mock = Mockito.mock(StationsRepository::class.java)
            `when`(mock.getStations()).thenReturn(stations)
            `when`(mock.fetchSensorsData(anyLong())).thenAnswer { invocation -> fetchStationWithSensors(invocation.arguments[0] as Long) }
            return mock
        }

        private fun fetchStationWithSensors(id: Long): Station? {
            val station = stationModelMap.keys.filter { it.id == id }.getOrElse(0) { return null }
            val sensors = stationSensorsById[station.id]
                    ?: throw RuntimeException("requested sensors for station which did not have any assigned")
            return station.assignSensors(sensors)
        }

        private class AirQualityServiceMock(var pairs: Map<Station, AirQualityModel>) : AirQualityService {
            override fun getCurrentAQ(stationId: Long?): Call<AirQualityModel> {
                if (stationId == null) throw RuntimeException("asked for a non-existent station (stationId were null)")
                //find station with id
                val model = pairs.filter { it.key.id == stationId }.values.toTypedArray().getOrNull(0)
                        ?: throw RuntimeException("asked for a non-existent station")
                return CallMock(model)
            }

            class CallMock(private val logModel: AirQualityModel) : Call<AirQualityModel> {
                override fun isExecuted(): Boolean {
                    throw RuntimeException("unimplemented method in CallMock class")
                }

                override fun clone(): Call<AirQualityModel> {
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

                override fun enqueue(callback: Callback<AirQualityModel>) {
                    throw RuntimeException("unimplemented method in CallMock class")
                }

                override fun execute(): Response<AirQualityModel> {
                    return Response.success(logModel)
                }
            }
        }

        private class AirQualityLogsDaoMock(var logs: MutableList<AirQualityLog>) : AirQualityLogsDao {
            override fun getLatestAirQualityLog(): AirQualityLog? {
                return logs.lastOrNull()
            }

            override fun getLatestCachedAirQualityLog(): LiveData<AirQualityLog?> {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun getNLatestLogs(n: Int): List<AirQualityLog> {
                return logs.subList(logs.size - n, logs.size)
            }

            override fun insertAirQualityLog(log: AirQualityLog): Long {
                val id = (logs.lastOrNull()?.id ?: 0) + 1
                logs.add(log.assignId(id))
                return id
            }
        }
    }
}