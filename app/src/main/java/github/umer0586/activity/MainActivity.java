package github.umer0586.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.preference.SwitchPreferenceCompat;

import github.umer0586.fragments.AvailableSensorsFragment;
import github.umer0586.fragments.ConnectionsFragment;
import github.umer0586.fragments.SettingsFragment;
import github.umer0586.R;
import github.umer0586.sensorserver.SensorWebSocketServer;
import github.umer0586.util.IpUtil;
import com.github.ybq.android.spinkit.SpinKitView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;
import java.net.InetSocketAddress;

public class MainActivity extends AppCompatActivity  implements NavigationBarView.OnItemSelectedListener {

    private final static String TAG = MainActivity.class.getName();
    private SensorWebSocketServer server;

    //To show websocket address on Top (e.g ws://192.168.1.0:8081)
    private TextView serverAddress;

    //To show loading animation when sever is started
    private SpinKitView loadingAnimation;

    //Fragments
    private SettingsFragment settingsFragment;
    private AvailableSensorsFragment availableSensorsFragment;
    private ConnectionsFragment connectionsFragment;

    // Reference to active Fragment
    private Fragment activeFragment;

    //SwitchReference from SettingsFragment to start and stop server
    private SwitchPreferenceCompat serverSwitch;

    //Bottom navigation to show and hide fragments as per navigation item selection
    private BottomNavigationView bottomNavigationView;

    private int port;



    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.nav_bar);
        serverAddress = findViewById(R.id.server_address);
        loadingAnimation = findViewById(R.id.loading_animation);

        //SettingsFragments will initially be displayed when app starts
        bottomNavigationView.setSelectedItemId(R.id.navigation_settings);
        bottomNavigationView.setOnItemSelectedListener(this);

        setupFragments();

    }



    private void handleSettingsFragment()
    {

        settingsFragment = (SettingsFragment) getSupportFragmentManager().findFragmentByTag("settings_fragment");
        serverSwitch = settingsFragment.getServerSwitch();


        serverSwitch.setOnPreferenceChangeListener((preference, newValue) -> {

            boolean newState = (boolean)newValue;

            // If new state of a switch is ON
            if(newState == true)
            {
                 // if WIFI is disable
                if(IpUtil.getWifiIpAddress(getApplicationContext()) == null)
                {
                    new AlertDialog.Builder(this)
                            .setTitle("No Network")
                            .setMessage("Unable to obtain WiFi Ip Address")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setCancelable(false)
                            .setPositiveButton("Okay", (dialog, id) -> {
                                dialog.cancel();
                            })
                            .create()
                            .show();

                    return false; //don't commit newState
                }

                startServer();
                return true; // commit newState

            }

            //If new state of a switch is OFF
            if(newState == false)
            {
                stopServer();
                return true;

            }

            return false;
        });

    }

    // This method will never get call when wifi is disabled
    private void startServer()
    {

        port = settingsFragment.getServerPort();

        String ipAddress = IpUtil.getWifiIpAddress(getApplicationContext());

        server = new SensorWebSocketServer(
                getApplicationContext(),
                new InetSocketAddress(ipAddress,port)
        );

        server.setSensorDelay( settingsFragment.getSensorDelay() );

        server.setOnServerStartListener(()->{
            runOnUiThread(()->{
                  showServerAddress("ws:/"+server.getAddress());
            });
        });

        server.setOnServerStopped(()->{

            runOnUiThread(()->{
                    hideServerAddress();
            });
        });

        server.setOnNewConnectionInfoListListener(connectionInfoList->{

            runOnUiThread(()->{
                connectionsFragment.setConnectionInfoList(connectionInfoList);

                if(!connectionInfoList.isEmpty())
                    bottomNavigationView.getOrCreateBadge(R.id.navigation_connections).setNumber(server.getConnectionCount());
                else
                    bottomNavigationView.removeBadge(R.id.navigation_connections);
            });

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
                serverSwitch.setChecked(false);
            }

        }
    }

    private void setupFragments()
    {

        settingsFragment = new SettingsFragment();
        connectionsFragment = new ConnectionsFragment();
        availableSensorsFragment = new AvailableSensorsFragment();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, "settings_fragment")
                .hide(settingsFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, availableSensorsFragment, null)
                .hide(availableSensorsFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, connectionsFragment,null)
                .hide(connectionsFragment)
                .commit();


        getSupportFragmentManager().beginTransaction()
                .show(settingsFragment)
                .commit();
        activeFragment = settingsFragment;

        // transaction.commit() is non blocking call therefore we need to make sure no transaction is pending
        getSupportFragmentManager().executePendingTransactions();

        handleSettingsFragment();
    }

    private void showServerAddress(String address)
    {
        serverAddress.setText(address);
        serverAddress.setVisibility(View.VISIBLE);
        loadingAnimation.setVisibility(View.VISIBLE);
    }

    private void hideServerAddress()
    {
        serverAddress.setVisibility(View.GONE);
        loadingAnimation.setVisibility(View.GONE);
    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {

        switch (item.getItemId())
        {
            case R.id.navigation_settings:
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .hide(activeFragment)
                        .show(settingsFragment)
                        .commit();
                activeFragment = settingsFragment;
                return true;


            case R.id.navigation_available_sensors:
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .hide(activeFragment)
                        .show(availableSensorsFragment)
                        .commit();
                activeFragment = availableSensorsFragment;
                return true;

            case R.id.navigation_connections:
                getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .hide(activeFragment)
                        .show(connectionsFragment)
                        .commit();
                activeFragment = connectionsFragment;
                return true;
        }


        return false;
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        stopServer();

    }


    /**
     *  onBackPressed() invokes finish() which in result invoked onDestroy()
     *  so to prevent activity from destroying when user presses back button, we must move activity as back task
     */
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }
}