package github.umer0586.sensorserver.util

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager

class SensorUtil private constructor(context: Context?)
{


    private val sensorManager: SensorManager

    init
    {
        sensorManager = context!!.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    }

    val availableSensors: List<Sensor>
        get() = sensorManager.getSensorList(Sensor.TYPE_ALL)

    fun getSensorFromStringType(sensorStringType: String?): Sensor?
    {
        for (availableSensor in availableSensors) if (availableSensor.stringType.equals(
                sensorStringType,
                ignoreCase = true
            )
        ) return availableSensor
        return null
    }

    companion object
    {


        private var utilInstance: SensorUtil? = null

        @kotlin.jvm.Synchronized
        fun getInstance(context: Context?): SensorUtil?
        {
            if (utilInstance == null) utilInstance = SensorUtil(context)
            return utilInstance
        }
    }
}