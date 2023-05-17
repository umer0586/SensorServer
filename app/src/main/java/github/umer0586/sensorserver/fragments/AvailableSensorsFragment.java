package github.umer0586.sensorserver.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.os.Bundle;
import android.text.Html;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.util.List;

import github.umer0586.sensorserver.R;
import github.umer0586.sensorserver.util.SensorUtil;


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
        sensorInfo += "MinDelay : " + sensor.getMinDelay() + "μs\n";
        sensorInfo += "MaxDelay : " + sensor.getMaxDelay() + "μs\n";
        sensorInfo += "MaxRange : " + sensor.getMaximumRange() + "\n";
        sensorInfo += "Resolution : " + sensor.getResolution() + "\n";
        sensorInfo += "Reporting Mode : " + getSensorReportingModeString(sensor.getReportingMode()) + "\n";
        sensorInfo += "Power : " + sensor.getPower() + "mA\n";
        sensorInfo += "Vendor : " + sensor.getVendor() + "\n";
        sensorInfo += "Version : " + sensor.getVersion() + "\n";
        sensorInfo += "WakeUp sensor : " + sensor.isWakeUpSensor()+ "\n";


        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Sensor Info")
                .setMessage(sensorInfo)
                .show();

    }

    private static String getSensorReportingModeString(int reportingMode)
    {
            String reportingModeString = "";

            switch (reportingMode)
            {
                case Sensor.REPORTING_MODE_CONTINUOUS:
                    reportingModeString = "Continuous";
                    break;
                case Sensor.REPORTING_MODE_ON_CHANGE:
                    reportingModeString = "On Change";
                    break;
                case Sensor.REPORTING_MODE_ONE_SHOT:
                    reportingModeString = "One Shot";
                    break;
                case Sensor.REPORTING_MODE_SPECIAL_TRIGGER:
                    reportingModeString = "Special Trigger";
                    break;

            }

            return reportingModeString;
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