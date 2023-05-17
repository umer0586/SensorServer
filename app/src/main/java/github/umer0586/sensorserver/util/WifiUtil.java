package github.umer0586.sensorserver.util;

import android.content.Context;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class WifiUtil {

    public static boolean isHotspotEnabled(Context context)
    {
        WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Method[] wmMethods = wifi.getClass().getDeclaredMethods();

        //is wifi access point (hotspot) enabled
        boolean isWifiAPenabled = false;

        for (Method method : wmMethods)
        {
            if (method.getName().equals("isWifiApEnabled"))
            {

                try
                {
                    method.setAccessible(true); //in the case of visibility change in future APIs
                    isWifiAPenabled = (boolean) method.invoke(wifi);

                } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e)
                {
                    e.printStackTrace();
                }
            }
        }
        return isWifiAPenabled;
    }

    public static boolean isWifiEnabled(Context context)
    {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager.isWifiEnabled();
    }



}
