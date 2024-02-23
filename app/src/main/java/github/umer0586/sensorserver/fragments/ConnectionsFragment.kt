package github.umer0586.sensorserver.fragments

import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import github.umer0586.sensorserver.databinding.FragmentConnectionsBinding
import github.umer0586.sensorserver.fragments.customadapters.ConnectionsRecyclerViewAdapter
import github.umer0586.sensorserver.service.WebsocketService
import github.umer0586.sensorserver.service.WebsocketService.LocalBinder
import github.umer0586.sensorserver.service.ServiceBindHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.java_websocket.WebSocket

/**
 * TODO: functionality to allow user to close all connections (using button in action bar)
 */
class ConnectionsFragment : Fragment()
{

    private var websocketService: WebsocketService? = null
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
            service = WebsocketService::class.java,
            componentLifecycle = lifecycle
        )

        serviceBindHelper.onServiceConnected(this::onServiceConnected)

    }

    override fun onPause()
    {
        super.onPause()
        Log.d(TAG, "onPause()")

        // To prevent memory leak
        websocketService?.onConnectionsChange( callBack = null)
    }


    fun onServiceConnected(binder: IBinder)
    {
        val localBinder = binder as LocalBinder
        websocketService = localBinder.service


        websocketService?.onConnectionsChange{ webSockets ->

            this.webSockets.clear()
            this.webSockets.addAll(webSockets)

            handleNoConnectionsText()
            lifecycleScope.launch(Dispatchers.Main) {
                connectionsRecyclerViewAdapter.notifyDataSetChanged()
            }
        }



        websocketService?.getConnectedClients().let { webSockets ->

            this.webSockets.clear()
            webSockets?.let{this.webSockets.addAll(it)}

            connectionsRecyclerViewAdapter.notifyDataSetChanged()

        }

        handleNoConnectionsText()



    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun handleNoConnectionsText()
    {
        lifecycleScope.launch(Dispatchers.Main) {
            if (webSockets.size > 0)
                binding.noConnectionsText.visibility = View.INVISIBLE
            else
                binding.noConnectionsText.visibility = View.VISIBLE
        }
    }


    companion object
    {
        private val TAG: String = ConnectionsFragment::class.java.getSimpleName()
    }
}