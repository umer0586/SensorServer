package github.umer0586.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.activity.FragmentNavigationActivity;
import github.umer0586.broadcastreceiver.MessageReceiver;
import github.umer0586.sensorserver.ConnectionCountChangeListener;
import github.umer0586.sensorserver.ConnectionInfo;
import github.umer0586.sensorserver.ConnectionInfoChangeListener;
import github.umer0586.sensorserver.SensorWebSocketServer;
import github.umer0586.sensorserver.ServerErrorListener;
import github.umer0586.sensorserver.ServerStartListener;
import github.umer0586.sensorserver.ServerStopListener;
import github.umer0586.util.IpUtil;

public class SensorService extends Service implements MessageReceiver.MessageListener {

    private static final String TAG = SensorService.class.getSimpleName();


    private SensorWebSocketServer sensorWebSocketServer;
    private SharedPreferences sharedPreferences;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    // cannot be zero
    public static final int ON_GOING_NOTIFICATION_ID = 332;
    private static final int TEMP_NOTIFICATION_ID = 421;

    // Broadcast intent action (published by other app's component) to stop server thread
    public static final String ACTION_STOP_SERVER = "ACTION_STOP_SERVER_"+SensorService.class.getName();


    //Intents broadcast by Fragment/Activity are received by this service via MessageReceiver (BroadCastReceiver)
    private MessageReceiver messageReceiver;


    //Callbacks
    private ServerStartListener serverStartListener;
    private ServerStopListener serverStopListener;
    private ConnectionInfoChangeListener connectionInfoChangeListener;
    private ServerErrorListener serverErrorListener;
    private ConnectionCountChangeListener connectionCountChangeListener;


    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate() called");

        createNotificationChannel();
        sharedPreferences = getApplicationContext().getSharedPreferences(getString(R.string.shared_pref_file),getApplicationContext().MODE_PRIVATE);


        messageReceiver = new MessageReceiver(getApplicationContext());
        messageReceiver.setMessageListener(this);
        messageReceiver.registerEvents();
    }

    @Override
    public void onMessage(Intent intent)
    {
        Log.d(TAG, "onMessage() called with: intent = [" + intent + "]");

        if( intent.getAction().equals(ACTION_STOP_SERVER) )
        {
            if(sensorWebSocketServer != null && sensorWebSocketServer.isRunning())
            {
                try
                {
                    sensorWebSocketServer.stop();
                    stopForeground(true);

                } catch (Exception e)
                {
                    e.printStackTrace();
                }

            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {

        Log.d(TAG, "onStartCommand()");


        boolean localHostPref = sharedPreferences.getBoolean(getString(R.string.pref_key_localhost),false);

        String ipAddress = null;

        // is "local host" switch in enable
        // no need to check for wifi network
        if(localHostPref)
            ipAddress = "127.0.0.1"; // use loopback address
        else // check wifi
            ipAddress = IpUtil.getWifiIpAddress(getApplicationContext());

        // This condition will always be false when localHostPref is true, hence no network check
        if(ipAddress == null)
        {
            Log.i(TAG, "hostIP = null");

            if(serverErrorListener != null)
                serverErrorListener.onError(new UnknownHostException());

            handleAndroid8andAbove();
            stopForeground(true);

            return START_NOT_STICKY;
        }

        int portNo = sharedPreferences.getInt(getString(R.string.pref_key_port_no),8081);

        sensorWebSocketServer = new SensorWebSocketServer(
                getApplicationContext(),
                new InetSocketAddress(ipAddress,portNo)
        );


        sensorWebSocketServer.setServerStartListener((hostIP, port)->{

          if(serverStartListener != null)
                serverStartListener.onServerStarted(hostIP,port);

            Intent notificationIntent = new Intent(this, FragmentNavigationActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_signal)
                    .setContentTitle("Sensor Server Running...")
                    .setContentText("ws://"+hostIP+":"+port)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    // don't cancel notification when user taps it
                    .setAutoCancel(false);

            Notification notification = notificationBuilder.build();
            startForeground(ON_GOING_NOTIFICATION_ID, notification);


        });

        sensorWebSocketServer.setServerStopListener(()->{

            if(serverStopListener != null)
                serverStopListener.onServerStopped();

            //remove the service from foreground but don't stop (destroy) the service
            stopForeground(true);

        });
        sensorWebSocketServer.setServerErrorListener((exception)->{

            if(serverErrorListener != null)
                serverErrorListener.onError(exception);

            stopForeground(true);

        });
        sensorWebSocketServer.setConnectionCountChangeListener((totalConnections)->{
                if(connectionCountChangeListener != null)
                    connectionCountChangeListener.onConnectionCountChange(totalConnections);
        });
        sensorWebSocketServer.setConnectionInfoChangeListener((connectionInfoList)->{
                if(connectionInfoChangeListener != null)
                    connectionInfoChangeListener.onConnectionInfoChanged(connectionInfoList);
        });

        int samplingRate = sharedPreferences.getInt(getString(R.string.pref_key_sampling_rate),200000);

        sensorWebSocketServer.setSamplingRate(samplingRate);

        sensorWebSocketServer.run();


        return START_NOT_STICKY;
    }


    private void createNotificationChannel()
    {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Log.d(TAG, "createNotificationChannel() called");

            CharSequence name = "SMS-Server";
            String description = "Notifications from SMS-server";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    /*
     * For Android 8 and above there is a framework restriction which required service.startForeground()
     * method to be called within five seconds after call to Context.startForegroundService()
     * so make sure we call this method even if we are returning from service.onStartCommand() without calling
     * service.startForeground()
     *
     * */
    private void handleAndroid8andAbove()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {

            Notification tempNotification =  new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_signal)
                    .setContentTitle("")
                    .setContentText("").build();

            startForeground(TEMP_NOTIFICATION_ID, tempNotification);
            //stopForeground(true);


        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy() called");


        if (sensorWebSocketServer != null)
        {
            try
                {
                    Log.d(TAG, "calling server.stop()");
                    sensorWebSocketServer.stop();

                } catch (Exception e)
                {
                    e.printStackTrace();
                }
        }

        messageReceiver.unregisterEvents();


    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public SensorWebSocketServer getSensorWebSocketServer()
    {
        return sensorWebSocketServer;
    }

    public int getConnectionCount()
    {
        return sensorWebSocketServer != null ? sensorWebSocketServer.getConnectionCount() : 0;
    }

    public ArrayList<ConnectionInfo> getConnectionInfoList()
    {
        return sensorWebSocketServer != null ? sensorWebSocketServer.getConnectionInfoList() : null;
    }


    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {

        public SensorService getService() {
            // Return this instance of LocalService so clients can call public methods
            return SensorService.this;
        }

    }


    public void setConnectionCountChangeListener(ConnectionCountChangeListener connectionCountChangeListener)
    {
        this.connectionCountChangeListener = connectionCountChangeListener;
    }

    public void setServerStartListener(ServerStartListener serverStartListener)
    {
        this.serverStartListener = serverStartListener;
    }

    public void setServerStopListener(ServerStopListener serverStopListener)
    {
        this.serverStopListener = serverStopListener;
    }

    public void setConnectionInfoChangeListener(ConnectionInfoChangeListener connectionInfoChangeListener)
    {
       this.connectionInfoChangeListener = connectionInfoChangeListener;
    }

    public void setServerErrorListener(ServerErrorListener serverErrorListener)
    {
        this.serverErrorListener = serverErrorListener;
    }


}