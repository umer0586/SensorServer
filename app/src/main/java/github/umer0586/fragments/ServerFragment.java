package github.umer0586.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.net.BindException;
import java.net.UnknownHostException;

import github.umer0586.R;
import github.umer0586.sensorserver.ServerInfo;
import github.umer0586.service.SensorService;
import github.umer0586.service.ServiceBindHelper;
import github.umer0586.setting.AppSettings;
import github.umer0586.util.UIUtil;
import github.umer0586.util.WifiUtil;


public class ServerFragment extends Fragment
        implements ServiceConnection, SensorService.ServerStateListener {

    private static final String TAG = ServerFragment.class.getSimpleName();

    private SensorService sensorService;
    private ServiceBindHelper serviceBindHelper;
    private AppSettings appSettings;

    // Button at center to start/stop server
    private MaterialButton startButton;

    // Address of server (ws://192.168.2.1:8081)
    private TextView serverAddress;

    // card view which holds serverAddress
    private CardView cardView;

    //Ripple animation behind startButton
    private SpinKitView pulseAnimation;


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

        appSettings = new AppSettings(getContext());

        AppCompatTextView donationText = view.findViewById(R.id.donationText);
        donationText.setOnClickListener(v->{
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if(intent.resolveActivity(getContext().getPackageManager()) != null)
            {
                intent.setData(Uri.parse("http://www.buymeacoffee.com/umerfarooq"));
                startActivity(Intent.createChooser(intent,"Select Browser"));
            }
            else
            {
                Toast.makeText(getContext(),"Browser app not found",Toast.LENGTH_SHORT).show();
            }
        });

        serviceBindHelper = new ServiceBindHelper(
                getContext(),
                this,
                SensorService.class
        );

        getLifecycle().addObserver(serviceBindHelper);


        hidePulseAnimation();
        hideServerAddress();

        // we will use tag to determine last state of button
        startButton.setOnClickListener(v -> {
            if(v.getTag().equals("stopped"))
                startServer();
            else if(v.getTag().equals("started"))
                stopServer();
        });


    }

    private void showServerAddress(final String address)
    {

            cardView.setVisibility(View.VISIBLE);
            serverAddress.setVisibility(View.VISIBLE);
            serverAddress.setText(address);

            showPulseAnimation();

    }


    private void startServer()
    {
        Log.d(TAG, "startServer() called");

        if(appSettings.isHotspotOptionEnabled())
        {
            if(WifiUtil.isHotspotEnabled(getContext()))
            {
                Intent intent = new Intent(getContext(), SensorService.class);
                ContextCompat.startForegroundService(getContext(),intent);
            }
            else
            {
                showMessage("Please Enable Hotspot");
            }
        }

        //If user has enabled local-host option (for adb) then don't check wifi state
        else if(appSettings.isLocalHostOptionEnable())
        {
            Intent intent = new Intent(getContext(), SensorService.class);
            ContextCompat.startForegroundService(getContext(),intent);
        }
        //If user has not enabled local-host and hotspot option then check if wifi is enabled
        else
        {
            if(WifiUtil.isWifiEnabled(getContext()))
            {
                Intent intent = new Intent(getContext(), SensorService.class);
                ContextCompat.startForegroundService(getContext(),intent);
            }
            else
            {
                showMessage("Please Enable Wi-Fi");
            }


        }




    }



    private void stopServer()
    {
        Log.d(TAG, "stopServer()");

       // getContext().stopService(new Intent(getContext(),SensorService.class));
        getContext().sendBroadcast(new Intent(SensorService.ACTION_STOP_SERVER));

    }

    @Override
    public void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause()");

        if(sensorService != null)
            sensorService.setServerStateListener(null);


    }

    @Override
    public void onServerStarted(ServerInfo serverInfo)
    {
        Log.d(TAG, "onServerStarted() called");
        UIUtil.runOnUiThread(()->{

            showServerAddress("ws://"+serverInfo.getIpAddress()+":"+serverInfo.getPort());
            showPulseAnimation();

            startButton.setTag("started");
            startButton.setText("STOP");

            showMessage("Server started");
        });

    }

    @Override
    public void onServerStopped()
    {
        Log.d(TAG, "onServerStopped() called ");
        UIUtil.runOnUiThread(()->{

            hideServerAddress();
            hidePulseAnimation();

            startButton.setTag("stopped");
            startButton.setText("START");

           showMessage("Server Stopped");

        });

    }


    @Override
    public void onServerError(Exception exception)
    {

        UIUtil.runOnUiThread(()->{

            if(exception instanceof BindException)
                showMessage("Port already in use");

            else if (exception instanceof UnknownHostException)
                showMessage("Unable to obtain IP");

            else
                showMessage("Failed to start server");

            Log.w(TAG, "onServerError() called");

            startButton.setTag("stopped");
            startButton.setText("START");

            hideServerAddress();
            hidePulseAnimation();

        });


    }

    @Override
    public void onServerAlreadyRunning(ServerInfo serverInfo)
    {
        Log.d(TAG, "onServerAlreadyRunning() called");
        UIUtil.runOnUiThread(()->{
            showServerAddress("ws://"+serverInfo.getIpAddress()+":"+serverInfo.getPort());
            Toast.makeText(getContext(),"service running",Toast.LENGTH_SHORT).show();
            startButton.setTag("started");
            startButton.setText("STOP");
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

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {

        SensorService.LocalBinder localBinder = (SensorService.LocalBinder) service;
        sensorService =  localBinder.getService();

        if(sensorService != null)
        {
            sensorService.setServerStateListener(this);
            sensorService.isServerRunning(); // this callbacks onServerAlreadyRunning()
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {

    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        getLifecycle().removeObserver(serviceBindHelper);
    }

    private void showMessage(String message)
    {

        View view = getView();
        Context context = getContext();

        if(view != null)
        {
            Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT);
            //R.id.nav_bar is in FragmentNavigationActivity
            snackbar.setAnchorView(R.id.nav_bar);
            snackbar.show();
        }
        else if( context != null)
           Toast.makeText(context,message,Toast.LENGTH_SHORT).show();
        else
            Log.e(TAG, "showMessage() cannot display message as getView() or getContext() returned null" );
    }

}
