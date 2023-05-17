package github.umer0586.fragments;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.java_websocket.WebSocket;

import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.fragments.customadapters.ConnectionsRecyclerViewAdapter;
import github.umer0586.websocketserver.ConnectionsChangeListener;
import github.umer0586.service.SensorService;
import github.umer0586.service.ServiceBindHelper;
import github.umer0586.util.UIUtil;

/**
 * TODO: functionality to allow user to close all connections (using button in action bar)
 */
public class ConnectionsFragment extends Fragment
        implements ServiceConnection, ConnectionsChangeListener {

    private static final String TAG = github.umer0586.fragments.ConnectionsFragment.class.getSimpleName();

    private SensorService sensorService;
    private ServiceBindHelper serviceBindHelper;

    private RecyclerView recyclerView;
    private ConnectionsRecyclerViewAdapter connectionsRecyclerViewAdapter;
    private ArrayList<WebSocket> webSockets = new ArrayList<>();

    private TextView noConnectionsText;

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
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        connectionsRecyclerViewAdapter = new ConnectionsRecyclerViewAdapter(webSockets);
        recyclerView.setAdapter(connectionsRecyclerViewAdapter);

        noConnectionsText = view.findViewById(R.id.no_connections_text);

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
            {
                this.webSockets.clear();
                this.webSockets.addAll(webSockets);
            }
            handleNoConnectionsText();

            if(webSockets != null)
                connectionsRecyclerViewAdapter.notifyDataSetChanged();

        }


    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {

    }


    @Override
    public void onConnectionsChanged(ArrayList<WebSocket> webSockets)
    {

        this.webSockets.clear();
        this.webSockets.addAll(webSockets);
        handleNoConnectionsText();

        UIUtil.runOnUiThread(()->{
            connectionsRecyclerViewAdapter.notifyDataSetChanged();
        });
    }

    private void handleNoConnectionsText()
    {
        if(webSockets.size() > 0)
            UIUtil.runOnUiThread(()-> noConnectionsText.setVisibility(View.INVISIBLE));
        else
            UIUtil.runOnUiThread(()-> noConnectionsText.setVisibility(View.VISIBLE));
    }


}