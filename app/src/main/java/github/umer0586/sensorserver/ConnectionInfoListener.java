package github.umer0586.sensorserver;

import java.util.ArrayList;

@FunctionalInterface
public interface ConnectionInfoListener {
    void onNewConnectionList(ArrayList<ConnectionInfo> connectionInfos);
}
