package github.umer0586.sensorserver;

import android.hardware.Sensor;

import java.net.InetSocketAddress;
import java.util.List;

public class ConnectionInfo {

    private Sensor sensor;
    private List<InetSocketAddress> connectedClients;

    public ConnectionInfo(Sensor sensor, List<InetSocketAddress> connectedClients)
    {
        this.sensor = sensor;
        this.connectedClients = connectedClients;
    }

    public Sensor getSensor()
    {
        return sensor;
    }

    public int getSensorConnectionCount()
    {
        return connectedClients.size();
    }

    public List<InetSocketAddress> getConnectedClients()
    {
        return connectedClients;
    }
}
