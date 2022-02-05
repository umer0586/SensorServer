package github.umer0586.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import java.util.concurrent.atomic.AtomicBoolean;

import github.umer0586.R;

public class SettingsFragment extends PreferenceFragmentCompat {


    private final static String TAG = SettingsFragment.class.getName();
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
    {
        setPreferencesFromResource(R.xml.settings_preference, rootKey);
        sharedPreferences = getContext().getSharedPreferences(getString(R.string.shared_pref_file),getContext().MODE_PRIVATE);

        handlePortNoPreference();
        handleLocalHostPreference();
        handleSamplingRatePreference();


    }

    private void handleLocalHostPreference()
    {
        SwitchPreferenceCompat localHostPref = findPreference(getString(R.string.pref_key_localhost));
        localHostPref.setOnPreferenceChangeListener((preference, newValue) -> {
            boolean newState = (boolean)newValue;
            sharedPreferences.edit()
                    .putBoolean(getString(R.string.pref_key_localhost),newState)
                    .commit();
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
                    sharedPreferences.edit()
                                     .putInt(getString(R.string.pref_key_port_no),portNo)
                                     .commit();
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
                "The data delay (or sampling rate) controls the interval at which sensor events are sent to application<br><br>" +
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


                    sharedPreferences.edit()
                            .putInt(getString(R.string.pref_key_sampling_rate),samplingRate)
                            .commit();
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