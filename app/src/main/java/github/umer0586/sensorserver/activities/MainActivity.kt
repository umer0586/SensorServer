package github.umer0586.sensorserver.activities

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.fragments.AvailableSensorsFragment
import github.umer0586.sensorserver.fragments.ConnectionsFragment
import github.umer0586.sensorserver.fragments.ServerFragment
import github.umer0586.sensorserver.service.SensorService
import github.umer0586.sensorserver.service.SensorService.LocalBinder
import github.umer0586.sensorserver.service.ServiceBindHelper

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener, ServiceConnection
{


    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var toolbar: Toolbar
    private lateinit var viewPager: ViewPager2
    private lateinit var serviceBindHelper: ServiceBindHelper
    private var sensorService: SensorService? = null
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var navigationView: NavigationView

    companion object
    {

        private val TAG: String = MainActivity::class.java.simpleName

        // Fragments Positions
        private const val POSITION_SERVER_FRAGMENT = 0
        private const val POSITION_CONNECTIONS_FRAGMENT = 1
        private const val POSITION_AVAILABLE_SENSORS_FRAGMENT = 2
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_navigation)

        // Set a Toolbar to replace the ActionBar.
        toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        bottomNavigationView = findViewById(R.id.nav_bar)
        bottomNavigationView.selectedItemId = R.id.navigation_server
        bottomNavigationView.setOnItemSelectedListener(this)

        serviceBindHelper = ServiceBindHelper(
            context = applicationContext,
            serviceConnection = this,
            service = SensorService::class.java
        )
        lifecycle.addObserver(serviceBindHelper)

        viewPager = findViewById(R.id.view_pager)
        viewPager.isUserInputEnabled = false
        viewPager.adapter = MyFragmentStateAdapter(this)

        drawerLayout = findViewById(R.id.drawer_layout)
        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.nav_open, R.string.nav_close)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()


        // to make the Navigation drawer icon always appear on the action bar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        navigationView = findViewById(R.id.drawer_navigation_view)
        navigationView.setNavigationItemSelectedListener { item: MenuItem ->

            if (item.itemId == R.id.nav_drawer_about)
                startActivity(Intent(this, AboutActivity::class.java))

            if (item.itemId == R.id.nav_drawer_settings)
                startActivity( Intent(this,SettingsActivity::class.java)  )

            if (item.itemId == R.id.nav_drawer_device_axis)
                startActivity( Intent(this, DeviceAxisActivity::class.java ) )

            false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        Log.d(TAG, "onOptionsItemSelected: $item")
        return if (actionBarDrawerToggle.onOptionsItemSelected(item))
        {
            true
        }
        else super.onOptionsItemSelected(item)
    }


    override fun onServiceConnected(name: ComponentName, service: IBinder)
    {
        val localBinder = service as LocalBinder
        sensorService = localBinder.service

        sensorService?.let{ setConnectionCountBadge( it.getConnectionCount() ) }

        sensorService?.connectionsCountChangeListener = { count ->

            runOnUiThread { setConnectionCountBadge(count) }
        }

    }

    override fun onServiceDisconnected(name: ComponentName)
    {
    }

    override fun onPause()
    {
        super.onPause()
        Log.d(TAG, "onPause()")
        sensorService?.connectionsCountChangeListener = null
    }

    override fun onDestroy()
    {
        super.onDestroy()
        Log.d(TAG, "onDestroy()")
        lifecycle.removeObserver(serviceBindHelper)
    }

    private fun setConnectionCountBadge(totalConnections: Int)
    {
        if (totalConnections > 0)
            bottomNavigationView.getOrCreateBadge(R.id.navigation_connections).number = totalConnections
        else
            bottomNavigationView.removeBadge(R.id.navigation_connections)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.navigation_available_sensors ->
            {
                viewPager.setCurrentItem(POSITION_AVAILABLE_SENSORS_FRAGMENT, false)
                supportActionBar?.title = "Available Sensors"
                return true
            }

            R.id.navigation_connections ->
            {
                viewPager.setCurrentItem(POSITION_CONNECTIONS_FRAGMENT, false)
                supportActionBar?.title = "Connections"
                return true
            }

            R.id.navigation_server ->
            {
                viewPager.setCurrentItem(POSITION_SERVER_FRAGMENT, false)
                supportActionBar?.title = "Sensor Server"
                return true
            }
        }
        return false
    }

    private inner class MyFragmentStateAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity)
    {

        override fun createFragment(pos: Int): Fragment
        {
            when (pos)
            {
                POSITION_SERVER_FRAGMENT -> return ServerFragment()
                POSITION_CONNECTIONS_FRAGMENT -> return ConnectionsFragment()
                POSITION_AVAILABLE_SENSORS_FRAGMENT -> return AvailableSensorsFragment()
            }
            return ServerFragment()
        }

        override fun getItemCount(): Int
        {
            return 3
        }
    }


}