package github.umer0586.sensorserver.fragments;

import android.os.Bundle;
import android.text.Html;
import android.text.InputType;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.snackbar.Snackbar;

import github.umer0586.sensorserver.R;
import github.umer0586.sensorserver.setting.AppSettings;
import github.umer0586.sensorserver.util.IpUtil;
import github.umer0586.sensorserver.util.WifiUtil;

public class SettingsFragment extends PreferenceFragmentCompat {


    private final static String TAG = SettingsFragment.class.getName();
    private AppSettings appSettings;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.settings_preference, rootKey);
        appSettings = new AppSettings(getContext());

        handlePortNoPreference();
        handleLocalHostPreference();
        handleSamplingRatePreference();
        handleHotspotPref();


    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(!WifiUtil.isHotspotEnabled(getContext()))
        {
            SwitchPreferenceCompat hotspotPref = findPreference(getString(R.string.pref_key_hotspot));
            hotspotPref.setChecked(false);
            appSettings.enableHotspotOption(false);
        }

    }

    private void handleHotspotPref()
    {
        SwitchPreferenceCompat hotspotPref = findPreference(getString(R.string.pref_key_hotspot));

        hotspotPref.setOnPreferenceChangeListener(((preference, newValue) -> {

            boolean newState = (boolean)newValue;

            //User disabled the switch
            if(newState == false)
            {
                appSettings.enableHotspotOption(false);
                return true; //persist switch state without doing anything
            }

            if(newState == true)
            {

                if (WifiUtil.isHotspotEnabled(getContext()))
                {
                    appSettings.enableHotspotOption(true);
                    hotspotPref.setSummary(IpUtil.getHotspotIPAddress(getContext()));
                    return true;
                }
                else
                {
                    Snackbar.make(getView(),"Please enable hotspot",Snackbar.LENGTH_SHORT).show();
                    appSettings.enableHotspotOption(false);
                    return false;
                }
            }

            return true;
        }));
    }

    private void handleLocalHostPreference()
    {
        SwitchPreferenceCompat localHostPref = findPreference(getString(R.string.pref_key_localhost));
        localHostPref.setOnPreferenceChangeListener((preference, newValue) -> {

            boolean newState = (boolean)newValue;
            appSettings.enableLocalHostOption(newState);
            return true;
        });
    }


    private void handlePortNoPreference()
    {
        EditTextPreference websocketPortPref = findPreference(getString(R.string.pref_key_port_no));

        websocketPortPref.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        });


        websocketPortPref.setOnPreferenceChangeListener((preference, newValue) -> {

            try {

                int portNo = Integer.parseInt(newValue.toString());

                if (portNo >= 1024 && portNo <= 49151)
                {
                    appSettings.savePortNo(portNo);
                    return true;
                }
                else {
                    showAlertDialog("Please Select valid port No");
                    return false;
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
                showAlertDialog("Please Select valid port No");
                return false;
            }


        });
    }

    private void handleSamplingRatePreference()
    {
        EditTextPreference samplingRatePref = findPreference(getString(R.string.pref_key_sampling_rate));
        String dialogText =
                "The data delay (or sampling rate) controls the interval at which sensor events are sent to application. Change this value before starting a Server<br><br>" +
                        "<font color=\"#689f38\"><b>Note : </b></font> <i>The delay that you specify is only a suggested delay. The Android system and other applications can alter this delay.</i><br><br>"+
                        "Normal Rate : <font color=\"#5c6bc0\"><b>200000</b>μs</font><br>" +
                        "Fastest Rate : <font color=\"#5c6bc0\"><b>0</b>μs</font><br><br>" +
                        "Enter value in <font color=\"#5c6bc0\"><b>Microseconds</b></font>";
        samplingRatePref.setDialogMessage(Html.fromHtml(dialogText));

        samplingRatePref.setOnBindEditTextListener(editText -> {
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED);

        });


        samplingRatePref.setOnPreferenceChangeListener((preference, newValue) -> {

            try {

                if(newValue.toString().trim().isEmpty())
                    return false;

                int samplingRate = Integer.parseInt(newValue.toString());

                if(samplingRate < 0)
                {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Invalid Input")
                            .setMessage("Negative value not allowed")
                            .setCancelable(false)
                            .setPositiveButton("Okay", (dialog, id) -> {
                                dialog.cancel();
                            })
                            .create()
                            .show();
                    return false;
                }


                appSettings.saveSamplingRate(samplingRate);
                return true;

            } catch (NumberFormatException e) {
                e.printStackTrace();
                new AlertDialog.Builder(getContext())
                        .setTitle("Invalid Input")
                        .setMessage("Value too large")
                        .setCancelable(false)
                        .setPositiveButton("Okay", (dialog, id) -> {
                            dialog.cancel();
                        })
                        .create()
                        .show();
                return false;
            }


        });
    }



    private void showAlertDialog(CharSequence message)
    {

        new AlertDialog.Builder(getContext())
                .setTitle("Invalid Port No")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Okay", (dialog, id) -> {
                    dialog.cancel();
                })
                .create()
                .show();

    }

}