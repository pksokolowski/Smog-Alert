package com.github.pksokolowski.smogalert

import com.github.pksokolowski.smogalert.api.models.SensorsModel
import com.github.pksokolowski.smogalert.utils.SensorsDataConverter
import org.junit.Assert.assertEquals
import org.junit.Test

class SensorsDataConverterTest {

    @Test
    fun extractsSensorsAvailabilityProperly() {
        val models = createModels("PM10", "O3", "NO2")
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(13, result)
    }

    @Test
    fun detectsAllWhenAllPresent() {
        val models = createModels("PM10", "PM2.5", "O3", "NO2", "SO2", "CO", "C6H6")
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(127, result)
    }

    @Test
    fun detectsAllWhenAllPresentInAnUnusualOrder() {
        val models = createModels("PM2.5", "C6H6", "O3", "CO", "NO2", "PM10", "SO2")
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(127, result)
    }

    @Test
    fun detectsPresentOnesDespiteMalformedAdditions() {
        val models = createModels("PM10", "PM7.5_INVALID", "O3", "NO2", "SO2", "CO", "C6H6")
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(125, result)
    }

    @Test
    fun detectsNothingWhenThereIsNothing() {
        val models = createModels()
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(0, result)
    }

    @Test
    fun detectsNothingWhenThereIsJustOneMalformedModel() {
        val models = createModels("MAL2.3")
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(0, result)
    }

    @Test
    fun dealsWithNullParamCodeGracefully(){
        val models = createModels("C6H6", null)
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(32, result)
    }

    @Test
    fun dealsWithNullParamsArrayGracefully(){
        val models = listOf(SensorsModel())
        val result = SensorsDataConverter.toSensorFlags(models)
        assertEquals(0, result)
    }

    private fun createModels(vararg paramCodes: String?): List<SensorsModel> {
        return List(paramCodes.size) {
            val code = paramCodes[it]
            val sensorsModel = SensorsModel().apply {
                param = SensorsModel.Param().apply { paramCode = code }
            }
            sensorsModel
        }
    }

//    private fun independentlyCalculateResult(vararg paramCodes: String): Int? {
//        var flags = 0
//        for (param in paramCodes) {
//            flags = flags or (codes[param] ?: 0)
//        }
//        return flags
//    }
//
//    companion object {
//        val codes = mapOf("PM10" to 1, "PM2.5" to 2, "O3" to 4, "NO2" to 8, "SO2" to 16, "C6H6" to 32, "CO" to 64)
//    }
}