package github.umer0586.sensorserver.service

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import github.umer0586.sensorserver.service.ServiceBindHelper

class ServiceBindHelper(private val context: Context, private val serviceConnection: ServiceConnection, private val service: Class<out Service>
) : ServiceConnection, LifecycleEventObserver
{


    private var bounded = false


    private fun bindToService()
    {
        Log.d(TAG, "bindToService()")
        val intent = Intent(context, service)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        bounded = true
    }

    private fun unBindFromService()
    {
        Log.d(TAG, "unBindFromService()")
        if (bounded)
        {
            context.unbindService(this)
            bounded = false
        }
    }

    override fun onServiceConnected(name: ComponentName, service: IBinder)
    {
        Log.d(TAG, "onServiceConnected()")
        bounded = true
        serviceConnection.onServiceConnected(name, service)
    }

    override fun onServiceDisconnected(name: ComponentName)
    {
        Log.d(TAG, "onServiceDisconnected()")
        bounded = false
        serviceConnection.onServiceDisconnected(name)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event)
    {
        Log.d(TAG + " : " + source.javaClass.getSimpleName(), event.name)
        if (event == Lifecycle.Event.ON_RESUME)
            bindToService()
        else if (event == Lifecycle.Event.ON_PAUSE)
            unBindFromService()
    }

    companion object
    {
        private val TAG: String = ServiceBindHelper::class.java.getSimpleName()
    }
}