package github.umer0586.sensorserver;

import java.util.ArrayList;

@FunctionalInterface
public interface ConnectionInfoChangeListener {
    void onConnectionInfoChanged(ArrayList<ConnectionInfo> connectionInfos);
}
