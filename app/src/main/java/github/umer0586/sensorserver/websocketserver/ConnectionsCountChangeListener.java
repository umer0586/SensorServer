package github.umer0586.sensorserver.websocketserver;

@FunctionalInterface
public interface ConnectionsCountChangeListener {
     void onConnectionCountChange(int count);
}
