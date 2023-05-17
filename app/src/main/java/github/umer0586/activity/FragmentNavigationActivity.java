package github.umer0586.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import github.umer0586.R;
import github.umer0586.fragments.AvailableSensorsFragment;
import github.umer0586.fragments.ConnectionsFragment;
import github.umer0586.fragments.ServerFragment;
import github.umer0586.fragments.SettingsFragment;
import github.umer0586.websocketserver.ConnectionsCountChangeListener;
import github.umer0586.service.SensorService;
import github.umer0586.service.ServiceBindHelper;


public class FragmentNavigationActivity extends AppCompatActivity
        implements NavigationBarView.OnItemSelectedListener,
        ServiceConnection, ConnectionsCountChangeListener {

    private static final String TAG = FragmentNavigationActivity.class.getSimpleName();
    private ViewPager2 viewPager;

    private ServiceBindHelper serviceBindHelper;
    private SensorService sensorService;

    private BottomNavigationView bottomNavigationView;

    // Fragments Positions
    private static final int POSITION_SERVER_FRAGMENT = 0;
    private static final int POSITION_SETTING_FRAGMENT = 1;
    private static final int POSITION_CONNECTIONS_FRAGMENT = 2;
    private static final int POSITION_AVAILABLE_SENSORS_FRAGMENT = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_navigation);

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




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item)
    {
        if(item.getItemId() == R.id.about)
            startActivity(new Intent(this,AboutActivity.class));

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
            case R.id.navigation_settings:
                viewPager.setCurrentItem(POSITION_SETTING_FRAGMENT,false);
                getSupportActionBar().setTitle("Settings");
                return true;


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
                case POSITION_SETTING_FRAGMENT: return new SettingsFragment();
                case POSITION_CONNECTIONS_FRAGMENT: return new ConnectionsFragment();
                case POSITION_AVAILABLE_SENSORS_FRAGMENT: return new AvailableSensorsFragment();

            }

            return new ServerFragment();
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }


}