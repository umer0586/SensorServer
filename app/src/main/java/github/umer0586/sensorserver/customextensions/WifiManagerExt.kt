package github.umer0586.sensorserver.customextensions

import android.net.wifi.WifiManager
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.math.BigInteger
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.net.UnknownHostException
import java.nio.ByteOrder

fun WifiManager.getIp() : String?
{
    var ipAddress = this.connectionInfo.ipAddress

    // Convert little-endian to big-endianif needed
    if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN)
    {
        ipAddress = Integer.reverseBytes(ipAddress)
    }
    val ipByteArray = BigInteger.valueOf(ipAddress.toLong()).toByteArray()
    val ipAddressString: String? = try
    {
        InetAddress.getByAddress(ipByteArray).hostAddress
    }
    catch (ex: UnknownHostException)
    {
        null
    }
    return ipAddressString
}

fun WifiManager.getHotspotIp() : String?
{
    if (this.isHotSpotEnabled() == false)
        return null

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

fun WifiManager.isHotSpotEnabled() : Boolean
{

    val wmMethods: Array<Method> = this.javaClass.getDeclaredMethods()

    //is wifi access point (hotspot) enabled
    var isWifiAPenabled = false
    for (method in wmMethods)
    {
        if (method.name == "isWifiApEnabled")
        {
            try
            {
                method.isAccessible = true //in the case of visibility change in future APIs
                isWifiAPenabled = method.invoke(this) as Boolean
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