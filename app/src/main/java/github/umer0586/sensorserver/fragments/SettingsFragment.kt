package github.umer0586.sensorserver.fragments

import android.content.Context
import android.content.DialogInterface
import android.net.wifi.WifiManager
import android.os.Bundle
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.google.android.material.snackbar.Snackbar
import github.umer0586.sensorserver.R
import github.umer0586.sensorserver.customextensions.getHotspotIp
import github.umer0586.sensorserver.customextensions.isHotSpotEnabled
import github.umer0586.sensorserver.setting.AppSettings

class SettingsFragment : PreferenceFragmentCompat()
{


    private lateinit var appSettings: AppSettings

    private var  hotspotPref : SwitchPreferenceCompat? = null
    private var  localHostPref : SwitchPreferenceCompat? = null
    private var  allInterfacesPref : SwitchPreferenceCompat? = null
    private var  discoverablePref : SwitchPreferenceCompat? = null


    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
    {
        setPreferencesFromResource(R.xml.settings_preference, rootKey)
        appSettings = AppSettings(requireContext())

        handleWebsocketPortNoPreference()
        handleHttpPortPreference()
        handleLocalHostPreference()
        handleAllInterfacesPreference()
        handleSamplingRatePreference()
        handleHotspotPref()
        handleDiscoverablePref()

    }

    private fun handleDiscoverablePref() {
        discoverablePref = findPreference(getString(R.string.pref_key_discoverable))
        discoverablePref?.isChecked = appSettings.isDiscoverableEnabled()

        discoverablePref?.setOnPreferenceChangeListener { _, newValue ->
            appSettings.saveDiscoverable(newValue as Boolean)

            if(newValue == true)
            {
                localHostPref?.apply {
                    isChecked = false
                    appSettings.enableLocalHostOption(false)
                }
            }

            return@setOnPreferenceChangeListener true
        }
    }

