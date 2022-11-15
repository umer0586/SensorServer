package github.umer0586.sensorserver;

@FunctionalInterface
public interface ConnectionCountChangeListener {
     void onConnectionCountChange(int count);
}
