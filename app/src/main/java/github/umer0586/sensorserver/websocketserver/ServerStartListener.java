package github.umer0586.sensorserver.websocketserver;

@FunctionalInterface
public interface ServerStartListener {
    void onServerStarted(ServerInfo serverInfo);
}
