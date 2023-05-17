package github.umer0586.websocketserver;

@FunctionalInterface
public interface ServerStartListener {
    void onServerStarted(ServerInfo serverInfo);
}
