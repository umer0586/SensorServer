package github.umer0586.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.DialogCompat;
import androidx.fragment.app.ListFragment;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import github.umer0586.R;
import github.umer0586.util.SensorUtil;

import java.util.ArrayList;
import java.util.List;


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

        List<Sensor> availableSensors = SensorUtil.getInstance(getContext()).getAvailableSensors();
        SensorsListAdapter sensorsListAdapter = new SensorsListAdapter(getContext(),availableSensors);
        getListView().setAdapter(sensorsListAdapter);
    }

    @Override
    public void onListItemClick(@NonNull ListView l, @NonNull View v, int position, long id)
    {
        super.onListItemClick(l, v, position, id);

        Sensor sensor = (Sensor)v.getTag();

        String sensorInfo = "";
        sensorInfo += "Name : " + sensor.getName() + "\n";
        sensorInfo += "Type : " + sensor.getStringType() + "\n";
        sensorInfo += "MinDelay : " + sensor.getMinDelay() + "\n";
        sensorInfo += "MaxDelay : " + sensor.getMaxDelay() + "\n";
        sensorInfo += "MaxRange : " + sensor.getMaximumRange() + "\n";
        sensorInfo += "Resolution : " + sensor.getResolution() + "\n";
        sensorInfo += "Power : " + sensor.getPower() + "\n";
        sensorInfo += "Vendor : " + sensor.getVendor() + "\n";
        sensorInfo += "Version : " + sensor.getVersion() + "\n";
        sensorInfo += "WakeUp sensor : " + sensor.isWakeUpSensor()+ "\n";


        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Sensor Info")
                .setMessage(sensorInfo)
                .show();

    }

    private class SensorsListAdapter extends ArrayAdapter<Sensor>{

        public SensorsListAdapter(@NonNull Context context, @NonNull List<Sensor> sensors)
        {
            super(context, R.layout.item_sensor, sensors);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {

            View view;
            if (convertView == null)
                view = getLayoutInflater().inflate(R.layout.item_sensor, parent, false);
             else
                view = convertView;

            Sensor sensor = getItem(position);

            AppCompatTextView sensorName = view.findViewById(R.id.sensor_name);
            AppCompatTextView sensorType = view.findViewById(R.id.sensor_type);

            sensorName.setText(sensor.getName());
            sensorType.setText(Html.fromHtml("<font color=\"#5c6bc0\"><b>Type = </b></font>" + sensor.getStringType()));



            view.setTag(sensor);
            return view;

        }


    }
}