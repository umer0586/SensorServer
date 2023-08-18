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

class ServiceBindHelper(private val context: Context,
                        private val service: Class<out Service>,
                        componentLifecycle : Lifecycle
) : ServiceConnection, LifecycleEventObserver
{

    init
    {
        // Make this class observe lifecycle of Activity/Fragment
        componentLifecycle.addObserver(this)
    }


    private var bounded = false
    private var onServiceConnectedCallBack : ((IBinder) -> Unit)? = null


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

    fun onServiceConnected(callBack: ((IBinder) -> Unit)?)
    {
        onServiceConnectedCallBack = callBack
    }

    override fun onServiceConnected(name: ComponentName, binder: IBinder)
    {
        Log.d(TAG, "onServiceConnected()")
        bounded = true

        onServiceConnectedCallBack?.invoke(binder)

    }

/*    The onServiceDisconnected() method in Android is called when the connection to the service is unexpectedly disconnected,
    usually due to a crash or the service being killed by the system.
    This allows you to handle the situation and possibly attempt to reestablish the connection.
    onServiceDisconnected() method is not called when you explicitly call context.unbindService().
    It's only called when the connection to the service is unexpectedly lost, such as when the service process crashes or is killed by the system.*/
    override fun onServiceDisconnected(name: ComponentName)
    {
        Log.d(TAG, "onServiceDisconnected()")
        bounded = false

    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event)
    {
        Log.d(TAG + " : " + source.javaClass.simpleName, event.name)

        when(event)
        {
            Lifecycle.Event.ON_RESUME -> bindToService()
            Lifecycle.Event.ON_PAUSE -> unBindFromService()
            else ->{}

        }

    }

    companion object
    {
        private val TAG: String = ServiceBindHelper::class.java.simpleName
    }
}