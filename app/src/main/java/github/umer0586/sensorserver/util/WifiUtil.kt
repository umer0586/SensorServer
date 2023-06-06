package github.umer0586.sensorserver.util

import android.content.Context
import android.net.wifi.WifiManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

object WifiUtil
{


    fun isHotspotEnabled(context: Context?): Boolean
    {
        val wifi =
            context!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val wmMethods: Array<Method> = wifi.javaClass.getDeclaredMethods()

        //is wifi access point (hotspot) enabled
        var isWifiAPenabled = false
        for (method in wmMethods)
        {
            if (method.name == "isWifiApEnabled")
            {
                try
                {
                    method.isAccessible = true //in the case of visibility change in future APIs
                    isWifiAPenabled = method.invoke(wifi) as Boolean
                }
                catch (e: IllegalArgumentException)
                {
                    e.printStackTrace()
                }
                catch (e: IllegalAccessException)
                {
                    e.printStackTrace()
                }
                catch (e: InvocationTargetException)
                {
                    e.printStackTrace()
                }
            }
        }
        return isWifiAPenabled
    }

    fun isWifiEnabled(context: Context?): Boolean
    {
        val wifiManager =
            context!!.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        return wifiManager.isWifiEnabled
    }
}