package github.umer0586.sensorserver;

import android.hardware.Sensor;

public class ConnectionInfo {

    private Sensor sensor;
    private int sensorUsageCount;

    public ConnectionInfo(Sensor sensor, int sensorUsageCount)
    {
        this.sensor = sensor;
        this.sensorUsageCount = sensorUsageCount;
    }

    public Sensor getSensor()
    {
        return sensor;
    }

    public int getSensorUsageCount()
    {
        return sensorUsageCount;
    }
}
