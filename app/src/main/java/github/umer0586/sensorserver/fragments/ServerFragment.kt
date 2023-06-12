package github.umer0586.sensorserver.fragments

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.wifi.WifiManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.customextensions.isHotSpotEnabled
import github.umer0586.sensorserver.databinding.FragmentServerBinding
import github.umer0586.sensorserver.service.SensorService
import github.umer0586.sensorserver.service.SensorService.LocalBinder
import github.umer0586.sensorserver.service.ServerStateListener
import github.umer0586.sensorserver.service.ServiceBindHelper
import github.umer0586.sensorserver.setting.AppSettings
import github.umer0586.sensorserver.util.UIUtil
import github.umer0586.sensorserver.websocketserver.ServerInfo
import java.net.BindException
import java.net.UnknownHostException

class ServerFragment : Fragment(), ServiceConnection, ServerStateListener
{

    private var sensorService: SensorService? = null
    private lateinit var serviceBindHelper: ServiceBindHelper
    private lateinit var appSettings: AppSettings

    private var _binding : FragmentServerBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        _binding = FragmentServerBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")


        appSettings = AppSettings(requireContext())


        serviceBindHelper = ServiceBindHelper(
            context = requireContext(),
            serviceConnection = this,
            service = SensorService::class.java
        )
        lifecycle.addObserver(serviceBindHelper)

        hidePulseAnimation()
        hideServerAddress()

        // we will use tag to determine last state of button
        binding.startButton.setOnClickListener { v ->
            if (v.tag == "stopped")
                startServer()
            else if (v.tag == "started")
                stopServer()
        }


    }

    private fun showServerAddress(address: String)
    {
        binding.cardView.visibility = View.VISIBLE
        binding.serverAddress.visibility = View.VISIBLE
        binding.serverAddress.text = address
        showPulseAnimation()
    }

    private fun startServer()
    {
        Log.d(TAG, "startServer() called")

        val wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        if (appSettings.isHotspotOptionEnabled())
        {
            if (wifiManager.isHotSpotEnabled())
            {
                val intent = Intent(context, SensorService::class.java)
                ContextCompat.startForegroundService(requireContext(), intent)
            }
            else
            {
                showMessage("Please Enable Hotspot")
            }
        }
        else if (appSettings.isLocalHostOptionEnable())
        {
            val intent = Intent(context, SensorService::class.java)
            ContextCompat.startForegroundService(requireContext(), intent)
        }
        else
        {
            if (wifiManager.isWifiEnabled())
            {
                val intent = Intent(context, SensorService::class.java)
                ContextCompat.startForegroundService(requireContext(), intent)
            }
            else
            {
                showMessage("Please Enable Wi-Fi")
            }
        }
    }

    private fun stopServer()
    {
        Log.d(TAG, "stopServer()")

        // getContext().stopService(new Intent(getContext(),SensorService.class));
        requireContext().sendBroadcast(Intent(SensorService.Companion.ACTION_STOP_SERVER))
    }

    override fun onPause()
    {
        super.onPause()
        Log.d(TAG, "onPause()")

        sensorService?.serverStateListener = null
    }

    override fun onServerStarted(serverInfo: ServerInfo)
    {
        Log.d(TAG, "onServerStarted() called")
        UIUtil.runOnUiThread {
            showServerAddress("ws://" + serverInfo.ipAddress + ":" + serverInfo.port)
            showPulseAnimation()

            binding.startButton.tag = "started"
            binding.startButton.text = "STOP"

            showMessage("Server started")
        }
    }

    override fun onServerStopped()
    {
        Log.d(TAG, "onServerStopped() called ")

        UIUtil.runOnUiThread {

            hideServerAddress()
            hidePulseAnimation()

            binding.startButton.tag = "stopped"
            binding.startButton.text = "START"

            showMessage("Server Stopped")
        }
    }

    override fun onServerError(ex: Exception?)
    {
        UIUtil.runOnUiThread {
            if (ex is BindException)
                showMessage("Port already in use")
            else if (ex is UnknownHostException)
                showMessage("Unable to obtain IP")

            else showMessage("Failed to start server")
            Log.w(TAG, "onServerError() called")

            binding.startButton.tag = "stopped"
            binding.startButton.text = "START"

            hideServerAddress()
            hidePulseAnimation()
        }
    }

    override fun onServerAlreadyRunning(serverInfo: ServerInfo)
    {
        Log.d(TAG, "onServerAlreadyRunning() called")
        UIUtil.runOnUiThread {
            showServerAddress("ws://" + serverInfo.ipAddress + ":" + serverInfo.port)
            Toast.makeText(context, "service running", Toast.LENGTH_SHORT).show()
            binding.startButton.tag = "started"
            binding.startButton.text = "STOP"
        }
    }

    private fun showPulseAnimation()
    {
        binding.pulseAnimation.visibility = View.VISIBLE

    }

    private fun hidePulseAnimation()
    {
        binding.pulseAnimation.visibility = View.INVISIBLE
    }

    private fun hideServerAddress()
    {
        binding.cardView.visibility = View.GONE
        binding.serverAddress.visibility = View.GONE
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder)
    {
        val localBinder = service as LocalBinder
        sensorService = localBinder.service

        sensorService?.serverStateListener = this
        sensorService?.checkState() // this callbacks onServerAlreadyRunning()

    }

    override fun onServiceDisconnected(name: ComponentName)
    {
    }

    override fun onDestroy()
    {
        super.onDestroy()
        lifecycle.removeObserver(serviceBindHelper)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showMessage(message: String)
    {

        view?.let{

            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).apply{
                //R.id.nav_bar is in FragmentNavigationActivity
                setAnchorView(R.id.bottom_nav_view)

            }.show()

        }

    }

    companion object
    {
        private val TAG: String = ServerFragment::class.java.simpleName
    }
}