package github.umer0586.sensorserver.fragments

import android.content.*
import android.hardware.Sensor
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.ListFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.fragments.AvailableSensorsFragment
import github.umer0586.sensorserver.util.SensorUtil

class AvailableSensorsFragment : ListFragment()
{

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
    {
        Log.i(TAG, "onCreateView: ")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_available_sensors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)
        Log.i(TAG, "onViewCreated: ")

        val availableSensors: List<Sensor> = SensorUtil.getInstance( context )!!.availableSensors

        val sensorsListAdapter = SensorsListAdapter(requireContext(), availableSensors)
        listView.adapter = sensorsListAdapter
    }

    override fun onListItemClick(l: ListView, v: View, position: Int, id: Long)
    {
        super.onListItemClick(l, v, position, id)

        val sensor = v.tag as Sensor

        var sensorInfo = ""
        sensorInfo += """
            Name : ${sensor.name}
           
            """.trimIndent()
        sensorInfo += """
            MinDelay : ${sensor.minDelay}μs
            
            """.trimIndent()
        sensorInfo += """
            MaxDelay : ${sensor.maxDelay}μs
            
            """.trimIndent()
        sensorInfo += """
            MaxRange : ${sensor.maximumRange}
            
            """.trimIndent()
        sensorInfo += """
            Resolution : ${sensor.resolution}
            
            """.trimIndent()
        sensorInfo += """
            Reporting Mode : ${getSensorReportingModeString(sensor.reportingMode)}
            
            """.trimIndent()
        sensorInfo += """
            Power : ${sensor.power}mA
            
            """.trimIndent()
        sensorInfo += """
            Vendor : ${sensor.vendor}
            
            """.trimIndent()
        sensorInfo += """
            Version : ${sensor.version}
            
            """.trimIndent()
        sensorInfo += """
            WakeUp sensor : ${sensor.isWakeUpSensor}
            
            """.trimIndent()
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Sensor Info")
            .setMessage(sensorInfo)
            .show()
    }

    private inner class SensorsListAdapter(context: Context, sensors: List<Sensor>) :  ArrayAdapter<Sensor?>(context, R.layout.item_sensor, sensors)
    {

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View
        {
            val view: View = convertView ?: layoutInflater.inflate( R.layout.item_sensor, parent,false)

            val sensor = getItem(position)
            val sensorName = view.findViewById<AppCompatTextView>(R.id.sensor_name)
            val sensorType = view.findViewById<AppCompatTextView>(R.id.sensor_type)

            sensor?.let {

                sensorName.text = sensor.name
                sensorType.text =
                    Html.fromHtml("<font color=\"#5c6bc0\"><b>Type = </b></font>" + sensor.stringType)

            }

            view.tag = sensor
            return view
        }
    }

    companion object
    {

        private val TAG: String = AvailableSensorsFragment::class.java.getSimpleName()
        private fun getSensorReportingModeString(reportingMode: Int): String
        {
            var reportingModeString = ""
            when (reportingMode)
            {
                Sensor.REPORTING_MODE_CONTINUOUS -> reportingModeString = "Continuous"
                Sensor.REPORTING_MODE_ON_CHANGE -> reportingModeString = "On Change"
                Sensor.REPORTING_MODE_ONE_SHOT -> reportingModeString = "One Shot"
                Sensor.REPORTING_MODE_SPECIAL_TRIGGER -> reportingModeString = "Special Trigger"
            }
            return reportingModeString
        }
    }
}