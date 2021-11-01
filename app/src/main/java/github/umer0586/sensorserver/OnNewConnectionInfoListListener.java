package github.umer0586.sensorserver;

import java.util.ArrayList;

@FunctionalInterface
public interface OnNewConnectionInfoListListener {
    void onNewConnectionList(ArrayList<ConnectionInfo> connectionInfos);
}
