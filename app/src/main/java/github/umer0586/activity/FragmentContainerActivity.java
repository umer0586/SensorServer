package github.umer0586.activity;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import github.umer0586.R;
import github.umer0586.fragments.AvailableSensorsFragment;
import github.umer0586.fragments.ConnectionsFragment;
import github.umer0586.fragments.ServerFragment;
import github.umer0586.fragments.SettingsFragment;


public class FragmentContainerActivity extends AppCompatActivity implements NavigationBarView.OnItemSelectedListener, ServerFragment.ConnectionCountListener {

    private ViewPager2 viewPager;

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
        setContentView(R.layout.activity_main_fragment_container);

        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.navigation_server);
        bottomNavigationView.setOnItemSelectedListener(this);


        viewPager = findViewById(R.id.view_pager);
        viewPager.setUserInputEnabled(false);
        viewPager.setAdapter(new MyFragmentStateAdapter(this));


        for (int i = 0; i <= 3 ; i++)
            viewPager.setCurrentItem(i);

        viewPager.postDelayed(()->{

            viewPager.setCurrentItem(POSITION_SERVER_FRAGMENT);

        },100);

    }

    @Override
    public void connectionCount(int totalConnections)
    {

        runOnUiThread(()->{

            if(totalConnections > 0)
                bottomNavigationView.getOrCreateBadge(R.id.navigation_connections).setNumber(totalConnections);
            else
                bottomNavigationView.removeBadge(R.id.navigation_connections);

        });

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



    /**
     *  onBackPressed() invokes finish() which in result invoked onDestroy()
     *  so to prevent activity from destroying when user presses back button, we must move activity back task
     */
    @Override
    public void onBackPressed()
    {
        moveTaskToBack(true);
    }

    private class MyFragmentStateAdapter extends FragmentStateAdapter {


        private ServerFragment serverFragment;
        private ConnectionsFragment connectionsFragment;


        public MyFragmentStateAdapter(@NonNull FragmentActivity fragmentActivity)
        {
            super(fragmentActivity);
        }

        @Override
        public Fragment createFragment(int pos) {

            switch(pos)
            {

                case POSITION_SERVER_FRAGMENT:
                {
                    serverFragment = new ServerFragment();
                    serverFragment.setConnectionCountListener(FragmentContainerActivity.this);

                    return serverFragment;
                }
                case POSITION_SETTING_FRAGMENT: return new SettingsFragment();
                case POSITION_CONNECTIONS_FRAGMENT:
                {
                    connectionsFragment = new ConnectionsFragment();
                    serverFragment.setOnNewConnectionInfoListener(connectionsFragment);

                    return connectionsFragment;
                }
                case POSITION_AVAILABLE_SENSORS_FRAGMENT: return new AvailableSensorsFragment();

            }

            return serverFragment;
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }


}