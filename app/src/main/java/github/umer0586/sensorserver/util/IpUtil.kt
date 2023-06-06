package github.umer0586.sensorserver.util

import android.content.Context
import android.net.wifi.WifiManager
import java.math.BigInteger
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteOrder

object IpUtil
{


    fun getWifiIpAddress(context: Context): String?
    {
        val wifiManager = context.getSystemService(Context.WIFI_SERVICE) as WifiManager
        var ipAddress = wifiManager.connectionInfo.ipAddress

        // Convert little-endian to big-endianif needed
        if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
        {
            ipAddress = Integer.reverseBytes(ipAddress)
        }
        val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
        val ipAddressString: String?
        ipAddressString = try
        {
            InetAddress.getByAddress(ipByteArray).hostAddress
        }
        catch (ex: UnknownHostException)
        {
            null
        }
        return ipAddressString
    }

    fun getHotspotIPAddress(context: Context?): String?
    {
        if (WifiUtil.isHotspotEnabled(context) == false) return null
        var ipAddress: String? = null
        try
        {
            val enumNetworkInterfaces = NetworkInterface.getNetworkInterfaces()
            while (enumNetworkInterfaces.hasMoreElements())
            {
                val networkInterface = enumNetworkInterfaces.nextElement()
                val enumInetAddress = networkInterface.inetAddresses
                while (enumInetAddress.hasMoreElements())
                {
                    val inetAddress = enumInetAddress.nextElement()
                    if (inetAddress.isSiteLocalAddress)
                    {
                        ipAddress = inetAddress.hostAddress
                    }
                }
            }
        }
        catch (e: SocketException)
        {
            // TODO Auto-generated catch block
            e.printStackTrace()
            ipAddress = null
        }
        return ipAddress
    }
}