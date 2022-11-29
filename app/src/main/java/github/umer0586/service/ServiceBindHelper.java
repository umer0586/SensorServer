package github.umer0586.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;

public class ServiceBindHelper implements ServiceConnection, LifecycleEventObserver {

    private static final String TAG = ServiceBindHelper.class.getSimpleName();

    private boolean bounded = false;
    private Context context;
    private ServiceConnection serviceConnection;
    private Class service;



    public ServiceBindHelper(Context context,ServiceConnection serviceConnection, Class<? extends Service> service)
    {
        this.context = context;
        this.serviceConnection = serviceConnection;
        this.service = service;
    }

    public void bindToService()
    {
        Log.d(TAG, "bindToService()");

        Intent intent = new Intent(this.context, this.service);
        context.bindService(intent, this, Context.BIND_AUTO_CREATE);
        bounded = true;
    }

    public void unBindFromService()
    {
        Log.d(TAG, "unBindFromService()");
        if(bounded)
        {
            context.unbindService(this);
            bounded = false;
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service)
    {
        Log.d(TAG, "onServiceConnected()");

        bounded = true;
        if(serviceConnection != null)
            serviceConnection.onServiceConnected(name,service);

    }

    @Override
    public void onServiceDisconnected(ComponentName name)
    {
        Log.d(TAG, "onServiceDisconnected()");

        bounded = false;
        if(serviceConnection != null)
            serviceConnection.onServiceDisconnected(name);

    }

    @Override
    public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event)
    {
        Log.d(TAG + " : " + source.getClass().getSimpleName(), event.name());

        if(event == Lifecycle.Event.ON_RESUME)
            bindToService();
        else if(event == Lifecycle.Event.ON_PAUSE)
            unBindFromService();

    }
}
