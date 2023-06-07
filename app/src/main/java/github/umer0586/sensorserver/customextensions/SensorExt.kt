package github.umer0586.sensorserver.customextensions

import android.hardware.Sensor

fun Sensor.detail(): String
{

    val reportingModeMapping = mapOf(
        Sensor.REPORTING_MODE_CONTINUOUS to "Continuous",
        Sensor.REPORTING_MODE_ON_CHANGE to "On Change",
        Sensor.REPORTING_MODE_ONE_SHOT to "One Shot",
        Sensor.REPORTING_MODE_SPECIAL_TRIGGER to "Special Trigger",
    )


    return """
    Name : $name
    MinDelay : ${minDelay}μs
    MaxDelay : ${maxDelay}μs 
    MaxRange : $maximumRange
    Resolution : $resolution
    Reporting Mode : ${if (reportingModeMapping.containsKey(reportingMode)) reportingModeMapping[reportingMode] else "Unknown"}
    Power : ${power}mA
    Vendor : $vendor
    Version : $version
    WakeUp sensor : $isWakeUpSensor        
   
    """.trimIndent()
}