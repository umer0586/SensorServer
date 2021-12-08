package github.umer0586.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import github.umer0586.sensorserver.ConnectionInfo;
import github.umer0586.R;
import github.umer0586.sensorserver.OnNewConnectionInfoListListener;

import java.util.ArrayList;


public class ConnectionsFragment extends ListFragment implements OnNewConnectionInfoListListener {

    private static final String TAG = ConnectionsFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_connections, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated: ");

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

            ConnectionInfo info = getItem(position);

            TextView sensorName = view.findViewById(R.id.sensor_name);
            TextView usageCount = view.findViewById(R.id.usage_count);

            sensorName.setText( info.getSensor().getStringType() );
            usageCount.setText( info.getSensorUsageCount() + "");


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

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
    }
}