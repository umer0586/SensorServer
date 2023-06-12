package github.umer0586.sensorserver.customextensions

import android.hardware.Sensor
import android.hardware.SensorManager

fun SensorManager.getAvailableSensors() = getSensorList(Sensor.TYPE_ALL)

fun SensorManager.getSensorFromStringType(sensorStringType: String) : Sensor?
{
    return getAvailableSensors()
        .filter { it.stringType.equals(sensorStringType, ignoreCase = true) }
        .firstOrNull()

}
