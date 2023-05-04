package github.umer0586.sensorserver;

@FunctionalInterface
public interface ConnectionsCountChangeListener {
     void onConnectionCountChange(int count);
}
