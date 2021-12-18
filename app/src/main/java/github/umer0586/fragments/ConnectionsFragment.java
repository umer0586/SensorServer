package github.umer0586.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.sensorserver.ConnectionInfo;
import github.umer0586.sensorserver.ConnectionInfoListener;


public class ConnectionsFragment extends ListFragment implements ConnectionInfoListener {

    private static final String TAG = ConnectionsFragment.class.getSimpleName();
    private onConnectionItemClickedListener onConnectionItemClickedListener;

    public interface onConnectionItemClickedListener{
        void onConnectionItemClicked(ConnectionInfo connectionInfo);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connections, container, false);
    }


    public void setOnConnectionItemClickedListener(ConnectionsFragment.onConnectionItemClickedListener onConnectionItemClickedListener)
    {
        this.onConnectionItemClickedListener = onConnectionItemClickedListener;
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);
        ConnectionInfo connectionInfo = (ConnectionInfo) v.getTag();

        if(onConnectionItemClickedListener != null)
            onConnectionItemClickedListener.onConnectionItemClicked(connectionInfo);
    }

    private class ConnectionListAdapter extends ArrayAdapter<ConnectionInfo>{


        public ConnectionListAdapter(@NonNull Context context, ArrayList<ConnectionInfo> connectionInfos)
        {
            super(context, R.layout.item_connection, connectionInfos);
            
        }


        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {

            View view;
            if (convertView == null)
            {
                view = getLayoutInflater().inflate(R.layout.item_connection, parent, false);
            } else
            {
                view = convertView;
            }

            ConnectionInfo connectionInfo = getItem(position);

            AppCompatTextView sensorName = view.findViewById(R.id.sensor_name);
            AppCompatTextView usageCount = view.findViewById(R.id.usage_count);

            sensorName.setText( connectionInfo.getSensor().getName() );
            usageCount.setText( connectionInfo.getSensorUsageCount() + "");

            view.setTag(connectionInfo);
            return view;


        }




    }


    @Override
    public void onNewConnectionList(ArrayList<ConnectionInfo> connectionInfos)
    {
        getActivity().runOnUiThread(()->{
            setListAdapter( new ConnectionListAdapter(getContext(),connectionInfos) );
        });

    }


}