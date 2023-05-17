package github.umer0586.sensorserver.broadcastreceiver;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;

import github.umer0586.sensorserver.service.SensorService;


public class BroadcastMessageReceiver extends BroadcastReceiver {

    private static final String TAG = BroadcastMessageReceiver.class.getSimpleName();

    private MessageListener messageListener;

    private Context context;

    private boolean isRegistered = false;


    public BroadcastMessageReceiver(@NonNull Context context)
    {
        this.context = context;
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.d(TAG, "onReceive() called with: context = [" + context + "], intent = [" + intent + "]");

        // Dispatch received broadcast intent via MessageListener Interface
        if(messageListener != null)
            messageListener.onMessage(intent);

    }

    public void setMessageListener(MessageListener messageListener)
    {
        this.messageListener = messageListener;
    }

    public void registerEvents()
    {
        Log.d(TAG, "registerEvents() called");

        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(SensorService.ACTION_STOP_SERVER);


        try
        {
            if(!isRegistered)
                this.context.registerReceiver(this, intentFilter);

            isRegistered = true;
        }catch(IllegalArgumentException e)
        {
            isRegistered = false;
            e.printStackTrace();
        }
    }

    public void unregisterEvents()
    {
        Log.d(TAG, "unregister() called");
        try
        {
            if (isRegistered)
                this.context.unregisterReceiver(this);

            isRegistered = false;
        }catch(IllegalArgumentException e)
        {
            isRegistered = false;
            e.printStackTrace();
        }
    }


    public interface MessageListener {
        public void onMessage(Intent intent);
    }
}
