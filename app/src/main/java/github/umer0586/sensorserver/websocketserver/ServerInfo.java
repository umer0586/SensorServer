package github.umer0586.sensorserver.websocketserver;

public class ServerInfo {

    private String IpAddress;
    private int port;

    public ServerInfo(String ipAddress, int port)
    {
        IpAddress = ipAddress;
        this.port = port;
    }

    public String getIpAddress()
    {
        return IpAddress;
    }

    public int getPort()
    {
        return port;
    }
}