    private fun handleHttpPortPreference()
    {
        val httpPortNoPref = findPreference<EditTextPreference>(getString(R.string.pref_key_http_port_no))
        //websocketPortPref?.summary = appSettings.getPortNo().toString()

        httpPortNoPref?.setOnBindEditTextListener { editText: EditText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        httpPortNoPref?.setOnPreferenceChangeListener { _, newValue ->
            try
            {
                val portNo: Int = newValue.toString().toInt()
                if (portNo >= 1024 && portNo <= 49151)
                {
                    if(portNo == appSettings.getWebsocketPortNo())
                    {
                        showAlertDialog("$portNo is already set for WebSocket server")
                        return@setOnPreferenceChangeListener false
                    }
                    appSettings.saveHttpPortNo(portNo)
                    return@setOnPreferenceChangeListener true
                }
                else
                {
                    showAlertDialog("Please Select valid port No")
                    return@setOnPreferenceChangeListener false
                }
            }
            catch (e: NumberFormatException)
            {
                e.printStackTrace()
                showAlertDialog("Please Select valid port No")
                return@setOnPreferenceChangeListener false
            }
        }
    }

    private fun handleHotspotPref()
    {
        val wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        hotspotPref = findPreference(getString(R.string.pref_key_hotspot))

        // sync setting interface with previously saved preference
        if(wifiManager.isHotSpotEnabled() && appSettings.isHotspotOptionEnabled())
        {

            hotspotPref?.apply {
                summary = wifiManager.getHotspotIp()
                isChecked = true
            }


        }

        hotspotPref?.setOnPreferenceChangeListener { _, newValue ->

            val newState = newValue as Boolean

            //User disabled the switch
            if (newState == false)
            {
                appSettings.enableHotspotOption(false)
                return@setOnPreferenceChangeListener true //persist switch state without doing anything
            }
            if (newState == true)
            {
                if (wifiManager.isHotSpotEnabled())
                {
                    appSettings.enableHotspotOption(true)
                    hotspotPref?.summary = wifiManager.getHotspotIp()

                    localHostPref?.apply {
                        isChecked = false
                        appSettings.enableLocalHostOption(false)
                    }

                    allInterfacesPref?.apply {
                        isChecked = false
                        appSettings.listenOnAllInterfaces(false)
                    }


                    return@setOnPreferenceChangeListener true
                }
                else
                {
                    Snackbar.make(requireView(), "Please enable hotspot", Snackbar.LENGTH_SHORT)
                        .show()
                    appSettings.enableHotspotOption(false)
                    return@setOnPreferenceChangeListener false
                }
            }

            return@setOnPreferenceChangeListener true

        }

    }

    private fun handleLocalHostPreference()
    {
        localHostPref = findPreference(getString(R.string.pref_key_localhost))

        localHostPref?.setOnPreferenceChangeListener { _, newValue ->
            val newState = newValue as Boolean
            appSettings.enableLocalHostOption(newState)

            if (newState == true)
            {
                hotspotPref?.apply {
                    isChecked = false
                    appSettings.enableHotspotOption(false)
                }

                allInterfacesPref?.apply {
                    isChecked = false
                    appSettings.listenOnAllInterfaces(false)
                }

                discoverablePref?.apply {
                    isChecked = false
                    appSettings.saveDiscoverable(false)
                }
            }


            return@setOnPreferenceChangeListener true
        }


    }


    private fun handleAllInterfacesPreference()
    {
        allInterfacesPref = findPreference(getString(R.string.pref_key_all_interface))
        allInterfacesPref?.isChecked = appSettings.isAllInterfaceOptionEnabled()


        allInterfacesPref?.setOnPreferenceChangeListener { preference, newValue ->
            val newState = newValue as Boolean
            appSettings.listenOnAllInterfaces(newState)

            if (newState == true)
            {
                hotspotPref?.apply {
                    isChecked = false
                    appSettings.enableHotspotOption(false)
                }

                localHostPref?.apply {
                    isChecked = false
                    appSettings.enableLocalHostOption(false)
                }
            }

            return@setOnPreferenceChangeListener true
        }


    }
    private fun handleWebsocketPortNoPreference()
    {
        val websocketPortPref = findPreference<EditTextPreference>(getString(R.string.pref_key_websocket_port_no))
        //websocketPortPref?.summary = appSettings.getPortNo().toString()

        websocketPortPref?.setOnBindEditTextListener { editText: EditText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        websocketPortPref?.setOnPreferenceChangeListener { _, newValue ->
            try
            {
                val portNo: Int = newValue.toString().toInt()
                if (portNo >= 1024 && portNo <= 49151)
                {
                    if(portNo == appSettings.getHttpPortNo()){
                        showAlertDialog("$portNo is already set for Http server")
                        return@setOnPreferenceChangeListener false
                    }
                    appSettings.saveWebsocketPortNo(portNo)
                    return@setOnPreferenceChangeListener true
                }
                else
                {
                    showAlertDialog("Please Select valid port No")
                    return@setOnPreferenceChangeListener false
                }
            }
            catch (e: NumberFormatException)
            {
                e.printStackTrace()
                showAlertDialog("Please Select valid port No")
                return@setOnPreferenceChangeListener false
            }
        }
    }

    private fun handleSamplingRatePreference()
    {
        val samplingRatePref = findPreference<EditTextPreference>(getString(R.string.pref_key_sampling_rate))
        //samplingRatePref?.summary = appSettings.getSamplingRate().toString()

        val dialogText = """
                The data delay (or sampling rate) controls the interval at which sensor events are sent to application. Change this value before starting a Server
                <br><br>
                <font color="#689f38"><b>Note : </b></font> <i>The delay that you specify is only a suggested delay. The Android system and other applications can alter this delay.</i>
                <br><br>
                 Normal Rate : <font color="#5c6bc0"><b>200000</b>μs</font>
                <br>
                 Fastest Rate : <font color="#5c6bc0"><b>0</b>μs</font>
                <br><br>
                 Enter value in <font color="#5c6bc0"><b>Microseconds</b></font>
                """.trimIndent()

        samplingRatePref?.dialogMessage = HtmlCompat.fromHtml(dialogText,HtmlCompat.FROM_HTML_MODE_LEGACY)
        samplingRatePref?.setOnBindEditTextListener { editText: EditText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_SIGNED
        }

        samplingRatePref?.setOnPreferenceChangeListener { _, newValue ->
            try
            {
                if (newValue.toString().trim { it <= ' ' }.isEmpty())
                    return@setOnPreferenceChangeListener false

                val samplingRate: Int = newValue.toString().toInt()

                if (samplingRate < 0)
                {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Invalid Input")
                        .setMessage("Negative value not allowed")
                        .setCancelable(false)
                        .setPositiveButton("Okay") { dialog: DialogInterface, id: Int -> dialog.cancel() }
                        .create()
                        .show()
                    return@setOnPreferenceChangeListener false
                }
                appSettings.saveSamplingRate(samplingRate)
                return@setOnPreferenceChangeListener true
            }
            catch (e: NumberFormatException)
            {
                e.printStackTrace()
                AlertDialog.Builder(requireContext())
                    .setTitle("Invalid Input")
                    .setMessage("Value too large")
                    .setCancelable(false)
                    .setPositiveButton("Okay") { dialog: DialogInterface, id: Int -> dialog.cancel() }
                    .create()
                    .show()
                return@setOnPreferenceChangeListener false
            }
        }
    }

    private fun showAlertDialog(message: CharSequence)
    {
        AlertDialog.Builder(requireContext())
            .setTitle("Invalid Port No")
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Okay") { dialog: DialogInterface, id: Int -> dialog.cancel() }
            .create()
            .show()
    }

    companion object
    {
        private val TAG: String = SettingsFragment::class.java.getName()
    }
}