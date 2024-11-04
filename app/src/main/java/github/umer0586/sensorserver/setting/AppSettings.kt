package github.umer0586.sensorserver.setting

import android.content.Context
import android.content.SharedPreferences
import github.umer0586.sensorserver.R

class AppSettings(context: Context)
{


    private val context: Context
    private val sharedPreferences: SharedPreferences

    init
    {
        this.context = context.applicationContext
        sharedPreferences = context.getSharedPreferences(
            context.getString(R.string.shared_pref_file),
            Context.MODE_PRIVATE
        )
    }

    fun saveWebsocketPortNo(portNo: Int)
    {
        sharedPreferences.edit()
            .putInt(context.getString(R.string.pref_key_websocket_port_no), portNo)
            .apply()
    }

    fun getWebsocketPortNo(): Int
    {
        return sharedPreferences.getInt(
            context.getString(R.string.pref_key_websocket_port_no),
            DEFAULT_WEBSOCKET_PORT_NO
        )
    }

    fun saveHttpPortNo(portNo: Int)
    {
        sharedPreferences.edit()
                .putInt(context.getString(R.string.pref_key_http_port_no), portNo)
                .apply()
    }

    fun getHttpPortNo(): Int
    {
        return sharedPreferences.getInt(
                context.getString(R.string.pref_key_http_port_no),
                DEFAULT_HTTP_PORT_NO
        )
    }

    fun saveSamplingRate(samplingRate: Int)
    {
        sharedPreferences.edit()
            .putInt(context.getString(R.string.pref_key_sampling_rate), samplingRate)
            .apply()
    }

    fun getSamplingRate(): Int
    {
        return sharedPreferences.getInt(
            context.getString(R.string.pref_key_sampling_rate),
            DEFAULT_SAMPLING_RATE
        )
    }

    fun enableLocalHostOption(state: Boolean)
    {
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.pref_key_localhost), state)
            .apply()
    }

    fun isLocalHostOptionEnable(): Boolean
    {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_localhost), false)
    }

    fun enableHotspotOption(state: Boolean)
    {
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.pref_key_hotspot), state)
            .apply()
    }

    fun isHotspotOptionEnabled(): Boolean
    {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_hotspot), false)
    }

    fun listenOnAllInterfaces(state : Boolean)
    {
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.pref_key_all_interface), state)
            .apply()
    }

    fun isAllInterfaceOptionEnabled() : Boolean
    {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_all_interface), false)
    }

    fun saveDiscoverable(state: Boolean)
    {
        sharedPreferences.edit()
            .putBoolean(context.getString(R.string.pref_key_discoverable), state)
            .apply()

    }

    fun isDiscoverableEnabled() : Boolean
    {
        return sharedPreferences.getBoolean(context.getString(R.string.pref_key_discoverable), DEFAULT_DISCOVERABLE)
    }

    companion object
    {


        private const val DEFAULT_WEBSOCKET_PORT_NO = 8080
        private const val DEFAULT_HTTP_PORT_NO = 9090
        private const val DEFAULT_SAMPLING_RATE = 200000
        private const val DEFAULT_DISCOVERABLE = false
    }
}