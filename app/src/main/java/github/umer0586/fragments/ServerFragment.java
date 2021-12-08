package github.umer0586.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.sensorserver.ConnectionInfo;
import github.umer0586.sensorserver.OnNewConnectionInfoListListener;
import github.umer0586.sensorserver.OnServerStartListener;
import github.umer0586.sensorserver.OnServerStopppedListener;
import github.umer0586.sensorserver.SensorWebSocketServer;
import github.umer0586.util.IpUtil;


public class ServerFragment extends Fragment implements OnServerStartListener, OnServerStopppedListener {

    private SensorWebSocketServer server;

    private static final String TAG = ServerFragment.class.getSimpleName();

    // Button at center to start/stop server
    private MaterialButton startButton;

    // Address of server (http://192.168.2.1:8081)
    private TextView serverAddress;


    // card view which holds serverAddress
    private CardView cardView;

    //Ripple animation behind startButton
    private SpinKitView pulseAnimation;

    private OnNewConnectionInfoListListener onNewConnectionInfoListListeners;
    private ConnectionCountListener connectionCountListener;


    private SharedPreferences sharedPreferences;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated: ");

        startButton = view.findViewById(R.id.start_button);
        serverAddress = view.findViewById(R.id.server_address);
        pulseAnimation = view.findViewById(R.id.loading_animation);
        cardView = view.findViewById(R.id.card_view);


        hidePulseAnimation();
        hideServerAddress();

        // we will use tag to determine last state of button
        startButton.setOnClickListener(v -> {
            if(v.getTag().equals("stopped"))
                startServer();
            else if(v.getTag().equals("started"))
                stopServer();
        });

        sharedPreferences = getContext().getSharedPreferences(getString(R.string.shared_pref_file),getContext().MODE_PRIVATE);

    }

    private void showServerAddress(final String address)
    {
        if(server != null)
        {
            cardView.setVisibility(View.VISIBLE);
            serverAddress.setVisibility(View.VISIBLE);
            serverAddress.setText(address);
        }


    }


    private void startServer()
    {


        String ipAddress = IpUtil.getWifiIpAddress(getContext());

        if(ipAddress == null)
        {
            Snackbar.make(getView(),"No Network", Snackbar.LENGTH_SHORT).show();
            return;
        }

        int portNo = sharedPreferences.getInt(getString(R.string.pref_key_port_no),8081);

        server = new SensorWebSocketServer(
                getContext(),
                new InetSocketAddress(ipAddress,portNo)
        );


        server.setOnServerStartListener(this);
        server.setOnServerStopped(this);

        server.setOnNewConnectionInfoListListener((connectionInfoArrayList)->{

                if ( onNewConnectionInfoListListeners != null)
                    onNewConnectionInfoListListeners.onNewConnectionList(connectionInfoArrayList);

                if( connectionCountListener != null)
                {
                    int totalConnections = 0;
                    for(ConnectionInfo connectionInfo : connectionInfoArrayList)
                        totalConnections += connectionInfo.getSensorUsageCount();

                    connectionCountListener.connectionCount(totalConnections);

                }

        });

        server.run();


    }



    private void stopServer()
    {
        if( server != null)
        {
            try {
                server.stop();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();

            }

        }
    }

    @Override
    public void onServerStarted()
    {
        getActivity().runOnUiThread(()->{
            showServerAddress("ws:/"+server.getAddress());
            showPulseAnimation();
            startButton.setTag("started");
            startButton.setText("STOP");
            Snackbar.make(getView(),"Server started",Snackbar.LENGTH_SHORT).show();

        });
    }

    @Override
    public void onServerStopped()
    {
        getActivity().runOnUiThread(()->{
            hideServerAddress();
            hidePulseAnimation();
            startButton.setTag("stopped");
            startButton.setText("START");
            Snackbar.make(getView(),"Server stopped",Snackbar.LENGTH_SHORT).show();

        });
    }

    private void showPulseAnimation()
    {
        pulseAnimation.setVisibility(View.VISIBLE);
    }

    private void hidePulseAnimation()
    {
        pulseAnimation.setVisibility(View.INVISIBLE);
    }

    private void hideServerAddress()
    {
        cardView.setVisibility(View.GONE);
        serverAddress.setVisibility(View.GONE);
    }

    public void setOnNewConnectionInfoListener(OnNewConnectionInfoListListener listener)
    {
        onNewConnectionInfoListListeners = listener;
    }

    public void setConnectionCountListener(ConnectionCountListener listener)
    {
        connectionCountListener = listener;
    }


    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "onDestroy: ");
        stopServer();

    }

    public interface ConnectionCountListener{
        void connectionCount(int count);
    }
}