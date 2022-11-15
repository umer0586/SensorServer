package github.umer0586.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;

public class ServiceBindHelper {

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
        Intent intent = new Intent(this.context, this.service);
        context.bindService(intent, this.serviceConnection, Context.BIND_AUTO_CREATE);
        bounded = true;
    }

    public void unBindFromService()
    {
        if(bounded)
        {
            context.unbindService(this.serviceConnection);
            bounded = false;
        }
    }

    public void setBounded(boolean bounded)
    {
        this.bounded = bounded;
    }
}
