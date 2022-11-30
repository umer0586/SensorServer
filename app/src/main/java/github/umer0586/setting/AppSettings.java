package github.umer0586.setting;

import android.content.Context;
import android.content.SharedPreferences;

import github.umer0586.R;


public class AppSettings {

    private Context context;
    private SharedPreferences sharedPreferences;


    public AppSettings(Context context)
    {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(context.getString(R.string.shared_pref_file),context.MODE_PRIVATE);
    }

    public int getPortNo()
    {
        return sharedPreferences.getInt(context.getString(R.string.pref_key_port_no),8081);
    }

    public int getSamplingRate()
    {
        return sharedPreferences.getInt(context.getString(R.string.pref_key_sampling_rate),200000);
    }

    public boolean isLocalHostOptionEnable()
    {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_localhost),false);
    }



}
