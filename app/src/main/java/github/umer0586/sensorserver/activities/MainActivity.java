package github.umer0586.sensorserver.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;

import github.umer0586.sensorserver.R;
import github.umer0586.sensorserver.fragments.AvailableSensorsFragment;
import github.umer0586.sensorserver.fragments.ConnectionsFragment;
import github.umer0586.sensorserver.fragments.ServerFragment;
import github.umer0586.sensorserver.websocketserver.ConnectionsCountChangeListener;
import github.umer0586.sensorserver.service.SensorService;
import github.umer0586.sensorserver.service.ServiceBindHelper;


public class MainActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener,
        ServiceConnection, ConnectionsCountChangeListener {

    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private Toolbar toolbar;

    private static final String TAG = MainActivity.class.getSimpleName();
    private ViewPager2 viewPager;

    private ServiceBindHelper serviceBindHelper;
    private SensorService sensorService;

    private BottomNavigationView bottomNavigationView;
    private NavigationView navigationView;

    // Fragments Positions
    private static final int POSITION_SERVER_FRAGMENT = 0;
    private static final int POSITION_CONNECTIONS_FRAGMENT = 1;
    private static final int POSITION_AVAILABLE_SENSORS_FRAGMENT = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_navigation);

        // Set a Toolbar to replace the ActionBar.
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.navigation_server);
        bottomNavigationView.setOnItemSelectedListener(this);


        serviceBindHelper = new ServiceBindHelper(
                getApplicationContext(),
                this,
                SensorService.class
        );

        getLifecycle().addObserver(serviceBindHelper);


        viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(new MyFragmentStateAdapter(this));


        drawerLayout = findViewById(R.id.drawer_layout);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,R.string.nav_open,R.string.nav_close);
        drawerLayout.addDrawerListener( actionBarDrawerToggle );
        actionBarDrawerToggle.syncState();


        // to make the Navigation drawer icon always appear on the action bar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        navigationView = findViewById(R.id.drawer_navigation_view);
        navigationView.setNavigationItemSelectedListener(item ->{

            if(item.getItemId() == R.id.nav_drawer_about)
                startActivity(new Intent(this,AboutActivity.class));

            if(item.getItemId() == R.id.nav_drawer_settings)
                startActivity(new Intent(this,SettingsActivity.class));

            if(item.getItemId() == R.id.nav_drawer_device_axis)
                startActivity(new Intent(this,DeviceAxisActivity.class));



            return false;
        });



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        Log.d(TAG, "onOptionsItemSelected: " + item);


        if (actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnectionCountChange(int count)
    {
        runOnUiThread(()-> setConnectionCountBadge( count ) );
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        SensorService.LocalBinder localBinder = (SensorService.LocalBinder) service;
        sensorService = localBinder.getService();

        if(sensorService != null)
        {
            setConnectionCountBadge(sensorService.getConnectionCount());

            sensorService.setConnectionsCountChangeListener(this);
        }

    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {

    }


    @Override
    protected void onPause()
    {
        super.onPause();
        Log.d(TAG, "onPause()");


        if(sensorService != null)
            sensorService.setConnectionsCountChangeListener(null);


    }


    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");

        getLifecycle().removeObserver(serviceBindHelper);


    }

    private void setConnectionCountBadge(int totalConnections)
    {
        if(totalConnections > 0)
            bottomNavigationView.getOrCreateBadge(R.id.navigation_connections).setNumber(totalConnections);
        else
            bottomNavigationView.removeBadge(R.id.navigation_connections);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {

        switch (item.getItemId())
        {

            case R.id.navigation_available_sensors:
                viewPager.setCurrentItem(POSITION_AVAILABLE_SENSORS_FRAGMENT,false);
                getSupportActionBar().setTitle("Available Sensors");
                return true;

            case R.id.navigation_connections:
                viewPager.setCurrentItem(POSITION_CONNECTIONS_FRAGMENT,false);
                getSupportActionBar().setTitle("Connections");
                return true;

            case R.id.navigation_server:
                viewPager.setCurrentItem(POSITION_SERVER_FRAGMENT,false);
                getSupportActionBar().setTitle("Sensor Server");
                return true;

        }


        return false;
    }


    private class MyFragmentStateAdapter extends FragmentStateAdapter {


        public MyFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity)
        {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int pos) {

            switch(pos)
            {

                case POSITION_SERVER_FRAGMENT:return new ServerFragment();
                case POSITION_CONNECTIONS_FRAGMENT: return new ConnectionsFragment();
                case POSITION_AVAILABLE_SENSORS_FRAGMENT: return new AvailableSensorsFragment();

            }

            return new ServerFragment();
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }


}