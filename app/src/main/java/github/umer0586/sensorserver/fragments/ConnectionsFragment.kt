package github.umer0586.sensorserver.fragments

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import github.umer0586.sensorserver.databinding.FragmentConnectionsBinding
import github.umer0586.sensorserver.fragments.customadapters.ConnectionsRecyclerViewAdapter
import github.umer0586.sensorserver.service.SensorService
import github.umer0586.sensorserver.service.SensorService.LocalBinder
import github.umer0586.sensorserver.service.ServiceBindHelper
import github.umer0586.sensorserver.util.UIUtil
import org.java_websocket.WebSocket

/**
 * TODO: functionality to allow user to close all connections (using button in action bar)
 */
class ConnectionsFragment : Fragment(), ServiceConnection
{

    private var sensorService: SensorService? = null
    private lateinit var serviceBindHelper: ServiceBindHelper

    private lateinit var connectionsRecyclerViewAdapter: ConnectionsRecyclerViewAdapter
    private val webSockets = ArrayList<WebSocket>()


    private var _binding : FragmentConnectionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View?
    {
        Log.d(TAG, "onCreateView()")

        _binding = FragmentConnectionsBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.recyclerView.setLayoutManager(LinearLayoutManager(context))

        connectionsRecyclerViewAdapter = ConnectionsRecyclerViewAdapter(webSockets)
        binding.recyclerView.setAdapter(connectionsRecyclerViewAdapter)


        serviceBindHelper = ServiceBindHelper(
            context = requireContext(),
            serviceConnection = this,
            service = SensorService::class.java
        )
        lifecycle.addObserver(serviceBindHelper)
    }

    override fun onPause()
    {
        super.onPause()
        Log.d(TAG, "onPause()")

        sensorService?.connectionsChangeListener = null
    }

    override fun onDestroy()
    {
        super.onDestroy()
        lifecycle.removeObserver(serviceBindHelper)
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder)
    {
        val localBinder = service as LocalBinder
        sensorService = localBinder.service


        sensorService?.connectionsChangeListener = { webSockets ->

            this.webSockets.clear()
            this.webSockets.addAll(webSockets)

            handleNoConnectionsText()
            UIUtil.runOnUiThread { connectionsRecyclerViewAdapter.notifyDataSetChanged() }
        }


        handleNoConnectionsText()


        sensorService?.getConnectedClients().let { webSockets ->
            this.webSockets.clear()
            if (webSockets != null)
            {
                this.webSockets.addAll(webSockets)
            }

          connectionsRecyclerViewAdapter.notifyDataSetChanged()

        }


    }

    override fun onServiceDisconnected(name: ComponentName)
    {
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun handleNoConnectionsText()
    {
        if (webSockets.size > 0) UIUtil.runOnUiThread {
            binding.noConnectionsText.visibility = View.INVISIBLE
        }
        else UIUtil.runOnUiThread { binding.noConnectionsText.visibility = View.VISIBLE }
    }

    companion object
    {
        private val TAG: String = ConnectionsFragment::class.java.getSimpleName()
    }
}