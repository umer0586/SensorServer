package github.umer0586.sensorserver.broadcastreceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import github.umer0586.sensorserver.broadcastreceiver.BroadcastMessageReceiver
import github.umer0586.sensorserver.service.SensorService

class BroadcastMessageReceiver(private val context: Context) : BroadcastReceiver()
{

    private var messageListener: MessageListener? = null
    private var isRegistered = false

    companion object
    {
        private val TAG: String = BroadcastMessageReceiver::class.java.getSimpleName()
    }

    override fun onReceive(context: Context, intent: Intent)
    {
        Log.d(TAG, "onReceive() called with: context = [$context], intent = [$intent]")

        // Dispatch received broadcast intent via MessageListener Interface
        if (messageListener != null) messageListener?.onMessage(intent)
    }

    fun setMessageListener(messageListener: MessageListener?)
    {
        this.messageListener = messageListener
    }

    fun registerEvents()
    {
        Log.d(TAG, "registerEvents() called")

        val intentFilter = IntentFilter()
        intentFilter.addAction(SensorService.ACTION_STOP_SERVER)

        try
        {
            if (!isRegistered) context.registerReceiver(this, intentFilter)
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
            if (isRegistered) context.unregisterReceiver(this)
            isRegistered = false
        }
        catch (e: IllegalArgumentException)
        {
            isRegistered = false
            e.printStackTrace()
        }
    }

    interface MessageListener
    {
        fun onMessage(intent: Intent)
    }


}