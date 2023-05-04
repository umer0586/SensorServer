package github.umer0586.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.ListFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.java_websocket.WebSocket;

import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.sensorserver.ConnectionsChangeListener;
import github.umer0586.sensorserver.SensorWebSocketServer;
import github.umer0586.service.SensorService;
import github.umer0586.service.ServiceBindHelper;
import github.umer0586.util.UIUtil;

/**
 * TODO: functionality to allow user to close all connections (using button in action bar)
 */
public class ConnectionsFragment extends ListFragment
        implements ServiceConnection, ConnectionsChangeListener {

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
            sensorService.setConnectionsChangeListener(null);

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
            sensorService.setConnectionsChangeListener(this);

            ArrayList<WebSocket> webSockets = sensorService.getConnectedClients();

            if(webSockets != null)
                setListAdapter( new ConnectionsListAdapter(getContext(),webSockets) );

        }


    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {

    }


    @Override
    public void onConnectionsChanged(ArrayList<WebSocket> webSockets)
    {
        UIUtil.runOnUiThread(()->{
            setListAdapter( new ConnectionsListAdapter(getContext(),webSockets) );
        });
    }

    private class ConnectionsListAdapter extends ArrayAdapter<WebSocket>{


        public ConnectionsListAdapter(@NonNull Context context, ArrayList<WebSocket> webSockets)
        {
            super(context, R.layout.item_connection, webSockets);
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


            WebSocket webSocket = getItem(position);
            //if(webSocket.getRemoteSocketAddress() == null) return view;

            AppCompatTextView clientAddress = view.findViewById(R.id.client_address);
            clientAddress.setText(webSocket.getRemoteSocketAddress().toString());

            AppCompatTextView sensorDetails = view.findViewById(R.id.sensors_detail);

            if(webSocket.getAttachment() instanceof Sensor)
                sensorDetails.setText( ((Sensor)webSocket.getAttachment()).getName() );

            else if(webSocket.getAttachment() instanceof ArrayList)
            {
                String detail = "";
                for(Sensor sensor : (ArrayList<Sensor>)webSocket.getAttachment() )
                    detail += sensor.getName() + "\n";
                sensorDetails.setText(detail.trim());
            }

            AppCompatTextView closeConnection = view.findViewById(R.id.close_connection);
            closeConnection.setOnClickListener(v->{

                new MaterialAlertDialogBuilder(getContext())
                        .setTitle("Websocket Connection")
                        .setMessage("Close Connection?")
                        .setPositiveButton("Yes",(dialog, which) -> {
                            webSocket.close(SensorWebSocketServer.CLOSE_CODE_CONNECTION_CLOSED_BY_APP_USER,"Connection closed by App user");
                        })
                        .setNegativeButton("No", (dialogInterface, i) -> {
                            dialogInterface.dismiss();
                        })
                        .setCancelable(false)
                        .show();


            });

            AppCompatTextView sensors = view.findViewById(R.id.sensors);
            ExpandableLayout expandableLayout = view.findViewById(R.id.expandable_layout);

            sensors.setOnClickListener(v->{
                expandableLayout.toggle();
                if(expandableLayout.isExpanded())
                    sensors.setText("Hide");
                else
                    sensors.setText("sensors");
            });

            return view;


        }




    }



}