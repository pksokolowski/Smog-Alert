package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.airquality.models.StationModel
import com.github.pksokolowski.smogalert.database.Station
import com.github.pksokolowski.smogalert.utils.StationDataConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class StationDataConverterTest {

    @Test
    fun retainsDataThroughConversion() {
        val modelAndStation = createModelAndStation(531, "50.21212", "21.3234311")
        val retrievedStation = StationDataConverter.toStation(modelAndStation.model)

        assertEquals("data was malformed during conversion from StationModel to Station", modelAndStation.station, retrievedStation)
    }

    @Test
    fun returnsNullWhenIdIsNull(){
        val model = createModel(null, "50.21212", "21.3234311")
        val retrievedStation = StationDataConverter.toStation(model)

        assertEquals("did not return null when converting a model with missing id", null, retrievedStation)
    }

    @Test
    fun returnsNullWhenLatitudeIsNull(){
        val model = createModel(109, null, "19.3234311")
        val retrievedStation = StationDataConverter.toStation(model)

        assertEquals("did not return null when converting a model with missing latitude", null, retrievedStation)
    }

    @Test
    fun returnsNullWhenLongitudeIsNull(){
        val model = createModel(109, "50.21212", null)
        val retrievedStation = StationDataConverter.toStation(model)

        assertEquals("did not return null when converting a model with missing longitude", null, retrievedStation)
    }

    @Test
    fun returnsNullWhenModelCorrupt(){
        val model = createModel(123, "50.UnexpectedString1212", "40.323Degrees7")
        val retrievedStation = StationDataConverter.toStation(model)

        assertEquals("did not return null when converting a corrupt model", null, retrievedStation)
    }

    /**
     * creates a StationModel and Station objects based on the same data. The Station object created
     * here might be later compared with the Station object returned by the tested class's toStation()
     */
    private fun createModelAndStation(id: Int, latitude: String, longitude: String): ModelAndStation {
        val model = createModel(id, latitude, longitude)
        val station = Station(id.toLong(), 0, latitude.toDouble(), longitude.toDouble())
        return ModelAndStation(model, station)
    }
    private class ModelAndStation(val model: StationModel, val station: Station)

    private fun createModel(id: Int?, latitude: String?, longitude: String?): StationModel {
        val model = StationModel()
        model.id = id
        model.latitude = latitude
        model.longitude = longitude
        return model
    }
}