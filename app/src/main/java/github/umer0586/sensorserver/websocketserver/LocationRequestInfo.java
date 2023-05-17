package github.umer0586.sensorserver.websocketserver;

public class LocationRequestInfo {

    private String provider;

    public LocationRequestInfo(String provider)
    {
        this.provider = provider;
    }

    public String getProvider()
    {
        return provider;
    }
}
