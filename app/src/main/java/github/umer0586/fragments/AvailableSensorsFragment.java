package github.umer0586.fragments;

import android.hardware.Sensor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.ListFragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import github.umer0586.R;
import github.umer0586.util.SensorUtil;

import java.util.ArrayList;


public class AvailableSensorsFragment extends ListFragment {

    private static final String TAG = AvailableSensorsFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        Log.i(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_available_sensors, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);
        Log.i(TAG, "onViewCreated: ");
        
        ArrayList<String> sensorsNameAndType = new ArrayList<>();

        for(Sensor sensor :  SensorUtil.getInstance(getContext()).getAvailableSensors())
        {
            sensorsNameAndType.add( sensor.getName() + "\n" + "Type = " + sensor.getStringType());
        }

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                getContext(), R.layout.item_list_view , R.id.text_view , sensorsNameAndType
        );

        getListView().setAdapter(arrayAdapter);
    }

    @Override
    public void onDestroyView()
    {
        super.onDestroyView();
        Log.i(TAG, "onDestroyView: ");
    }
}