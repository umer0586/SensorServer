package github.umer0586.sensorserver;

import android.hardware.Sensor;

import java.net.InetSocketAddress;
import java.util.List;

public class ConnectionInfo {

    private Sensor sensor;
    private int sensorUsageCount;
    private List<InetSocketAddress> connectedClients;

    public ConnectionInfo(Sensor sensor, int sensorUsageCount, List<InetSocketAddress> connectedClients)
    {
        this.sensor = sensor;
        this.sensorUsageCount = sensorUsageCount;
        this.connectedClients = connectedClients;
    }

    public Sensor getSensor()
    {
        return sensor;
    }

    public int getSensorUsageCount()
    {
        return sensorUsageCount;
    }

    public List<InetSocketAddress> getConnectedClients()
    {
        return connectedClients;
    }
}
