package github.umer0586.sensorserver;

import android.hardware.Sensor;

import java.net.InetSocketAddress;
import java.util.List;

public class ConnectionInfo {

    private Sensor sensor;
    private int sensorConnectionCount;
    private List<InetSocketAddress> connectedClients;

    public ConnectionInfo(Sensor sensor, int sensorConnectionCount, List<InetSocketAddress> connectedClients)
    {
        this.sensor = sensor;
        this.sensorConnectionCount = sensorConnectionCount;
        this.connectedClients = connectedClients;
    }

    public Sensor getSensor()
    {
        return sensor;
    }

    public int getSensorConnectionCount()
    {
        return sensorConnectionCount;
    }

    public List<InetSocketAddress> getConnectedClients()
    {
        return connectedClients;
    }
}
