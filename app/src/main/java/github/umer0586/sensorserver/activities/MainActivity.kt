package github.umer0586.sensorserver.activities

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.navigation.NavigationBarView
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.databinding.ActivityMainBinding
import github.umer0586.sensorserver.fragments.AvailableSensorsFragment
import github.umer0586.sensorserver.fragments.ConnectionsFragment
import github.umer0586.sensorserver.fragments.ServerFragment
import github.umer0586.sensorserver.service.WebsocketService
import github.umer0586.sensorserver.service.WebsocketService.LocalBinder
import github.umer0586.sensorserver.service.ServiceBindHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener
{

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var serviceBindHelper: ServiceBindHelper
    private var websocketService: WebsocketService? = null

    private lateinit var binding : ActivityMainBinding

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

        binding = ActivityMainBinding.inflate(layoutInflater)
       //toolBarBinding = ToolbarBinding.inflate(layoutInflater)

        setContentView(binding.root)

        // Set a Toolbar to replace the ActionBar.
        setSupportActionBar(binding.toolbar.root)


        binding.dashboard.bottomNavView.selectedItemId = R.id.navigation_server
        binding.dashboard.bottomNavView.setOnItemSelectedListener(this)

        serviceBindHelper = ServiceBindHelper(
            context = applicationContext,
            service = WebsocketService::class.java,
            componentLifecycle = lifecycle
        )

        serviceBindHelper.onServiceConnected(this::onServiceConnected)



        binding.dashboard.viewPager.isUserInputEnabled = false
        binding.dashboard.viewPager.adapter = MyFragmentStateAdapter(this)


        actionBarDrawerToggle = ActionBarDrawerToggle(this, binding.drawerLayout, R.string.nav_open, R.string.nav_close)
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()


        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        binding.drawerNavigationView.setNavigationItemSelectedListener { menuItem ->

            if (menuItem.itemId == R.id.nav_drawer_about)
                startActivity(Intent(this, AboutActivity::class.java))

            if (menuItem.itemId == R.id.nav_drawer_settings)
                startActivity( Intent(this,SettingsActivity::class.java)  )

            if (menuItem.itemId == R.id.nav_drawer_device_axis)
                startActivity( Intent(this, DeviceAxisActivity::class.java ) )

            if (menuItem.itemId == R.id.nav_drawer_touch_sensors)
                startActivity( Intent(this, TouchScreenActivity::class.java) )


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


    fun onServiceConnected(binder: IBinder)
    {
        val localBinder = binder as LocalBinder
        websocketService = localBinder.service

        websocketService?.let{ setConnectionCountBadge( it.getConnectionCount() ) }

        websocketService?.onConnectionsCountChange { count ->

            lifecycleScope.launch(Dispatchers.Main) {
                setConnectionCountBadge(count)
            }
        }

    }


    override fun onPause()
    {
        super.onPause()
        Log.d(TAG, "onPause()")

        // To prevent memory leak
        websocketService?.onConnectionsCountChange(callBack = null)
    }


    private fun setConnectionCountBadge(totalConnections: Int)
    {
        if (totalConnections > 0)
            binding.dashboard.bottomNavView.getOrCreateBadge(R.id.navigation_connections).number = totalConnections
        else
            binding.dashboard.bottomNavView.removeBadge(R.id.navigation_connections)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean
    {
        when (item.itemId)
        {
            R.id.navigation_available_sensors ->
            {
                binding.dashboard.viewPager.setCurrentItem(POSITION_AVAILABLE_SENSORS_FRAGMENT, false)
                supportActionBar?.title = "Available Sensors"
                return true
            }

            R.id.navigation_connections ->
            {
                binding.dashboard.viewPager.setCurrentItem(POSITION_CONNECTIONS_FRAGMENT, false)
                supportActionBar?.title = "Connections"
                return true
            }

            R.id.navigation_server ->
            {
                binding.dashboard.viewPager.setCurrentItem(POSITION_SERVER_FRAGMENT, false)
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