package github.umer0586.sensorserver.activities

import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
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
import github.umer0586.sensorserver.service.HttpServerStateListener
import github.umer0586.sensorserver.service.HttpService
import github.umer0586.sensorserver.service.WebsocketService
import github.umer0586.sensorserver.service.ServiceBindHelper
import github.umer0586.sensorserver.webserver.HttpServerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.Exception

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener
{

    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle

    private lateinit var websocketServiceBindHelper: ServiceBindHelper
    private var websocketService: WebsocketService? = null

    private lateinit var httpServiceBindHelper: ServiceBindHelper
    private var httpService: HttpService? = null

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






        websocketServiceBindHelper = ServiceBindHelper(
            context = applicationContext,
            service = WebsocketService::class.java,
            componentLifecycle = lifecycle
        )

        websocketServiceBindHelper.onServiceConnected(this::onWebsocketServiceConnected)

        httpServiceBindHelper = ServiceBindHelper(
                context = applicationContext,
                service = HttpService::class.java,
                componentLifecycle = lifecycle
        )

        httpServiceBindHelper.onServiceConnected(this::onHttpServiceConnected)

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


    private fun onWebsocketServiceConnected(binder: IBinder)
    {
        val localBinder = binder as WebsocketService.LocalBinder
        websocketService = localBinder.service

        websocketService?.let{ setConnectionCountBadge( it.getConnectionCount() ) }

        websocketService?.onConnectionsCountChange { count ->

            lifecycleScope.launch(Dispatchers.Main) {
                setConnectionCountBadge(count)
            }
        }

    }

    private fun onHttpServiceConnected(binder: IBinder){


        val httpServerAddressParentView = (binding.drawerNavigationView.menu
                .findItem(R.id.nav_drawer_http_server_address).actionView as RelativeLayout)
        val httpServerAddress = httpServerAddressParentView.findViewById<TextView>(R.id.server_address)

        val httpServerSwitch = (binding.drawerNavigationView.menu.findItem(R.id.nav_drawer_http_server_switch).actionView as RelativeLayout).getChildAt(0) as SwitchCompat


        val showServerAddress : ((HttpServerInfo) -> Unit) = {info ->
            httpServerAddressParentView.visibility = View.VISIBLE
            httpServerAddress.apply {
                visibility = View.VISIBLE
                text = info.baseUrl
            }

        }

        val hideServerAddress  = {
            httpServerAddressParentView.visibility = View.GONE
            httpServerAddress.visibility = View.INVISIBLE
        }

        hideServerAddress()

        val localBinder = binder as HttpService.LocalBinder
        httpService = localBinder.service

        httpService?.setServerStateListener(object : HttpServerStateListener{
            override fun onStart(httpServerInfo: HttpServerInfo) {
                lifecycleScope.launch(Dispatchers.Main){
                    showServerAddress(httpServerInfo)
                    Toast.makeText(this@MainActivity,"web server started",Toast.LENGTH_SHORT).show()
                    httpServerSwitch.isChecked = true
                }
            }

            override fun onStop() {
                lifecycleScope.launch(Dispatchers.Main){
                    hideServerAddress()
                    Toast.makeText(this@MainActivity,"web server stopped",Toast.LENGTH_SHORT).show()
                    httpServerSwitch.isChecked = false
                }
            }

            override fun onError(exception: Exception) {
                lifecycleScope.launch(Dispatchers.Main){
                    Toast.makeText(this@MainActivity,exception.message,Toast.LENGTH_SHORT).show()
                    httpServerSwitch.isChecked = false
                    Log.e(TAG,exception.message.toString())
                }

            }

            override fun onRunning(httpServerInfo: HttpServerInfo) {
                lifecycleScope.launch(Dispatchers.Main){
                    showServerAddress(httpServerInfo)
                    httpServerSwitch.isChecked = true
                }
            }

        })

        httpService?.checkState()

        httpServerSwitch.setOnCheckedChangeListener { _, isChecked ->
            val isServerRunning = httpService?.isServerRunning ?: false
            if(isChecked && !isServerRunning){
                val intent = Intent(applicationContext, HttpService::class.java)
                ContextCompat.startForegroundService(applicationContext, intent)
            }
            else if (!isChecked && isServerRunning) {
                val intent = Intent(HttpService.ACTION_STOP_SERVER).apply {
                    setPackage(applicationContext.packageName)
                }
                this.sendBroadcast(intent)
            }
        }
    }


    override fun onPause()
    {
        super.onPause()
        Log.d(TAG, "onPause()")

        // To prevent memory leak
        websocketService?.onConnectionsCountChange(callBack = null)
        httpService?.setServerStateListener(null)
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