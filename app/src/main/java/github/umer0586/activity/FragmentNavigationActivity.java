package github.umer0586.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import github.umer0586.R;
import github.umer0586.fragments.AvailableSensorsFragment;
import github.umer0586.fragments.ConnectionsFragment;
import github.umer0586.fragments.ServerFragment;
import github.umer0586.fragments.SettingsFragment;

public class FragmentNavigationActivity extends AppCompatActivity  implements NavigationBarView.OnItemSelectedListener {


    //Fragments
    private ServerFragment serverFragment;
    private SettingsFragment settingsFragment;
    private AvailableSensorsFragment availableSensorsFragment;
    private ConnectionsFragment connectionsFragment;

    // Reference to active Fragment
    private Fragment activeFragment;

    //Bottom navigation to show and hide fragments as per navigation item selection
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_navigation);

        bottomNavigationView = findViewById(R.id.nav_bar);

        //ServerFragment will initially be displayed when app starts
        bottomNavigationView.setSelectedItemId(R.id.navigation_server);
        bottomNavigationView.setOnItemSelectedListener(this);
        setupFragments();
    }

    private void setupFragments()
    {
        serverFragment = new ServerFragment();
        settingsFragment = new SettingsFragment();
        connectionsFragment = new ConnectionsFragment();
        availableSensorsFragment = new AvailableSensorsFragment();

        /*
            when user click connection item from ConnectionsFragments
            ServerFragment will catch that click event and prompts if user wants to close selected connection
            its because ServerFragment holds reference to SensorWebSocketServer's object
         */
        connectionsFragment.setOnConnectionItemClickedListener(serverFragment);

        // ServerFragment hold reference to server object therefore to get a connection info list from server, client must register to ServerFragment
        serverFragment.setConnectionInfoListener(connectionsFragment);
        serverFragment.setConnectionCountListener((totalConnections)->{
            runOnUiThread(()->{

                if(totalConnections > 0)
                    bottomNavigationView.getOrCreateBadge(R.id.navigation_connections).setNumber(totalConnections);
                else
                    bottomNavigationView.removeBadge(R.id.navigation_connections);
            });
        });


        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, serverFragment, null)
                .hide(serverFragment)
                .commit();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.fragment_container, settingsFragment, null)
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
                .show(serverFragment)
                .commit();

        activeFragment = serverFragment;

        // transaction.commit() is non blocking call therefore we need to make sure no transaction is pending
        getSupportFragmentManager().executePendingTransactions();

    }
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.navigation_server:
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(serverFragment)
                        .commit();
                activeFragment = serverFragment;
                getSupportActionBar().setTitle("Sensor Server");
                return true;

            case R.id.navigation_settings:
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(settingsFragment)
                        .commit();
                activeFragment = settingsFragment;
                getSupportActionBar().setTitle("Settings");
                return true;


            case R.id.navigation_available_sensors:
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(availableSensorsFragment)
                        .commit();
                activeFragment = availableSensorsFragment;
                getSupportActionBar().setTitle("Available Sensors");
                return true;

            case R.id.navigation_connections:
                getSupportFragmentManager().beginTransaction()
                        .hide(activeFragment)
                        .show(connectionsFragment)
                        .commit();
                activeFragment = connectionsFragment;
                getSupportActionBar().setTitle("Connections");
                return true;
        }


        return false;
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