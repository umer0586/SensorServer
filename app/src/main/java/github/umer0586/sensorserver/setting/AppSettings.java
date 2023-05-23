package github.umer0586.sensorserver.setting;

import android.content.Context;
import android.content.SharedPreferences;

import github.umer0586.sensorserver.R;


public class AppSettings {

    private Context context;
    private SharedPreferences sharedPreferences;


    public AppSettings(Context context)
    {
        this.context = context.getApplicationContext();
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_pref_file),context.MODE_PRIVATE);
    }

    public void savePortNo(int portNo)
    {
        sharedPreferences.edit()
                .putInt(context.getString(R.string.pref_key_port_no),portNo)
                .apply();
    }

    public int getPortNo()
    {
        return sharedPreferences.getInt(context.getString(R.string.pref_key_port_no),8081);
    }

    public void saveSamplingRate(int samplingRate)
    {
        sharedPreferences.edit()
                .putInt(context.getString(R.string.pref_key_sampling_rate),samplingRate)
                .apply();
    }

    public int getSamplingRate()
    {
        return sharedPreferences.getInt(context.getString(R.string.pref_key_sampling_rate),200000);
    }

    public void enableLocalHostOption(boolean state)
    {
        sharedPreferences.edit()
                .putBoolean(context.getString(R.string.pref_key_localhost),state)
                .apply();
    }

    public boolean isLocalHostOptionEnable()
    {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_localhost),false);
    }

    public void enableHotspotOption(boolean state)
    {
        sharedPreferences.edit()
                .putBoolean(context.getString(R.string.pref_key_hotspot), state)
                .apply();
    }

    public boolean isHotspotOptionEnabled()
    {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_hotspot), false);
    }



}
