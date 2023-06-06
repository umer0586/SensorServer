package github.umer0586.sensorserver.fragments

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.ybq.android.spinkit.SpinKitView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.service.SensorService
import github.umer0586.sensorserver.service.SensorService.LocalBinder
import github.umer0586.sensorserver.service.ServerStateListener
import github.umer0586.sensorserver.service.ServiceBindHelper
import github.umer0586.sensorserver.setting.AppSettings
import github.umer0586.sensorserver.util.UIUtil
import github.umer0586.sensorserver.util.WifiUtil
import github.umer0586.sensorserver.websocketserver.ServerInfo
import java.net.BindException
import java.net.UnknownHostException

class ServerFragment : Fragment(), ServiceConnection, ServerStateListener
{

    private var sensorService: SensorService? = null
    private lateinit var serviceBindHelper: ServiceBindHelper
    private lateinit var appSettings: AppSettings

    // Button at center to start/stop server
    private lateinit var startButton: MaterialButton

    // Address of server (ws://192.168.2.1:8081)
    private lateinit var serverAddress: TextView

    // card view which holds serverAddress
    private lateinit var cardView: CardView

    //Ripple animation behind startButton
    private lateinit var pulseAnimation: SpinKitView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        Log.i(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_server, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")


        startButton = view.findViewById(R.id.start_button)
        serverAddress = view.findViewById(R.id.server_address)
        pulseAnimation = view.findViewById(R.id.loading_animation)


        cardView = view.findViewById(R.id.card_view)
        appSettings = AppSettings(context)


        serviceBindHelper = ServiceBindHelper(
            context = requireContext(),
            serviceConnection = this,
            service = SensorService::class.java
        )
        lifecycle.addObserver(serviceBindHelper)

        hidePulseAnimation()
        hideServerAddress()

        // we will use tag to determine last state of button
        startButton.setOnClickListener { v ->
            if (v.tag == "stopped")
                startServer()
            else if (v.tag == "started")
                stopServer()
        }
    }

    private fun showServerAddress(address: String)
    {
        cardView.visibility = View.VISIBLE
        serverAddress.visibility = View.VISIBLE
        serverAddress.text = address
        showPulseAnimation()
    }

    private fun startServer()
    {
        Log.d(TAG, "startServer() called")

        if (appSettings.isHotspotOptionEnabled())
        {
            if (WifiUtil.isHotspotEnabled(context))
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
            if (WifiUtil.isWifiEnabled(context))
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

        sensorService?.setServerStateListener(null)
    }

    override fun onServerStarted(serverInfo: ServerInfo)
    {
        Log.d(TAG, "onServerStarted() called")
        UIUtil.runOnUiThread {
            showServerAddress("ws://" + serverInfo.ipAddress + ":" + serverInfo.port)
            showPulseAnimation()

            startButton.tag = "started"
            startButton.text = "STOP"

            showMessage("Server started")
        }
    }

    override fun onServerStopped()
    {
        Log.d(TAG, "onServerStopped() called ")

        UIUtil.runOnUiThread {

            hideServerAddress()
            hidePulseAnimation()

            startButton.tag = "stopped"
            startButton.text = "START"

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

            startButton.tag = "stopped"
            startButton.text = "START"

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
            startButton.tag = "started"
            startButton.text = "STOP"
        }
    }

    private fun showPulseAnimation()
    {
        pulseAnimation.visibility = View.VISIBLE
    }

    private fun hidePulseAnimation()
    {
        pulseAnimation.visibility = View.INVISIBLE
    }

    private fun hideServerAddress()
    {
        cardView.visibility = View.GONE
        serverAddress.visibility = View.GONE
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder)
    {
        val localBinder = service as LocalBinder
        sensorService = localBinder.service

        sensorService?.setServerStateListener(this)
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

    private fun showMessage(message: String)
    {

        view?.let{

            Snackbar.make(it, message, Snackbar.LENGTH_SHORT).apply{
                //R.id.nav_bar is in FragmentNavigationActivity
                setAnchorView(R.id.nav_bar)

            }.show()

        }

    }

    companion object
    {
        private val TAG: String = ServerFragment::class.java.simpleName
    }
}