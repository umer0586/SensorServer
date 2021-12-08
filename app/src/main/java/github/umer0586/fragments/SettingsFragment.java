package github.umer0586.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.EditTextPreference;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

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