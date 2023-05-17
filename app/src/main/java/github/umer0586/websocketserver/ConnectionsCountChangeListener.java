package github.umer0586.websocketserver;

@FunctionalInterface
public interface ConnectionsCountChangeListener {
     void onConnectionCountChange(int count);
}
