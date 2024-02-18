package github.umer0586.sensorserver.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.core.content.ContextCompat

class BroadcastMessageReceiver(private val context: Context) : BroadcastReceiver()
{
    private var messageReceiveCallBack : ((Intent) -> Unit)? = null
    private var isRegistered = false

    companion object
    {
        private val TAG: String = BroadcastMessageReceiver::class.java.simpleName
    }

    fun setOnMessageReceived(callBack : ((Intent) -> Unit)?)
    {
        messageReceiveCallBack = callBack
    }

    override fun onReceive(context: Context, intent: Intent)
    {
        Log.d(TAG, "onReceive() intent = [$intent]")
        messageReceiveCallBack?.invoke(intent)

    }

    fun registerEvents(intentFilter: IntentFilter)
    {
        Log.d(TAG, "registerEvents() called")

        try
        {
            if (!isRegistered)
                ContextCompat.registerReceiver(context,this, intentFilter,ContextCompat.RECEIVER_NOT_EXPORTED)


            isRegistered = true
        }
        catch (e: IllegalArgumentException)
        {
            isRegistered = false
            e.printStackTrace()
        }
    }

    fun unregisterEvents()
    {
        Log.d(TAG, "unregister() called")

        try
        {
            if (isRegistered)
                context.unregisterReceiver(this)

            isRegistered = false
        }
        catch (e: IllegalArgumentException)
        {
            isRegistered = false
            e.printStackTrace()
        }
    }


}