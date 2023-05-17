package github.umer0586.sensorserver.util;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.List;

public class SensorUtil {

    private static SensorUtil utilInstance = null;
    private SensorManager sensorManager;

    private SensorUtil(Context context)
    {
        sensorManager = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
    }

    public static synchronized SensorUtil getInstance(Context context)
    {

        if(utilInstance == null)
            utilInstance  = new SensorUtil(context);

        return utilInstance;

    }

    public List<Sensor> getAvailableSensors()
    {
        return sensorManager.getSensorList(Sensor.TYPE_ALL);
    }

    public Sensor getSensorFromStringType(String sensorStringType)
    {

        for(Sensor availableSensor : getAvailableSensors())
            if(availableSensor.getStringType().equalsIgnoreCase(sensorStringType))
                return availableSensor;


         return null;
    }


}
