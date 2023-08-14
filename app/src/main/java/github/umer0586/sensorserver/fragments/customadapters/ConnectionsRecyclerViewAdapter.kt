package github.umer0586.sensorserver.fragments.customadapters

import android.hardware.Sensor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.fragments.customadapters.ConnectionsRecyclerViewAdapter.MyViewHolder
import github.umer0586.sensorserver.websocketserver.GPS
import github.umer0586.sensorserver.websocketserver.SensorWebSocketServer
import github.umer0586.sensorserver.websocketserver.TouchSensors
import net.cachapa.expandablelayout.ExpandableLayout
import org.java_websocket.WebSocket

class ConnectionsRecyclerViewAdapter(private val webSockets: List<WebSocket>) :  RecyclerView.Adapter<MyViewHolder>()
{

    // class to hold reference to items in each list item
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view)
    {

        // address of connected websocket client
        var clientAddress: AppCompatTextView

        // A text which user can tap to close websocket connection
        var closeConnection: AppCompatTextView

        //A text which user can tap to see associated sensors with websocket connection
        var expand: AppCompatImageView

        // List of sensors associated with websocket connection
        var sensorDetails: AppCompatTextView

        // Expandable view which hides and reveals sensorDetails
        var expandableLayout: ExpandableLayout

        init
        {
            clientAddress = view.findViewById(R.id.client_address)
            expand = view.findViewById(R.id.expand)
            sensorDetails = view.findViewById(R.id.sensors_detail)
            closeConnection = view.findViewById(R.id.close_connection)
            expandableLayout = view.findViewById(R.id.expandable_layout)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): MyViewHolder
    {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.item_connection, viewGroup, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: MyViewHolder, position: Int)
    {
        val webSocket = webSockets[position]
        viewHolder.clientAddress.text = webSocket.remoteSocketAddress.toString()

        if (webSocket.getAttachment<Any>() is Sensor)
            viewHolder.sensorDetails.text = (webSocket.getAttachment<Any>() as Sensor).name

        else if (webSocket.getAttachment<Any>() is TouchSensors)
            viewHolder.sensorDetails.text = "Touch sensor"

        else if (webSocket.getAttachment<Any>() is ArrayList<*>)
        {
            var detail = ""
            for (sensor in webSocket.getAttachment<Any>() as ArrayList<Sensor>)
                detail += sensor.name + "\n"

            viewHolder.sensorDetails.setText(detail.trim { it <= ' ' })
        }
        else if (webSocket.getAttachment<Any>() is GPS)
            viewHolder.sensorDetails.text =  "GPS"

        viewHolder.closeConnection.setOnClickListener { v ->
            MaterialAlertDialogBuilder(v.context)
                .setTitle("Websocket Connection")
                .setMessage("Close Connection?")
                .setPositiveButton("Yes") { _, _ ->
                    webSocket.close(
                        SensorWebSocketServer.CLOSE_CODE_CONNECTION_CLOSED_BY_APP_USER,
                        "Connection closed by App user"
                    )
                }
                .setNegativeButton("No") {
                        dialogInterface, _ -> dialogInterface.dismiss()
                }
                .setCancelable(false)
                .show()
        }
        viewHolder.expand.setOnClickListener {
            viewHolder.expandableLayout.toggle()

            if (viewHolder.expandableLayout.isExpanded)
                viewHolder.expand.setImageResource(R.drawable.ic_expand_up)
            else
                viewHolder.expand.setImageResource(R.drawable.ic_expand_down)
        }
    }

    override fun getItemCount(): Int
    {
        return webSockets.size
    }
}