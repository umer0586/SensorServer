package github.umer0586.sensorserver;

@FunctionalInterface
public interface ServerStartListener {
    void onServerStarted(ServerInfo serverInfo);
}
