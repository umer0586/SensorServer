package github.umer0586.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

import github.umer0586.R;
import github.umer0586.activity.FragmentNavigationActivity;
import github.umer0586.broadcastreceiver.BroadcastMessageReceiver;
import github.umer0586.websocketserver.ConnectionsChangeListener;
import github.umer0586.websocketserver.ConnectionsCountChangeListener;
import github.umer0586.websocketserver.SensorWebSocketServer;
import github.umer0586.websocketserver.ServerInfo;
import github.umer0586.setting.AppSettings;
import github.umer0586.util.IpUtil;

public class SensorService extends Service implements BroadcastMessageReceiver.MessageListener {

    private static final String TAG = SensorService.class.getSimpleName();


    private SensorWebSocketServer sensorWebSocketServer;
    private AppSettings appSettings;

    // Binder given to clients
    private final IBinder binder = new LocalBinder();

    public static final String CHANNEL_ID = "ForegroundServiceChannel";

    // cannot be zero
    public static final int ON_GOING_NOTIFICATION_ID = 332;
    private static final int TEMP_NOTIFICATION_ID = 421;

    // Broadcast intent action (published by other app's component) to stop server thread
    public static final String ACTION_STOP_SERVER = "ACTION_STOP_SERVER_"+SensorService.class.getName();


    //Intents broadcast by Fragment/Activity are received by this service via MessageReceiver (BroadCastReceiver)
    private BroadcastMessageReceiver broadcastMessageReceiver;


    //Callbacks

    private ServerStateListener serverStateListener;

    private ConnectionsChangeListener connectionsChangeListener;
    private ConnectionsCountChangeListener connectionsCountChangeListener;

    public interface ServerStateListener {
        void onServerStarted(ServerInfo serverInfo);
        void onServerStopped();
        void onServerError(Exception ex);
        void onServerAlreadyRunning(ServerInfo serverInfo);
    }


    @Override
    public void onCreate()
    {
        super.onCreate();
        Log.d(TAG, "onCreate()");

        createNotificationChannel();
        appSettings = new AppSettings(getApplicationContext());


        broadcastMessageReceiver = new BroadcastMessageReceiver(getApplicationContext());
        broadcastMessageReceiver.setMessageListener(this);
        broadcastMessageReceiver.registerEvents();
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
        handleAndroid8andAbove();

        if(appSettings.isHotspotOptionEnabled())
        {
            String hotspotIpAddress = IpUtil.getHotspotIPAddress(getApplicationContext());

            if( hotspotIpAddress != null)
            {
                sensorWebSocketServer = new SensorWebSocketServer(
                        getApplicationContext(),
                        new InetSocketAddress(hotspotIpAddress,appSettings.getPortNo())
                );
            }
            else
            {
                if(serverStateListener != null)
                    serverStateListener.onServerError(new UnknownHostException("Unable to obtain hotspot IP"));

                stopForeground(true);
                return START_NOT_STICKY;
            }
        }
        else if(appSettings.isLocalHostOptionEnable())
        {
            sensorWebSocketServer = new SensorWebSocketServer(
                    getApplicationContext(),
                    new InetSocketAddress("127.0.0.1",appSettings.getPortNo())
            );
        }
        else
        {
            String wifiIpAddress = IpUtil.getWifiIpAddress(getApplicationContext());

            if(wifiIpAddress != null)
            {
                sensorWebSocketServer = new SensorWebSocketServer(
                        getApplicationContext(),
                        new InetSocketAddress(wifiIpAddress,appSettings.getPortNo())
                );
            }
            else
            {
                if (serverStateListener != null)
                    serverStateListener.onServerError(new UnknownHostException("Unable to obtain IP"));

                stopForeground(true);

                return START_NOT_STICKY;
            }
        }


        sensorWebSocketServer.setServerStartListener((serverInfo)->{

          if(serverStateListener != null)
              serverStateListener.onServerStarted(serverInfo);

            Intent notificationIntent = new Intent(this, FragmentNavigationActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_radar_signal)
                    .setContentTitle("Sensor Server Running...")
                    .setContentText("ws://"+serverInfo.getIpAddress()+":"+serverInfo.getPort())
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    // Set the intent that will fire when the user taps the notification
                    .setContentIntent(pendingIntent)
                    // don't cancel notification when user taps it
                    .setAutoCancel(false);

            Notification notification = notificationBuilder.build();
            startForeground(ON_GOING_NOTIFICATION_ID, notification);


        });

        sensorWebSocketServer.setServerStopListener(()->{

            if(serverStateListener != null)
                serverStateListener.onServerStopped();

            //remove the service from foreground but don't stop (destroy) the service
            stopForeground(true);

        });
        sensorWebSocketServer.setServerErrorListener((exception)->{

            if(serverStateListener != null)
                serverStateListener.onServerError(exception);

            stopForeground(true);

        });

        sensorWebSocketServer.setConnectionsChangeListener((webSockets)->{
            if(connectionsChangeListener != null)
                connectionsChangeListener.onConnectionsChanged(webSockets);
            if(connectionsCountChangeListener != null)
                connectionsCountChangeListener.onConnectionCountChange(webSockets.size());
        });

        sensorWebSocketServer.setSamplingRate(appSettings.getSamplingRate());

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
            stopForeground(true);


        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");


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

        broadcastMessageReceiver.unregisterEvents();


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
        return sensorWebSocketServer != null ? sensorWebSocketServer.getConnections().size() : 0;
    }

    public void isServerRunning()
    {
        if(sensorWebSocketServer != null && sensorWebSocketServer.isRunning())
        {
            if(serverStateListener != null)
            {
                ServerInfo serverInfo = new ServerInfo(sensorWebSocketServer.getAddress().getHostName(),sensorWebSocketServer.getPort());
                serverStateListener.onServerAlreadyRunning(serverInfo);

            }

        }
    }

    public ArrayList<WebSocket> getConnectedClients()
    {
        if(sensorWebSocketServer != null)
            return new ArrayList<>(sensorWebSocketServer.getConnections());

        return null;

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


    public void setServerStateListener(ServerStateListener serverStateListener)
    {
        this.serverStateListener = serverStateListener;
    }

    public void setConnectionsChangeListener(ConnectionsChangeListener connectionsChangeListener)
    {
        this.connectionsChangeListener = connectionsChangeListener;
    }

    public void setConnectionsCountChangeListener(ConnectionsCountChangeListener connectionsCountChangeListener)
    {
        this.connectionsCountChangeListener = connectionsCountChangeListener;
    }
}