package github.umer0586.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.ListFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.sensorserver.ConnectionInfo;
import github.umer0586.sensorserver.ConnectionInfoChangeListener;
import github.umer0586.sensorserver.SensorWebSocketServer;
import github.umer0586.service.SensorService;
import github.umer0586.service.ServiceBindHelper;
import github.umer0586.util.UIUtil;

/**
 * TODO: functionality to allow user to close all connections (using button in action bar)
 */
public class ConnectionsFragment extends ListFragment
        implements ServiceConnection, ConnectionInfoChangeListener {

    private static final String TAG = ConnectionsFragment.class.getSimpleName();

    private SensorService sensorService;
    private ServiceBindHelper serviceBindHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.d(TAG, "onCreateView()");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        serviceBindHelper = new ServiceBindHelper(
                getContext(),
                this,
                SensorService.class
        );

        getLifecycle().addObserver(serviceBindHelper);
    }


    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause()");

        if(sensorService != null)
            sensorService.setConnectionInfoChangeListener(null);

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        getLifecycle().removeObserver(serviceBindHelper);
    }


    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {

        SensorService.LocalBinder localBinder = (SensorService.LocalBinder) service;
        sensorService =  localBinder.getService();

        if(sensorService != null)
        {
            sensorService.setConnectionInfoChangeListener(this);

            ArrayList<ConnectionInfo> connectionInfos = sensorService.getConnectionInfoList();

            if(connectionInfos != null)
                setListAdapter( new ConnectionListAdapter(getContext(),connectionInfos) );

        }


    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {

    }


    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        ConnectionInfo connectionInfo = (ConnectionInfo) v.getTag();

        // show dialog

        String message = "Do you want to close this connection\n" +
                "Sensor : " + connectionInfo.getSensor().getName() + "\n" +
                "Connections : " + connectionInfo.getSensorConnectionCount() + "\n\n";

        for(InetSocketAddress inetSocketAddress : connectionInfo.getConnectedClients())
            message += inetSocketAddress + "\n";

        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Close Connection")
                .setMessage(message)
                .setPositiveButton("Yes",(dialogInterface, i) -> {

                 if(sensorService != null)
                 {
                     SensorWebSocketServer sensorWebSocketServer = sensorService.getSensorWebSocketServer();
                     if (sensorWebSocketServer != null)
                         sensorWebSocketServer.closeConnectionBySensor(connectionInfo.getSensor());

                     dialogInterface.dismiss();
                 }
                })
                .setNegativeButton("No", (dialogInterface, i) -> {
                    dialogInterface.dismiss();
                })
                .setCancelable(false)
                .show();

    }

    @Override
    public void onConnectionInfoChanged(ArrayList<ConnectionInfo> connectionInfos)
    {
        Log.d(TAG, "onConnectionInfo() called with: connectionInfos = [" + connectionInfos + "]");
        UIUtil.runOnUiThread(()->{
            setListAdapter( new ConnectionListAdapter(getContext(),connectionInfos) );
        });

    }

    private class ConnectionListAdapter extends ArrayAdapter<ConnectionInfo>{


        public ConnectionListAdapter(@NonNull Context context, ArrayList<ConnectionInfo> connectionInfos)
        {
            super(context, R.layout.item_connection, connectionInfos);
            
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {

            View view;
            if (convertView == null)
                view = getLayoutInflater().inflate(R.layout.item_connection, parent, false);
             else
                view = convertView;


            ConnectionInfo connectionInfo = getItem(position);

            AppCompatTextView sensorName = view.findViewById(R.id.sensor_name);
            AppCompatTextView connectionCount = view.findViewById(R.id.connection_count);

            sensorName.setText( connectionInfo.getSensor().getName() );
            connectionCount.setText( connectionInfo.getSensorConnectionCount() + "");

            view.setTag(connectionInfo);
            return view;


        }




    }



}