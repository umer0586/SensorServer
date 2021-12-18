package github.umer0586.sensorserver;

import java.util.ArrayList;

@FunctionalInterface
public interface ConnectionInfoListener {
    void onConnectionInfo(ArrayList<ConnectionInfo> connectionInfos);
}
