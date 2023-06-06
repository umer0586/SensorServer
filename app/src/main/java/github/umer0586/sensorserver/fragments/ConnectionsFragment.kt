package github.umer0586.sensorserver.fragments

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.fragments.customadapters.ConnectionsRecyclerViewAdapter
import github.umer0586.sensorserver.service.SensorService
import github.umer0586.sensorserver.service.SensorService.LocalBinder
import github.umer0586.sensorserver.service.ServiceBindHelper
import github.umer0586.sensorserver.util.UIUtil
import org.java_websocket.WebSocket
import java.util.*

/**
 * TODO: functionality to allow user to close all connections (using button in action bar)
 */
class ConnectionsFragment : Fragment(), ServiceConnection
{

    private var sensorService: SensorService? = null
    private lateinit var serviceBindHelper: ServiceBindHelper
    private lateinit var recyclerView: RecyclerView
    private lateinit var connectionsRecyclerViewAdapter: ConnectionsRecyclerViewAdapter
    private val webSockets = ArrayList<WebSocket>()
    private lateinit var noConnectionsText: TextView

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View?
    {
        Log.d(TAG, "onCreateView()")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connections, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = view.findViewById(R.id.recycler_view)
        recyclerView.setLayoutManager(LinearLayoutManager(context))

        connectionsRecyclerViewAdapter = ConnectionsRecyclerViewAdapter(webSockets)
        recyclerView.setAdapter(connectionsRecyclerViewAdapter)


        noConnectionsText = view.findViewById(R.id.no_connections_text)


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


    private fun handleNoConnectionsText()
    {
        if (webSockets.size > 0) UIUtil.runOnUiThread {
            noConnectionsText.visibility = View.INVISIBLE
        }
        else UIUtil.runOnUiThread { noConnectionsText.visibility = View.VISIBLE }
    }

    companion object
    {
        private val TAG: String = ConnectionsFragment::class.java.getSimpleName()
    }
}