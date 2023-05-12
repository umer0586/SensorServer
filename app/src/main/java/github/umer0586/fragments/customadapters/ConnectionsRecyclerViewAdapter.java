package github.umer0586.fragments.customadapters;

import android.hardware.Sensor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import net.cachapa.expandablelayout.ExpandableLayout;

import org.java_websocket.WebSocket;

import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.sensorserver.SensorWebSocketServer;

public class ConnectionsRecyclerViewAdapter extends RecyclerView.Adapter<ConnectionsRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<WebSocket> webSockets;

    // class to hold reference to items in each list item
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        // address of connected websocket client
        public AppCompatTextView clientAddress;

        // A text which user can tap to close websocket connection
        public AppCompatTextView closeConnection;

        //A text which user can tap to see associated sensors with websocket connection
        public AppCompatImageView expand;

        // List of sensors associated with websocket connection
        public AppCompatTextView sensorDetails;

        // Expandable view which hides and reveals sensorDetails
        public ExpandableLayout expandableLayout;


        public MyViewHolder(@NonNull View view)
        {
            super(view);

            clientAddress = view.findViewById(R.id.client_address);
            expand = view.findViewById(R.id.expand);
            sensorDetails = view.findViewById(R.id.sensors_detail);
            closeConnection = view.findViewById(R.id.close_connection);
            expandableLayout = view.findViewById(R.id.expandable_layout);

        }
    }

    public ConnectionsRecyclerViewAdapter(ArrayList<WebSocket> webSockets)
    {
        this.webSockets = webSockets;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
    {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_connection, viewGroup, false);

        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder viewHolder, int position)
    {
        WebSocket webSocket = webSockets.get(position);

        viewHolder.clientAddress.setText(webSocket.getRemoteSocketAddress().toString());

        if(webSocket.getAttachment() instanceof Sensor)
            viewHolder.sensorDetails.setText( ((Sensor)webSocket.getAttachment()).getName() );

        else if(webSocket.getAttachment() instanceof ArrayList)
        {
            String detail = "";
            for(Sensor sensor : (ArrayList<Sensor>)webSocket.getAttachment() )
                detail += sensor.getName() + "\n";
            viewHolder.sensorDetails.setText(detail.trim());
        }


        viewHolder.closeConnection.setOnClickListener(v->{

            new MaterialAlertDialogBuilder(v.getContext())
                    .setTitle("Websocket Connection")
                    .setMessage("Close Connection?")
                    .setPositiveButton("Yes",(dialog, which) -> {
                        webSocket.close(SensorWebSocketServer.CLOSE_CODE_CONNECTION_CLOSED_BY_APP_USER,"Connection closed by App user");
                    })
                    .setNegativeButton("No", (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                    })
                    .setCancelable(false)
                    .show();


        });

        viewHolder.expand.setOnClickListener(v->{
            viewHolder.expandableLayout.toggle();
            if(viewHolder.expandableLayout.isExpanded())
                viewHolder.expand.setImageResource(R.drawable.ic_expand_up);
            else
                viewHolder.expand.setImageResource(R.drawable.ic_expand_down);
        });


    }

    @Override
    public int getItemCount()
    {
        return webSockets.size();
    }



}
